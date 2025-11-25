<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

const menuStates = ref({ menu1: false, menu2: false, menu3: false, menu4: false });
const menuRefs = ref({ menu1: null, menu2: null, menu3: null, menu4: null });
const timeoutIds = ref({ menu1: null, menu2: null, menu3: null, menu4: null });

const toggleMenu = (menu) => {
  Object.keys(timeoutIds.value).forEach(key => clearTimeout(timeoutIds.value[key]));
  const newState = !menuStates.value[menu];
  Object.keys(menuStates.value).forEach(key => menuStates.value[key] = false);
  menuStates.value[menu] = newState;
  if (newState) startAutoCloseTimer(menu);
};

const startAutoCloseTimer = (menu) => {
  clearTimeout(timeoutIds.value[menu]);
  timeoutIds.value[menu] = setTimeout(() => menuStates.value[menu] = false, 2000);
};

const handleMouseEnter = (menu) => clearTimeout(timeoutIds.value[menu]);
const handleMouseLeave = (menu) => menuStates.value[menu] && startAutoCloseTimer(menu);

const handleLogout = () => {
  localStorage.removeItem('userToken');
  router.push('/login');
};

const closeMenus = (event) => {
  let clickedOutside = true;
  Object.entries(menuRefs.value).forEach(([key, ref]) => {
    if (ref?.contains(event.target)) clickedOutside = false;
  });
  if (clickedOutside) {
    Object.keys(menuStates.value).forEach(key => {
      menuStates.value[key] = false;
      clearTimeout(timeoutIds.value[key]);
    });
  }
};

// 合并所有 onMounted 逻辑到一个钩子中
onMounted(() => {
  // 路由重定向逻辑
  if (route.path === '/main') {
    router.replace('/tool-map');
  }
  
  // 菜单点击外部关闭逻辑
  document.addEventListener('click', closeMenus);
});

onUnmounted(() => {
  document.removeEventListener('click', closeMenus);
  Object.keys(timeoutIds.value).forEach(key => clearTimeout(timeoutIds.value[key]));
});
</script>

<template>
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <div class="container">
    <header class="top-bar">
      <div class="brand">
        <h1>社区共享工具库</h1>
      </div>
      <div class="menu-group">
        <!-- 工具地图菜单 -->
        <div class="menu-item" ref="menuRefs.menu1">
          <button @click.stop="toggleMenu('menu1')" class="menu-button">
            <span class="material-icons">map</span>
            工具地图
          </button>
          <transition name="slide">
            <div v-if="menuStates.menu1" class="submenu" @mouseenter="handleMouseEnter('menu1')" @mouseleave="handleMouseLeave('menu1')">
              <RouterLink to="/tool-map" class="submenu-item" @click="menuStates.menu1 = false">
                <span class="material-icons">map</span>
                地图浏览
              </RouterLink>
            </div>
          </transition>
        </div>

        <!-- 借用管理菜单 -->
        <div class="menu-item" ref="menuRefs.menu2">
          <button @click.stop="toggleMenu('menu2')" class="menu-button">
            <span class="material-icons">handyman</span>
            借用管理
          </button>
          <transition name="slide">
            <div v-if="menuStates.menu2" class="submenu" @mouseenter="handleMouseEnter('menu2')" @mouseleave="handleMouseLeave('menu2')">
              <RouterLink to="/my-borrow" class="submenu-item" @click="menuStates.menu2 = false">
                <span class="material-icons">list</span>
                我的借用
              </RouterLink>
              <RouterLink to="/my-published" class="submenu-item" @click="menuStates.menu2 = false">
                <span class="material-icons">inventory_2</span>
                我发布的
              </RouterLink>
            </div>
          </transition>
        </div>

        <!-- 消息与沟通菜单 -->
        <div class="menu-item" ref="menuRefs.menu3">
          <button @click.stop="toggleMenu('menu3')" class="menu-button">
            <span class="material-icons">chat</span>
            消息沟通
          </button>
          <transition name="slide">
            <div v-if="menuStates.menu3" class="submenu" @mouseenter="handleMouseEnter('menu3')" @mouseleave="handleMouseLeave('menu3')">
              <RouterLink to="/message-center" class="submenu-item" @click="menuStates.menu3 = false">
                <span class="material-icons">notifications</span>
                消息中心
              </RouterLink>
              <RouterLink to="/community-chat" class="submenu-item" @click="menuStates.menu3 = false">
                <span class="material-icons">forum</span>
                社区聊天
              </RouterLink>
            </div>
          </transition>
        </div>

        <!-- 个人中心菜单 -->
        <div class="menu-item" ref="menuRefs.menu4">
          <button @click.stop="toggleMenu('menu4')" class="menu-button">
            <span class="material-icons">person</span>
            个人中心
          </button>
          <transition name="slide">
            <div v-if="menuStates.menu4" class="submenu" @mouseenter="handleMouseEnter('menu4')" @mouseleave="handleMouseLeave('menu4')">
              <RouterLink to="/profile" class="submenu-item" @click="menuStates.menu4 = false">
                <span class="material-icons">account_circle</span>
                个人信息
              </RouterLink>
              <RouterLink to="/credit" class="submenu-item" @click="menuStates.menu4 = false">
                <span class="material-icons">credit_score</span>
                信用与押金
              </RouterLink>
            </div>
          </transition>
        </div>
      </div>
      <div class="user-profile">
        <span class="material-icons">account_circle</span>
        <span>用户</span>
        <button @click="handleLogout" class="logout-button">退出</button>
      </div>
    </header>
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', system-ui, sans-serif;
}

.container {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  background: #f5f6fa;
}

.top-bar {
  position: relative;
  flex-shrink: 0;
  height: 60px;
  background: linear-gradient(180deg, #2c3e50, #34495e);
  display: flex;
  align-items: center;
  padding: 0 20px;
  z-index: 1000;
}

.brand h1 {
  color: #ecf0f1;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1;
}

.menu-group {
  display: flex;
  height: 100%;
  margin-left: 30px;
}

.menu-item {
  position: relative;
  height: 100%;
}

.menu-button {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 15px;
  background: transparent;
  border: none;
  color: #ecf0f1;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.2s;
}

.menu-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.menu-button .material-icons {
  font-size: 1.2rem;
  margin-right: 8px;
}

.submenu {
  position: absolute;
  top: 100%;
  left: 0;
  min-width: 200px;
  background: #34495e;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 1001;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  color: #bdc3c7;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.submenu-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: #ecf0f1;
}

.submenu-item .material-icons {
  font-size: 1.1rem;
  margin-right: 10px;
}

.submenu-item.router-link-active {
  background: rgba(52, 152, 219, 0.2);
  color: #ecf0f1;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
  color: #ecf0f1;
}

.logout-button {
  padding: 6px 12px;
  background: #e74c3c;
  border: none;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.logout-button:hover {
  background: #c0392b;
}

.main-content {
  flex: 1;
  overflow: auto;
  position: relative;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .top-bar {
    padding: 0 10px;
  }

  .menu-button {
    padding: 0 10px;
    font-size: 0.85rem;
  }

  .menu-button .material-icons {
    margin-right: 5px;
  }

  .brand h1 {
    font-size: 1.2rem;
  }
}
</style>