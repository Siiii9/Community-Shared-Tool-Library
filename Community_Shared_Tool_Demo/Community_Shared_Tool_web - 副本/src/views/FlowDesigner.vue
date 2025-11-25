<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from 'vue';

interface Item {
    id: string;
    name: string;
    x: number;
    y: number;
    isHovered: boolean;
    params?: { [key: string]: string };//可选的params属性，用于包含键值对，限制值为string、number或boolean
}

interface Line {
    start: { x: number, y: number };
    end: { x: number, y: number };
    startElementId: number | null;//起点元素的id
    endElementId: number | null;//终点元素的id
    opRunId?: string | null;//可选的oprunid属性
    group?: string;//新增的连线组别属性
    status?: string;//新增的操作状态属性
}

interface ConditionNode {
    id: string;//节点唯一ID
    type: "condition";//标记为条件节点
    conditions: {
        expression: string;//条件表达式，例如 "value > 10"
        targetNodeId: string;//满足条件时连接的目标节点ID
    }[];
    defaultTargetNodeId: string;//默认目标节点ID（条件均不满足时）
}

const canvas_items = ref<Item[]>([]);
const lines = ref<Line[]>([]);
const canvas = ref<HTMLCanvasElement | null>(null);
const canvasContainer = ref<HTMLDivElement | null>(null);
let dragItemIndex: number | null = null;
let offsetX = 0;
let offsetY = 0;
let drawingLine = false;
let currentLine: Line | null = null;
let isDragging = false;//记录元素是否在拖拽
let mouseDownTime: number = 0;
let mouseUpTime: number = 0;
const showDrawer = ref(false);//控制抽屉是否显示
const generateUniqueId = () => `${Math.random().toString(36).substr(2, 9)}`;//生成唯一ID
const selectedItem = ref<Item | null>(null);
const showAddPropertyDialog = ref(false);//控制属性添加对话框是否显示

const showlinedialog = ref(false);//控制连线属性对话框是否显示
const lineProperties = ref({//连线的属性
    group: '',
    status: ''
});

const newPropName = ref('');//自定义新建属性名
const newPropValue = ref('');//自定义新建属性值
const resetCanvasState = () => {//重置元素变量
    canvas_items.value = [];
    lines.value = [];
    canvas.value = null;
    canvasContainer.value = null;
    dragItemIndex = null;
    offsetX = 0;
    offsetY = 0;
    drawingLine = false;
    currentLine = null;
};

const handleDragStart = (event: DragEvent) => {//侧边栏开始拖拽
    const Div = event.target as HTMLElement;
    event.dataTransfer?.setData('elementid', Div?.getAttribute('id') ?? '');
};

const handleDrop = (event: DragEvent) => {//元素拖拽至画布上
    event.preventDefault();
    const ElementId = event.dataTransfer?.getData('elementid');
    if (ElementId && canvasContainer.value) {
        const uniqueElemrntId = generateUniqueId();
        const canvasRect = canvasContainer.value.getBoundingClientRect();
        const x = event.clientX - canvasRect.left;
        const y = event.clientY - canvasRect.top;
        canvas_items.value.push({
            id: uniqueElemrntId,
            name: `Element ${ElementId}`,
            x: Math.max(0, Math.min(x, canvasRect.width - 100)),
            y: Math.max(0, Math.min(y, canvasRect.height - 50)),
            isHovered: false,
        });
        // console.log(event.target);
    }
};

const onMouseEnter = (index: number) => {//鼠标进入元素
    canvas_items.value[index].isHovered = true;
};

const onMouseLeave = (index: number) => {//鼠标离开元素
    canvas_items.value[index].isHovered = false;
};

const onItemMouseDown = (index: number, event: MouseEvent) => {//点击元素
    const item = canvas_items.value[index];
    const canvasRect = canvasContainer.value?.getBoundingClientRect();
    if (!canvasRect) return;
    const borderwidth = 15;
    const mouseX = event.clientX - canvasRect.left;
    const mouseY = event.clientY - canvasRect.top;
    selectedItem.value = item;
    //通过点击时间判断是否点击或拖拽
    mouseDownTime = Date.now();
    document.addEventListener('mouseup', onItemMouseUp);
    //鼠标点击元素内部则拖拽元素
    if (mouseX - item.x > borderwidth && item.x + 100 - mouseX > borderwidth && mouseY - item.y > borderwidth && item.y + 50 - mouseY > borderwidth) {
        dragItemIndex = index;
        offsetX = mouseX - item.x;
        offsetY = mouseY - item.y;
        isDragging = true;
        startItemDrag(index);
    } else {
        //点击边框开始连线
        startDrawLine(index);
    }
};

