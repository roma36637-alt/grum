import { CITY } from './world.js';

const canvas = document.querySelector('#minimap canvas');
const ctx = canvas.getContext('2d');
const W = canvas.width, H = canvas.height;
const scale = W / (CITY.size * 2);

export function drawMinimap(player, peds, cops, missionDest) {
  ctx.fillStyle = '#132';
  ctx.fillRect(0, 0, W, H);
  // grid
  ctx.strokeStyle = '#1e3a4a';
  for (let i = -CITY.size; i <= CITY.size; i += CITY.block) {
    const p = (i + CITY.size) * scale;
    ctx.beginPath();
    ctx.moveTo(p, 0); ctx.lineTo(p, H);
    ctx.moveTo(0, p); ctx.lineTo(W, p);
    ctx.stroke();
  }
  // peds
  ctx.fillStyle = '#88ff88';
  for (const p of peds) {
    if (!p.alive) continue;
    const x = (p.pos.x + CITY.size) * scale;
    const y = (p.pos.z + CITY.size) * scale;
    ctx.fillRect(x - 1, y - 1, 2, 2);
  }
  // cops
  ctx.fillStyle = '#ff4444';
  for (const c of cops) {
    if (!c.alive) continue;
    const x = (c.pos.x + CITY.size) * scale;
    const y = (c.pos.z + CITY.size) * scale;
    ctx.fillRect(x - 2, y - 2, 4, 4);
  }
  // mission dest
  ctx.fillStyle = '#66ff99';
  if (missionDest) {
    const x = (missionDest.x + CITY.size) * scale;
    const y = (missionDest.z + CITY.size) * scale;
    ctx.beginPath();
    ctx.arc(x, y, 4, 0, Math.PI * 2);
    ctx.fill();
  }
  // player
  const px = (player.pos.x + CITY.size) * scale;
  const py = (player.pos.z + CITY.size) * scale;
  ctx.fillStyle = '#ffff00';
  ctx.beginPath();
  ctx.arc(px, py, 3, 0, Math.PI * 2);
  ctx.fill();
}
