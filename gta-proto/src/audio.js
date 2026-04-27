// Tiny WebAudio SFX helper — no external files.
let ctx = null;
function ensureCtx() {
  if (!ctx) {
    const Ctx = window.AudioContext || window.webkitAudioContext;
    ctx = new Ctx();
  }
  if (ctx.state === 'suspended') ctx.resume();
  return ctx;
}

export function sfx(kind) {
  try {
    const ac = ensureCtx();
    const t = ac.currentTime;
    const o = ac.createOscillator();
    const g = ac.createGain();
    o.connect(g);
    g.connect(ac.destination);
    let f = 440, dur = 0.08, type = 'square', v = 0.05;
    if (kind === 'shoot') { f = 180; dur = 0.07; type = 'sawtooth'; v = 0.08; }
    else if (kind === 'hit') { f = 90; dur = 0.12; type = 'square'; v = 0.09; }
    else if (kind === 'death') { f = 60; dur = 0.4; type = 'sawtooth'; v = 0.1; }
    else if (kind === 'siren') { f = 700; dur = 0.3; type = 'sine'; v = 0.04; }
    else if (kind === 'reload') { f = 320; dur = 0.1; type = 'triangle'; v = 0.05; }
    else if (kind === 'cash') { f = 880; dur = 0.15; type = 'triangle'; v = 0.07; }
    else if (kind === 'engine') { f = 110; dur = 0.12; type = 'sawtooth'; v = 0.04; }
    o.type = type;
    o.frequency.setValueAtTime(f, t);
    o.frequency.exponentialRampToValueAtTime(Math.max(40, f / 3), t + dur);
    g.gain.setValueAtTime(v, t);
    g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    o.start(t);
    o.stop(t + dur + 0.02);
  } catch (_) {}
}