const onItemMouseUp = () => {//判断是否为点击事件
    mouseUpTime = Date.now();
    if (mouseUpTime - mouseDownTime < 100) {//点击事件
        showDrawer.value = true;
    }
    document.removeEventListener('mouseup', onItemMouseUp);
};

const startItemDrag = (index: number) => {//开始拖拽
    isDragging = true;
    const fdragmove = (e: MouseEvent) => ItemDragMove(e, index);
    const fdragend = () => stopItemDrag(fdragmove, fdragend, index);//使用闭包，防止移除函数与添加函数中匿名函数不一致
    document.addEventListener('mousemove', fdragmove);//需要一个函数引用，而不是函数调用的结果
    document.addEventListener('mouseup', fdragend);
}

const ItemDragMove = (event: MouseEvent, index: number) => {//拖拽移动
    if (dragItemIndex !== null && canvasContainer.value && isDragging) {
        const canvasRect = canvasContainer.value.getBoundingClientRect();
        if (!canvasRect) return;
        const x = Math.max(0, Math.min(event.clientX - canvasRect.left - offsetX, canvasRect.width - 100));
        const y = Math.max(0, Math.min(event.clientY - canvasRect.top - offsetY, canvasRect.height - 50));
        const item = canvas_items.value[dragItemIndex];
        item.x = x;
        item.y = y;
        // console.log(`${index}开始拖拽`);
        lines.value.forEach(line => {
            if (line.startElementId === index) {
                line.start.x = item.x + 50;//假设元素中心点
                line.start.y = item.y + 25;
            }
            if (line.endElementId === index) {
                line.end.x = item.x + 50;
                line.end.y = item.y;
            }
        });
        const mycanvas = document.getElementById('myCanvas');
        if (mycanvas instanceof HTMLCanvasElement) {
            const ctx = mycanvas.getContext('2d');
            if (ctx) {
                ctx.clearRect(0, 0, mycanvas.width, mycanvas.height);//清除之前的绘制
                //重新绘制所有已有的连线
                lines.value.forEach(line => {
                    drawLinefunc(line.start.x, line.start.y, line.end.x, line.end.y, 1);
                });
            }
        };
    }
};

const stopItemDrag = (fdragmove: (e: MouseEvent) => void, fdragend: () => void, index: number) => {//拖拽结束
    isDragging = false;
    document.removeEventListener('mousemove', fdragmove);
    document.removeEventListener('mouseup', fdragend);
    dragItemIndex = null;
    // console.log(`${index}拖拽结束`);
};

const startDrawLine = (index: number) => {//开始连线
    const item = canvas_items.value[index];
    const canvasRect = canvasContainer.value?.getBoundingClientRect();
    if (!canvasRect) return;
    const start = {
        x: item.x + 50,
        y: item.y + 25
    };
    currentLine = { start, end: start, startElementId: index, endElementId: null };
    drawingLine = true;
    // console.log(`${index}开始画线`);
    const drawend = () => stopDrawLine(drawend, index);//使用闭包，防止移除函数与添加函数中匿名函数不一致
    document.addEventListener('mousemove', DrawingLineMove);
    document.addEventListener('mouseup', drawend);
};

const DrawingLineMove = (event: MouseEvent) => {//连线鼠标移动过程
    if (drawingLine && currentLine) {
        const canvasRect = canvasContainer.value?.getBoundingClientRect();
        if (!canvasRect) return;
        const x = event.clientX - canvasRect.left;
        const y = event.clientY - canvasRect.top;
        const mycanvas = document.getElementById('myCanvas');
        // const canvas = canvasContainer.value?.querySelector('canvas');
        if (mycanvas instanceof HTMLCanvasElement) {
            const ctx = mycanvas.getContext('2d');
            if (ctx) {
                ctx.clearRect(0, 0, mycanvas.width, mycanvas.height);//清除之前的绘制
                //重新绘制所有已有的连线
                lines.value.forEach(line => {
                    drawLinefunc(line.start.x, line.start.y, line.end.x, line.end.y, 1);
                });
                //绘制当前正在绘制的连线
                drawLinefunc(currentLine.start.x, currentLine.start.y, x, y, 1);
            }
        }
        const end = { x, y };
        currentLine.end = end;
    };
}

