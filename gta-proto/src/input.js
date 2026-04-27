export const input = {
  keys: new Set(),
  mouseDown: false,
  mouseDownR: false,
  yaw: 0,
  pitch: -0.15,
  pressed: new Set(),
  pointerLocked: false,
};

export function initInput(canvas) {
  window.addEventListener('keydown', (e) => {
    const k = e.code;
    if (!input.keys.has(k)) input.pressed.add(k);
    input.keys.add(k);
    if (['ArrowUp','ArrowDown','ArrowLeft','ArrowRight','Space'].includes(k)) e.preventDefault();
  });
  window.addEventListener('keyup', (e) => input.keys.delete(e.code));

  canvas.addEventListener('mousedown', (e) => {
    if (e.button === 0) input.mouseDown = true;
    if (e.button === 2) input.mouseDownR = true;
    if (!input.pointerLocked) canvas.requestPointerLock();
  });
  canvas.addEventListener('contextmenu', (e) => e.preventDefault());
  window.addEventListener('mouseup', (e) => {
    if (e.button === 0) input.mouseDown = false;
    if (e.button === 2) input.mouseDownR = false;
  });

  document.addEventListener('pointerlockchange', () => {
    input.pointerLocked = document.pointerLockElement === canvas;
  });
  window.addEventListener('mousemove', (e) => {
    if (!input.pointerLocked) return;
    input.yaw -= e.movementX * 0.0025;
    input.pitch -= e.movementY * 0.0025;
    input.pitch = Math.max(-1.2, Math.min(0.6, input.pitch));
  });
}

export function consume(code) {
  if (input.pressed.has(code)) {
    input.pressed.delete(code);
    return true;
  }
  return false;
}

export function clearFrame() {
  input.pressed.clear();
}
