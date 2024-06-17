<template>
  <div id="whiteboard">
    <canvas
      ref="canvas"
      @mousedown="startDrawing"
      @mousemove="draw"
      @mouseup="stopDrawing"
      style="z-index: 1"
    >
    </canvas>
<!--    <canvas ref="canvas2" style="z-index: 2;"></canvas>/-->
    <div id="designTool">
      <button @click="buttonErase">모두 지우기</button>
      <br>
      <label for="drawing-line-width">Line width : </label>
      <span class="info">{{ lineWidth }}</span>
      <input type="range" :value="lineWidth" min="0" max="150" id="drawing-line-width" ref="drawingLineWidthEl" @change="changLineWidth"><br>

      <label for="drawing-color">Line color : </label>
      <span class="info">{{ color }}</span>
      <input type="color" :value="color" id="drawing-color" ref="drawingColorEl" @change="changeColor"><br>


      <button type="button" id="drawing-rect" ref="drawingRectEl" @click="addRect">사각형 추가</button><br>
      <button type="button" id="clickObject" @click="clickObject">객체 선택</button><br>

    </div>
  </div>
</template>

<script>
export default {
  name: "Whiteboard",
  props: {
    classCode: {
      type: String,
      required: true,
    },
    sender: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      drawing: false,
      context: null,
      lastX: 0,
      lastY: 0,
      scaleFactorX: 1,
      scaleFactorY: 1,
      // 화면 속성 변수
      width: 1850,
      height: 837,
      mouseX: 0,
      mouseY: 0,
      rect:{},
      // 두 수의 차이
      xMinusX:0,
      yMinusY:0,
      // 속성 변수
      strokeStyle: '',
      lineWidth: 10,
      color: '#ffffff',
      lineCap: 'round',
    };
  },


  mounted() {
    this.initCanvas();
    this.$root.$bus.on("event", this.handleIncomingDrawing);
    // window.addEventListener("resize", this.resizeCanvas);
  },
  beforeUnmount() {
    this.$root.$bus.off("event", this.handleIncomingDrawing);
    // window.removeEventListener("resize", this.resizeCanvas);
  },

  computed:{

  },
  methods: {
    // 캔버스 세팅
    initCanvas() {

      const canvas = this.$refs.canvas;
      this.context = canvas.getContext("2d");
      this.rect = this.$refs.canvas.getBoundingClientRect();

      canvas.width = this.width;
      canvas.height = this.height;

      this.context.strokeStyle = this.color;
      this.context.lineWidth = this.lineWidth;
      this.context.lineCap = this.lineCap;

      console.log("initCanvas", this.rect);
    },


    // 그리기 시작 함수
    startDrawing(e) {
      this.drawing = true;
      // mouseDown 이벤트 발생시 현재 좌표값 업데이트

      this.lastX = e.clientX - this.rect.x;
      this.lastY = e.clientY - this.rect.y;

      console.log("offset : ",this.rect , this.lastY);

    },

    // 선 굵기 변경
    changLineWidth(e) {
      this.lineWidth = e.target.value;

      console.log("선 굵기 : ",this.lineWidth);

    },

    // 선 색상 변경
    changeColor(e){
      this.color = e.target.value;
    },

    // 그리기 함수
    draw(e) {
      if (!this.drawing) return;

      // 현재 좌표값
      const newX = e.clientX - this.rect.left;
      const newY = e.clientY - this.rect.top;
      // 좌표 업데이트
      const prevX = this.lastX;
      const prevY = this.lastY;

      // 그리기 시작
      this.context.stroke();
      // 도형 그리기 시작 + 도형 그릴 시작점 지정. 도형 쪽으로 옮기기
      this.context.beginPath();
      this.context.moveTo(this.lastX, this.lastY);
      // 선이 끝나는 좌표 설정
      this.context.lineTo(newX, newY);
      // 선 굵기 설정 = 현재 선택된 선 굵기
      this.context.lineWidth = this.lineWidth;
      // 선 색상 설정 = 현재 선택된 선 색상
      this.context.strokeStyle = this.color;
      // 도형 그릴 시작점 지정. 도형 쪽으로 옮기기

      this.lastX = newX;
      this.lastY = newY;

      console.log("draw : ", this.lastX, this.lastY);

      // 메시지 전송
      const message = JSON.stringify({
        type: "DRAW",
        sender: this.sender,
        data: {
          x: this.lastX,
          y: this.lastY,
          prevX,
          prevY,
          color: this.color,
          lineWidth: this.lineWidth,
        },
      });
      if (this.$root.$socket && this.$root.$socket.connected) {
        this.$root.$socket.publish({
          destination: `/pub/update/${this.classCode}`,
          body: message,
        });
      }
    },
    // 그리기 스탑
    stopDrawing() {
      this.drawing = false;
    },
    // 지우개 버튼
    buttonErase() {
      this.context.clearRect(0, 0, this.$refs.canvas.width, this.$refs.canvas.height);
    },

    // 메시지 수신 처리 함수
    handleIncomingDrawing(message) {
      const { type, data } = message;
      if (type === "DRAW") {
        const { x, y, prevX, prevY, color, lineWidth } = data;
        console.log(`보낸 메세지 (${prevX}, ${prevY}) to (${x}, ${y}) & (${color}) & (${lineWidth})`);
      }
    },
  },
};
</script>

<style scoped>
#whiteboard {
  position: relative;
  background: #043e1a;
}
canvas {
  display: block;
}
#designTool {
  position: absolute;
  top: 10px;
  left: 10px;
}
</style>