const stopDrawLine = (drawend: () => void, index: number) => {//连线结束
    if (!currentLine) return;
    const mycanvas = document.getElementById('myCanvas');
    if (mycanvas instanceof HTMLCanvasElement) {
        const ctx = mycanvas.getContext('2d');
        if (isLineInElement()) {//连线连到了元素上面
            lines.value.push(currentLine);//将当前绘制的线条添加到lines数组中
            currentLine.opRunId = generateUniqueId();
            // 显示连线属性对话框
            showlinedialog.value = true;
            currentLine.group = ''; // 初始化组别
            currentLine.status = 'success'; // 默认操作状态
            console.log(showlinedialog.value);
        }
        if (ctx && currentLine) {
            ctx.clearRect(0, 0, mycanvas.width, mycanvas.height);//清除之前的绘制
            //重新绘制所有已有的连线
            lines.value.forEach(line => {
                drawLinefunc(line.start.x, line.start.y, line.end.x, line.end.y, 1);
            });
        }
    }

    // console.log(`${index}结束画线`);
    drawingLine = false;
    currentLine = null;
    document.removeEventListener('mousemove', DrawingLineMove);
    document.removeEventListener('mouseup', drawend);
};

const drawLinefunc = (startx: number, starty: number, endx: number, endy: number, lineWidth: number) => {//画线
    const canvasRect = canvasContainer.value?.getBoundingClientRect();
    if (!canvasRect) return;
    const headLen = lineWidth * 10;//箭头长度
    const angle = Math.atan2(endy - starty, endx - startx);//箭头角度
    const mycanvas = document.getElementById('myCanvas');
    if (mycanvas instanceof HTMLCanvasElement) {
        const ctx = mycanvas.getContext('2d');
        if (ctx) {
            //绘制线条
            ctx.beginPath();
            ctx.moveTo(startx, starty);
            ctx.lineTo(endx, endy);
            ctx.strokeStyle = 'black';
            ctx.lineWidth = lineWidth;
            ctx.stroke();
            //绘制箭头
            ctx.beginPath();
            ctx.moveTo(endx, endy);
            ctx.lineTo(endx - headLen * Math.cos(angle - Math.PI / 6), endy - headLen * Math.sin(angle - Math.PI / 6));
            ctx.lineTo(endx - headLen * Math.cos(angle + Math.PI / 6), endy - headLen * Math.sin(angle + Math.PI / 6));
            ctx.closePath();
            ctx.fillStyle = 'black';
            ctx.fill();
        }
    }
};

const isLineInElement = () => {//判断连线是否在元素内且无重复连线
    if (currentLine) {
        let index = 0;
        for (const item of canvas_items.value) {//检查线段的结束点是否在某个元素的边界内
            if (currentLine.end.x > item.x && currentLine.end.x < item.x + 100 && currentLine.end.y > item.y && currentLine.end.y < item.y + 50) {
                currentLine.end.x = item.x + 50;
                currentLine.end.y = item.y;
                currentLine.endElementId = index;
                if (currentLine.endElementId === currentLine.startElementId) return false;
                for (const line of lines.value) {//判断是否已存在相同线段
                    if (currentLine.start.x === line.start.x && currentLine.start.y === line.start.y && currentLine.end.x === line.end.x && currentLine.end.y === line.end.y) {
                        console.log("已存在连线");
                        return false;
                    }
                }
                console.log(`连到了${item.name}`);
                return true;//在元素内部并且没有重复线段
            }
            index++;
        }
    }
    return false;
};

