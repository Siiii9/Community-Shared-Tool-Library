<template>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <div class="container">
      <!-- 右侧区域 -->
      <main class="right-side">
        <section class="main-content">
          <div class="content-card">
            <!-- 筛选区域 -->
            <div class="filter-form">
              <div class="row mb-3">
                <div class="col-md-3">
                  <select v-model="filterStatus" class="form-select">
                    <option value="all">全部</option>
                    <option value="processing">进行中</option>
                    <option value="completed">已完成</option>
                    <option value="terminated">终止</option>
                  </select>
                </div>
                <div class="col-md-3">
                  <input type="date" v-model="filterDate" class="form-control" placeholder="选择日期">
                </div>
                <div class="col-md-3">
                  <input type="text" v-model="filterName" class="form-control" placeholder="任务名查询">
                </div>
                <div class="col-md-3">
                  <input type="text" v-model="filterOwner" class="form-control" placeholder="负责人查询">
                </div>
              </div>
              <button class="btn btn-primary" @click="applyFilter">应用筛选</button>
              <button class="btn btn-secondary" @click="resetFilter">重置筛选</button>
            </div>
  
            <!-- 任务表格 -->
            <table class="table table-hover align-middle">
              <thead>
                <tr>
                  <th @click="sortData('name')">任务名</th>
                  <th @click="sortData('shared')">共享权限</th>
                  <th @click="sortData('description')">任务说明</th>
                  <th @click="sortData('status')">任务进度</th>
                  <th @click="sortData('createdAt')">生成时间</th>
                  <th>负责人</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(task, index) in filteredTasks" :key="index">
                  <td>{{ task.name }}</td>
                  <td>{{ task.shared }}</td>
                  <td>{{ task.description }}</td>
                  <td>
                    <span :class="`status-badge ${task.status}`">{{ task.status | capitalize }}</span>
                  </td>
                  <td>{{ task.createdAt }}</td>
                  <td>{{ task.owner }}</td>
                  <td>
                    <a href="#" class="text-decoration-none" @click.prevent="openTaskDetails(task)">查看详情</a>
                  </td>
                </tr>
              </tbody>
            </table>
  
            <!-- 分页 -->
            <nav>
              <ul class="pagination justify-content-center">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                  <a class="page-link" href="#" @click.prevent="previousPage">上一页</a>
                </li>
                <li class="page-item" v-for="page in pageNumbers" :key="page" :class="{ active: page === currentPage }">
                  <a class="page-link" href="#" @click.prevent="currentPage = page">{{ page }}</a>
                </li>
                <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                  <a class="page-link" href="#" @click.prevent="nextPage">下一页</a>
                </li>
              </ul>
            </nav>
          </div>
        </section>
      </main>
    </div>
  </template>
  
  <script setup>
  import { ref, computed } from 'vue';
  import { useRouter } from 'vue-router';
  
  // 路由
  const router = useRouter();
  
  // 退出登录
  const handleLogout = () => {
    localStorage.removeItem('userToken');
    router.push('/login');
  };
  
  // 任务列表状态和筛选
  const tasks = ref([
    {
      name: 'TEST',
      shared: 'PRIVATE',
      description: '说明用例',
      status: 'completed',
      createdAt: '2025-02-26',
      owner: '张三'
    },
    {
      name: '测试任务2',
      shared: 'PUBLIC',
      description: '说明用例2',
      status: 'processing',
      createdAt: '2025-02-27',
      owner: '李四'
    },
    // 更多任务数据...
  ]);
  
  const filterStatus = ref('all');
  const filterDate = ref('');
  const filterName = ref('');
  const filterOwner = ref('');
  const currentPage = ref(1);
  const tasksPerPage = ref(5);
  
  // 过滤和分页
  const filteredTasks = computed(() => {
    let filtered = tasks.value;
  
    if (filterStatus.value !== 'all') {
      filtered = filtered.filter((task) => task.status === filterStatus.value);
    }
  
    if (filterDate.value) {
      filtered = filtered.filter((task) => new Date(task.createdAt) >= new Date(filterDate.value));
    }
  
    if (filterName.value) {
      filtered = filtered.filter((task) => task.name.includes(filterName.value));
    }
  
    if (filterOwner.value) {
      filtered = filtered.filter((task) => task.owner.includes(filterOwner.value));
    }
  
    return filtered;
  });
  
  // 分页计算
  const pageNumbers = computed(() => {
    const numberOfPages = Math.ceil(filteredTasks.value.length / tasksPerPage.value);
    return Array.from({ length: numberOfPages }, (_, index) => index + 1);
  });
  
  const totalPages = computed(() => pageNumbers.value.length);
  
  // 分页方法
  const previousPage = () => {
    if (currentPage.value > 1) {
      currentPage.value -= 1;
    }
  };
  
  const nextPage = () => {
    if (currentPage.value < totalPages.value) {
      currentPage.value += 1;
    }
  };
  
  // 任务详情
  const openTaskDetails = (task) => {
    console.log('查看详情:', task);
    // 这里可以实现跳转到任务详情页面的逻辑
  };
  
  // 筛选和重置
  const applyFilter = () => {
    currentPage.value = 1;
  };
  
  const resetFilter = () => {
    filterStatus.value = 'all';
    filterDate.value = '';
    filterName.value = '';
    filterOwner.value = '';
    currentPage.value = 1;
  };
  </script>
  
  <style scoped>
  /* 右侧区域 */
  .right-side {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  .header {
    background: white;
    padding: 1rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .header h1 {
    font-size: 1.6rem;
    font-weight: 600;
    color: #2c3e50;
  }
  
  .user-profile {
    display: flex;
    align-items: center;
    gap: 1rem;
    color: #2c3e50;
    font-size: 1rem;
    font-weight: 500;
  }
  
  .logout-button {
    padding: 0.5rem 1rem;
    background: #e74c3c;
    border: none;
    border-radius: 4px;
    color: white;
    cursor: pointer;
    transition: background 0.3s;
  }
  
  .logout-button:hover {
    background: #c0392b;
  }
  
  /* 内容卡片样式 */
  .content-card {
    background: white;
    border-radius: 12px;
    padding: 2rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    margin: 1rem;
  }
  
  /* 筛选区域 */
  .filter-form {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
    margin-bottom: 1.5rem;
  }
  
  .form-select, .form-control {
    width: 100%;
    padding: 0.75rem;
    background-color: #fff;
    border: 1px solid #ced4da;
    border-radius: 0.25rem;
  }
  
  .form-select:focus, .form-control:focus {
    border-color: #096dd9;
    outline: none;
  }
  
  button {
    margin-top: 10px;
    padding: 0.5rem 1rem;
    cursor: pointer;
  }
  
  .btn-primary {
    background-color: #096dd9;
    color: white;
  }
  
  .btn-primary:hover {
    background-color: #0056b3;
  }
  
  .btn-secondary {
    background-color: #ccc;
    color: white;
  }
  
  .btn-secondary:hover {
    background-color: #999;
  }
  
  /* 任务表格 */
  .table {
    width: 100%;
    margin-bottom: 1rem;
    color: #212529;
    border-collapse: collapse;
  }
  
  .table-hover {
    cursor: pointer;
  }
  
  .table th, .table td {
    padding: 12px;
    text-align: left;
    border: 1px solid #ddd;
  }
  
  .table tbody tr:hover {
    background-color: #f8f9fa;
  }
  
  /* 状态标签 */
  .status-badge {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 0.9em;
    font-weight: 500;
  }
  
  .completed {
    background: #e6f4ea;
    color: #389e0d;
  }
  
  .processing {
    background: #e6f4ff;
    color: #096dd9;
  }
  
  .terminated {
    background: #fff1f0;
    color: #cf1322;
  }
  
  .not-started {
    background: #f5f5f5;
    color: #595959;
  }
  
  /* 分页样式 */
  .pagination {
    display: flex;
    list-style: none;
    justify-content: center;
    margin-top: 1.5rem;
  }
  
  .page-item {
    margin: 0 5px;
  }
  
  .page-link {
    display: block;
    padding: 0.5rem 1rem;
    background-color: #fff;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    color: #096dd9;
    text-decoration: none;
    cursor: pointer;
  }
  
  .page-link:hover {
    background-color: #e9ecef;
    color: #0056b3;
  }
  
  .page-item.active .page-link {
    background-color: #096dd9;
    color: white;
  }
  
  .page-item.disabled .page-link {
    background-color: #f5f5f5;
    color: #6c757d;
    cursor: not-allowed;
  }
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .header h1 {
      font-size: 1.3rem;
    }
  
    .user-profile {
      font-size: 0.9rem;
    }
  
    .content-card {
      padding: 1.5rem;
    }
  
    .table th, .table td {
      font-size: 0.9rem;
    }
  }
  </style>
  