const generateXML = () => {//生成XML
    const operationXML = canvas_items.value.map(item => {
        let paramsXML = '';
        if (item.params && Object.keys(item.params).length > 0) {
            paramsXML = `
                <params>
                    ${Object.entries(item.params).map(([key, value]) => `
                        <param>
                            <key>${key}</key>
                            <value>${value}</value>
                        </param>
                    `).join('')}
                </params>
            `;
        }
        return `
            <Operation id="${item.id}">
                <OpName>${item.name.replace("Element ", "")}</OpName>
                <OpDesc>${item.name.replace("Element ", "")}操作的描述</OpDesc>
                <RunApi>127.0.0.1/start/${item.id}</RunApi>
                <StateApi>127.0.0.1/state/${item.id}</StateApi>
                <PositionX>${item.x}</PositionX>
                <PositionY>${item.y}</PositionY>
                ${paramsXML}
            </Operation>
        `;
    }).join('');
    const operationRunXML = canvas_items.value.map(item => {//遍历每个item
        const opRunId = generateUniqueId();//生成唯一的opRunId
        const conditions = lines.value
            .filter(line => line.endElementId !== null && canvas_items.value[line.endElementId]?.id === item.id)
            .map(line => {
                if (line.startElementId === null) return '';//如果startElementId为null，跳过当前行
                const startElement = canvas_items.value[line.startElementId]?.id;
                if (!startElement) return '';//如果startElement无效，则跳过
                return `
            <Condition>
                <DependOpRunId>${startElement}</DependOpRunId>
                <DependState>success</DependState>
            </Condition>
        `;
            }).join('');

        //如果没有条件则 `<StartConditions/>`，否则 `<StartConditions><ConditionGroup>...</ConditionGroup></StartConditions>`
        const startConditionsXML = conditions
            ? `<StartConditions><ConditionGroup>${conditions}</ConditionGroup></StartConditions>`
            : `<StartConditions/>`;

        return `
            <OperationRun opRunId="${opRunId}" opId="${item.id}">
                <Params>
                    ${item.params ? `<Param><Key>x</Key><Value>${item.params.x}</Value></Param>` : ''}
                </Params>
                ${startConditionsXML}
            </OperationRun>
        `;
    }).join('');
    const xmlString = `
<Task>
    <TaskName>任务1</TaskName>
    <TaskDesc>任务1的描述信息</TaskDesc>
    <OperationList>
        ${operationXML}
    </OperationList>
    <OperationRunList>
        ${operationRunXML}
    </OperationRunList>
</Task>
    `;
    // console.log(xmlString);
    downloadXML(xmlString);
};

const downloadXML = (xmlContent: string) => {//下载XML文件
    //Blob是一个表示不可变原始数据的类，这里将传入的xmlContent字符串包装成一个Blob对象，类型为'text/xml'，表示它是一个XML文件
    const blob = new Blob([xmlContent], { type: 'text/xml' });
    //创建一个<a>标签。这个<a>标签的作用是作为下载链接来触发下载操作
    const link = document.createElement('a');
    //通过createObjectURL创建一个指向该Blob对象的URL，这样浏览器就可以识别和下载
    link.href = URL.createObjectURL(blob);
    //设置download属性指定下载文件的默认名称
    link.download = 'task.xml';
    //模拟点击这个<a>标签，触发下载。虽然这个<a>标签没有显示出来，但它仍然会执行点击事件，启动文件的下载
    link.click();
};

const handleFileDrop = (event: DragEvent) => {//处理文件拖拽并解析并在画布上显示
    //浏览器在接收到文件拖拽事件时会自动尝试打开文件，调用preventDefault()防止这种默认行为
    event.preventDefault();
    resetCanvasState();//重置画布元素数据
    //获取拖拽的文件
    const file = event.dataTransfer?.files[0];
    //通过dataTransfer对象访问拖拽的文件。files是一个文件列表，这里获取第一个文件（files[0]）
    if (file && file.type === "text/xml") {//确保文件类型是text/xml（XML文件）
        const reader = new FileReader();
        reader.readAsText(file);//启动文件读取的操作，但文件内容只能在读取完成后通过事件处理函数来获取
        reader.onload = (e) => {
            if (e.target?.result) {
                const xmlContent = e.target.result as string;//e.target.result包含了读取的文件内容
                parseXML(xmlContent);//解析XML
                const mycanvas = document.getElementById('myCanvas');
                if (mycanvas instanceof HTMLCanvasElement) {
                    const ctx = mycanvas.getContext('2d');
                    if (ctx) {
                        ctx.clearRect(0, 0, mycanvas.width, mycanvas.height);//清除之前的绘制
                        //重新绘制所有已有的连线
                        lines.value.forEach(line => {
                            drawLinefunc(line.start.x, line.start.y, line.end.x, line.end.y, 1);
                        });
                    }
                };
            }
        };
    }
    else {
        alert("请拖拽一个有效的XML文件！");
    }
};

const parseXML = (xmlContent: string) => {//解析XML文件
    const parser = new DOMParser();//DOMParser是一个内置的JavaScript对象，用于解析XML或HTML字符串，并将其转换成相应的文档对象模型（DOM）
    //xmlContent是一个包含XML内容的字符串。"application/xml"是一个MIME类型，指定了要解析的内容类型为XML
    //这个方法会解析xmlContent字符串，并返回一个XMLDocument对象，赋值给变量xmlDoc
    const xmlDoc = parser.parseFromString(xmlContent, "application/xml");
    //提取元素（Operation）信息
    const operations = Array.from(xmlDoc.getElementsByTagName("Operation"));//将返回的所有<Operation>元素的集合(NodeList)转换为一个数组
    operations.forEach(op => {
        const id = op.getAttribute('id');//获取id
        const name = op.getElementsByTagName('OpName')[0].textContent || '';//获取该元素中第一个<OpName>标签的文本内容（即操作名称），如果没有找到，则为空
        const x = op.getElementsByTagName('PositionX')[0].textContent || '';
        const y = op.getElementsByTagName('PositionY')[0].textContent || '';
        // const params = op.getElementsByTagName('params')[0].textContent || '';
        //提取 params
        const params = Array.from(op.getElementsByTagName('params')[0]?.getElementsByTagName('param') || []).reduce((acc, curParamNode) => {
            //reduce将包含<param>节点的数组转化为一个对象
            const key = curParamNode.getElementsByTagName('key')[0]?.textContent || '';
            const value = curParamNode.getElementsByTagName('value')[0]?.textContent || '';
            if (key) acc[key] = value;
            return acc;
        }, {} as Record<string, string>);//空对象{}是累积器的初始值。as Record<string, string>是TypeScript的类型断言，表示累积器的类型是一个键值对对象，其中键和值都是字符串类型。
        if (id && name && x && y) {//将一个新的对象添加到canvas_items.value数组，默认（100, 100）
            const posX = parseInt(x);
            const posY = parseInt(y);
            if (Object.keys(params).length === 0) {
                canvas_items.value.push({ id, name, x: posX, y: posY, isHovered: false });
            }
            else {
                canvas_items.value.push({ id, name, x: posX, y: posY, isHovered: false, params: params });
            }
            // canvas_items.value.push({ id, name, x: posX, y: posY, isHovered: false, params: params });
        }
    });
    //提取连线（OperationRun）信息
    const operationRuns = Array.from(xmlDoc.getElementsByTagName("OperationRun"));
    operationRuns.forEach(run => {
        const opId = run.getAttribute('opId');
        const dependsOn = Array.from(run.getElementsByTagName('Condition')).map(cond => {
            //对于每个<Condition>元素，获取<DependOpRunId>标签的内容，即依赖的操作运行ID
            const dependOpRunId = cond.getElementsByTagName('DependOpRunId')[0].textContent;
            return dependOpRunId;//返回将所有依赖的操作运行ID
        });
        dependsOn.forEach(depId => {
            const startElementId = canvas_items.value.findIndex(item => item.id === depId);//查找依赖的操作运行ID(depId)所对应的元素，linestart
            const endElementId = canvas_items.value.findIndex(item => item.id === opId);//查找当前操作运行ID(opId)所对应的元素,lineend

            if (startElementId !== -1 && endElementId !== -1) {
                lines.value.push({
                    start: { x: canvas_items.value[startElementId].x + 50, y: canvas_items.value[startElementId].y + 25 },
                    end: { x: canvas_items.value[endElementId].x + 50, y: canvas_items.value[endElementId].y },
                    startElementId: startElementId,
                    endElementId: endElementId
                });
            }
        });
    });
};

const onDocumentClick = (event: MouseEvent) => {//监听鼠标点击事件，如果点击的地方不是抽屉区域，则关闭抽屉
    const drawerElement = document.querySelector('.drawer');
    if (drawerElement && !drawerElement.contains(event.target as Node)) {
        showDrawer.value = false;
        showAddPropertyDialog.value = false;
    }
    const canvasRect = canvasContainer.value?.getBoundingClientRect();
    if (!canvasRect) return;
    const mouseX = event.clientX - canvasRect.left;
    const mouseY = event.clientY - canvasRect.top;
    let index = 0;
    //遍历每条线检查是否点击到了线上
    for (const line of lines.value) {
        if (isPointOnLine(mouseX, mouseY, line)) {
            drawLinefunc(line.start.x, line.start.y, line.end.x, line.end.y, 2);
            console.log("点击了线段" + lines.value[index].startElementId + "->" + lines.value[index].endElementId);
        }
        index++;
    }
};

//判断点是否在直线上
const isPointOnLine = (px: number, py: number, line: Line) => {
    const tolerance = 5;//容差范围
    const dx = line.end.x - line.start.x;
    const dy = line.end.y - line.start.y;
    const t = ((px - line.start.x) * dx + (py - line.start.y) * dy) / (dx * dx + dy * dy);
    if (t >= 0 && t <= 1) {
        const closestX = line.start.x + t * dx;
        const closestY = line.start.y + t * dy;
        return Math.abs(closestX - px) < tolerance && Math.abs(closestY - py) < tolerance;
    }
    return false;
}

onMounted(() => {//确保canvas的尺寸与canvasContainer一致
    if (canvas.value && canvasContainer.value) {
        const canvasRect = canvasContainer.value.getBoundingClientRect();
        canvas.value.width = canvasRect.width;
        canvas.value.height = canvasRect.height;
    }
    document.addEventListener('mousedown', onDocumentClick);
});

onUnmounted(() => {
    //在组件卸载时移除事件监听器
    document.removeEventListener('mousedown', onDocumentClick);
});

const addProperty = () => {
    if (newPropName.value && newPropValue.value && selectedItem.value) {
        selectedItem.value.params = selectedItem.value.params || {};
        selectedItem.value.params[newPropName.value] = newPropValue.value;
        newPropName.value = '';
        newPropValue.value = '';
        showAddPropertyDialog.value = false;
    } else {
        alert('请输入有效的属性名称和值');
    }
};

</script>
<template>
    <div class="main">
        <!-- 侧边栏 -->
        <div class="sidebar">
            <div class="item" :draggable="true" @dragstart="handleDragStart" id="A">Element A</div>
            <div class="item" :draggable="true" @dragstart="handleDragStart" id="B">Element B</div>
            <div class="item" :draggable="true" @dragstart="handleDragStart" id="C">Element C</div>
            <div class="item" :draggable="true" @dragstart="handleDragStart" id="D">Element D</div>
            <div class="item" :draggable="true" @dragstart="handleDragStart" id="E">Element E</div>
            <div class="item" :draggable="true" @dragstart="handleDragStart" id="F">Element F</div>
            <div class="item" :draggable="true" @dragstart="handleDragStart" id="G">Element G</div>
        </div>

        <!-- 画布区域 -->
        <div class="canvas" @dragover.prevent @drop="handleDrop" ref="canvasContainer">
            <canvas ref="canvas" width="100%" height="100%" id="myCanvas" class="canvas-bg"></canvas>
            <div v-for="(item, index) in canvas_items" :key="item.id" class="canvas-item"
                :style="{ left: item.x + 'px', top: item.y + 'px' }" @mouseenter="onMouseEnter(index)"
                @mouseleave="onMouseLeave(index)" @mousedown="onItemMouseDown(index, $event)">
                {{ item.name }}
            </div>
        </div>

        <!-- 控制区域 -->
        <div class="controls">
            <button @click="generateXML" class="generate-button">生成 XML</button>
            <div class="file-drop-area" @dragover.prevent @drop="handleFileDrop">
                <p>拖拽XML文件到此区域</p>
            </div>
        </div>

        <!-- Drawer 面板 -->
        <div v-if="showDrawer" class="drawer">
            <div class="drawer-header">
                <span class="drawer-title">{{ selectedItem?.name }}</span>
                <span class="drawer-id">ID: {{ selectedItem?.id }}</span>
            </div>
            <div class="drawer-content">
                <div v-if="selectedItem?.params" class="drawer-content-body">
                    <div v-for="(value, key) in selectedItem.params" :key="key" class="item-param">
                        <label :for="`${key}`">{{ key }}: </label>
                        <input v-if="typeof value === 'number'" type="number" v-model="selectedItem.params[key]" />
                        <input v-if="typeof value === 'string'" type="text" v-model="selectedItem.params[key]" />
                        <input v-if="typeof value === 'boolean'" type="checkbox" v-model="selectedItem.params[key]" />
                    </div>
                </div>
                <div class="drawer-footer">
                    <button @click="showAddPropertyDialog = true" class="custom-button">添加属性</button>
                </div>

                <!-- 添加属性对话框 -->
                <div v-if="showAddPropertyDialog" class="dialog">
                    <h3>设置属性</h3>
                    <label for="propName">属性名称:</label>
                    <input type="text" v-model="newPropName" id="propName" placeholder="请输入属性名称" />
                    <label for="propValue">属性值:</label>
                    <input type="text" v-model="newPropValue" id="propValue" placeholder="请输入属性值" />
                    <button @click="addProperty" class="dialog-button">添加</button>
                    <button @click="showAddPropertyDialog = false" class="dialog-button cancel">取消</button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.main {
    display: flex;
    width: 100%;
    height: 100%;
}

.sidebar {
    width: 220px;
    height: 100%;
    background: #f4f6f9;
    padding: 20px;
    border-right: 1px solid #ddd;
    box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
}

.item {
    padding: 12px;
    background: #e0e0e0;
    margin: 8px 0;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.item:hover {
    background: #007BFF;
    color: white;
}

.canvas {
    flex: 1;
    min-height: 100%;
    background: #fafafa;
    border: 1px solid #ccc;
    position: relative;
    width: 100%;
    height: 100%;
}

.canvas-bg {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 0;
}

.canvas-item {
    position: absolute;
    padding: 12px;
    background: gray;
    color: white;
    cursor: pointer;
    width: 100px;
    height: 50px;
    text-align: center;
    border: 2px solid black;
    border-radius: 8px;
    transition: border 0.2s ease, transform 0.3s ease;
    user-select: none;
}

.canvas-item:hover {
    border: 3px dotted #007BFF;
    transform: scale(1.05);
}

.controls {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px;
    background: #f9f9f9;
    border-left: 1px solid #ddd;
}

.generate-button {
    margin-bottom: 20px;
    padding: 12px 20px;
    font-size: 16px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.generate-button:hover {
    background-color: #218838;
}

.file-drop-area {
    width: 100%;
    height: 200px;
    border: 2px dashed #ccc;
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
    font-size: 14px;
    color: #777;
}

.drawer {
    position: absolute;
    top: 0;
    right: 0;
    width: 320px;
    height: 100%;
    background: linear-gradient(135deg, #f0f4f8, #ffffff);
    border-left: 1px solid #ddd;
    padding: 20px;
    box-shadow: -4px 0 10px rgba(0, 0, 0, 0.1);
    border-radius: 10px 0 0 10px;
    transition: transform 0.3s ease;
    z-index: 100;
}

.drawer-header {
    display: flex;
    justify-content: space-between;
    font-size: 1.2em;
    font-weight: bold;
    margin-bottom: 15px;
    color: #333;
}

.drawer-title {
    color: #007BFF;
}

.drawer-id {
    font-size: 1.1em;
    color: #555;
}

.drawer-content {
    margin-top: 10px;
    padding: 20px;
    background-color: #fff;
    border-radius: 10px;
    border: 1px solid #ddd;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    overflow-y: auto;
}

.drawer-content-body {
    max-height: 500px;
    overflow-y: auto;
    padding: 15px;
}

.drawer-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
}

.custom-button {
    padding: 8px 15px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 16px;
    transition: background-color 0.3s ease;
}

.custom-button:hover {
    background-color: #0056b3;
}

.dialog {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: white;
    padding: 20px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    z-index: 10;
}

.dialog input {
    width: 100%;
    padding: 10px;
    margin: 10px 0;
    border: 1px solid #ddd;
    border-radius: 5px;
    font-size: 1em;
}

.dialog button {
    padding: 8px 15px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    margin-top: 10px;
}

.dialog button:hover {
    background-color: #218838;
}

.dialog-button {
    padding: 10px 20px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.dialog-button.cancel {
    background-color: #dc3545;
}

.dialog-button:hover {
    background-color: #218838;
}
</style>