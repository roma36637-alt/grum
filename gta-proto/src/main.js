import * as THREE from 'three';
import { input, initInput, consume, clearFrame } from './input.js';
import { buildWorld, CITY, segmentClear, shopZone, missionStart, missionDest } from './world.js';
import { Player } from './player.js';
import { Car, playerCarInputs } from './car.js';
import { spawnPeds } from './npc.js';
import { PoliceSystem } from './police.js';
import { updateHud, setPrompt, setBanner, tickPrompt } from './hud.js';
import { drawMinimap } from './minimap.js';
import { sfx } from './audio.js';

// --- Renderer / scene / camera
const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.5));
renderer.setSize(window.innerWidth, window.innerHeight);
document.getElementById('app').appendChild(renderer.domElement);
window.addEventListener('resize', () => {
  renderer.setSize(window.innerWidth, window.innerHeight);
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
});

const scene = new THREE.Scene();
const camera = new THREE.PerspectiveCamera(70, window.innerWidth / window.innerHeight, 0.1, 600);

initInput(renderer.domElement);
buildWorld(scene);

// --- Entities
const player = new Player(scene);
const peds = spawnPeds(scene, 60);

const cars = [];
// spawn parked cars along streets
const parkedColors = [0xff2a2a, 0x2a78ff, 0x55aa55, 0xffaa00, 0x888888, 0x111111, 0xff69b4];
for (let i = 0; i < 30; i++) {
  const x = (Math.random() - 0.5) * CITY.size * 1.8;
  const z = (Math.random() - 0.5) * CITY.size * 1.8;
  const c = new Car(scene, x, z, parkedColors[i % parkedColors.length]);
  c.yaw = Math.random() * Math.PI * 2;
  cars.push(c);
}
// One car near the player
cars.push(new Car(scene, 5, 5, 0xffd54f));

const police = new PoliceSystem(scene);

// --- Mission
let missionPhase = 'pickup'; // pickup -> deliver -> done
let missionStartedAt = 0;

// --- Bullets: pure visual tracers (raycast instant damage)
const tracers = [];
function addTracer(a, b, color = 0xffee66) {
  const geo = new THREE.BufferGeometry().setFromPoints([a.clone(), b.clone()]);
  const mat = new THREE.LineBasicMaterial({ color });
  const line = new THREE.Line(geo, mat);
  scene.add(line);
  tracers.push({ line, life: 0.08 });
}

function raycastHit(origin, dir, maxDist) {
  // Check peds, cops, cars, buildings — nearest hit in XZ roughly
  let bestT = maxDist;
  let bestHit = null;
  const step = 0.25;
  // Buildings (use segment clear against colliders – find first blocked distance)
  const ends = origin.clone().add(dir.clone().multiplyScalar(maxDist));
  // Simple sampling for entities
  const entities = [
    ...peds.filter(p => p.alive).map(p => ({ obj: p, kind: 'ped', pos: p.pos, r: 0.6, h: 2 })),
    ...police.cops.filter(c => c.alive).map(c => ({ obj: c, kind: 'cop', pos: c.pos, r: 0.6, h: 2 })),
    ...cars.filter(c => c.alive && c !== player.inCar).map(c => ({ obj: c, kind: 'car', pos: c.pos, r: 2.2, h: 1.8 })),
  ];
  for (const e of entities) {
    // Ray-sphere approximation in 3D
    const oc = new THREE.Vector3().subVectors(origin, new THREE.Vector3(e.pos.x, 1.0, e.pos.z));
    const a = dir.dot(dir);
    const b = 2 * oc.dot(dir);
    const c = oc.dot(oc) - e.r * e.r;
    const disc = b * b - 4 * a * c;
    if (disc >= 0) {
      const t = (-b - Math.sqrt(disc)) / (2 * a);
      if (t > 0.5 && t < bestT) {
        // also make sure building doesn't block
        const p = origin.clone().add(dir.clone().multiplyScalar(t));
        if (segmentClear(origin, p)) {
          bestT = t;
          bestHit = { entity: e, point: p };
        }
      }
    }
  }
  // If nothing hit, truncate at first building obstruction by sampling
  if (!bestHit) {
    for (let t = 1; t < maxDist; t += step * 4) {
      const p = origin.clone().add(dir.clone().multiplyScalar(t));
      if (!segmentClear(origin, p)) {
        bestT = t;
        break;
      }
    }
  }
  return { hit: bestHit, end: origin.clone().add(dir.clone().multiplyScalar(bestT)) };
}

function doPlayerShoot() {
  if (!player.hasGun) return;
  if (player.reloadTimer > 0) return;
  if (player.clip <= 0) {
    setPrompt('Нет патронов — нажми R', 0.8);
    return;
  }
  if (player.shootCd > 0) return;
  player.shootCd = 0.12;
  player.clip--;
  sfx('shoot');
  const { origin, dir } = player.shootRay();
  const { hit, end } = raycastHit(origin, dir, 80);
  addTracer(origin, end);
  if (hit) {
    sfx('hit');
    if (hit.entity.kind === 'ped') {
      hit.entity.obj.takeDamage(50);
      if (!hit.entity.obj.alive) {
        police.addWanted(1);
      }
    } else if (hit.entity.kind === 'cop') {
      hit.entity.obj.takeDamage(35);
      if (!hit.entity.obj.alive) {
        police.addWanted(1);
        player.money += 100;
      }
    } else if (hit.entity.kind === 'car') {
      hit.entity.obj.hp -= 10;
    }
  }
}

function tryReload() {
  if (!player.hasGun) return;
  if (player.reloadTimer > 0) return;
  if (player.clip >= player.clipSize) return;
  if (player.ammo <= 0) return;
  player.reloadTimer = 1.2;
  sfx('reload');
  setPrompt('Перезарядка...', 1.2);
}

function nearestDrivableCar() {
  let best = null;
  let bestD = 3.5;
  for (const c of cars) {
    if (c === player.inCar || !c.alive) continue;
    const d = Math.hypot(c.pos.x - player.pos.x, c.pos.z - player.pos.z);
    if (d < bestD) { bestD = d; best = c; }
  }
  return best;
}

function handleEnterExit() {
  if (player.inCar) {
    // exit
    const car = player.inCar;
    player.inCar = null;
    player.pos.set(car.pos.x + Math.cos(car.yaw) * 2.5, 0, car.pos.z - Math.sin(car.yaw) * 2.5);
    car.driver = null;
    return;
  }
  const c = nearestDrivableCar();
  if (c) {
    player.inCar = c;
    c.driver = player;
    // stealing counts as minor crime
    police.addWanted(0); // no stars for stealing in this prototype; keep low-key
    sfx('engine');
  }
}

function handleInteract() {
  // Weapon shop
  const ds = Math.hypot(player.pos.x - shopZone.x, player.pos.z - shopZone.z);
  if (ds < 4 && !player.inCar) {
    // buy 60 rounds for $100, or a pistol for $250 if not owned
    if (!player.hasGun) {
      if (player.money >= 250) {
        player.money -= 250;
        player.hasGun = true;
        player.ammo = 30;
        player.clip = player.clipSize;
        sfx('cash');
        setBanner('Куплен пистолет (+30 патронов). ЛКМ — стрелять.', 2.5);
      } else {
        setPrompt('Нужно $250 на пистолет', 1.2);
      }
    } else {
      if (player.money >= 100) {
        player.money -= 100;
        player.ammo += 60;
        sfx('cash');
        setBanner('+60 патронов', 1.4);
      } else {
        setPrompt('Нужно $100 за 60 патронов', 1.2);
      }
    }
    return;
  }
  // Mission pickup
  const dm = Math.hypot(player.pos.x - missionStart.x, player.pos.z - missionStart.z);
  if (missionPhase === 'pickup' && dm < missionStart.r + 1) {
    missionPhase = 'deliver';
    missionStartedAt = performance.now() / 1000;
    setBanner('Миссия начата: довези груз до зелёного маркера', 3);
  }
}

// --- Main loop
const startScreen = document.getElementById('start');
document.getElementById('startBtn').onclick = () => {
  startScreen.style.display = 'none';
  renderer.domElement.requestPointerLock();
};

let last = performance.now();
function loop() {
  const now = performance.now();
  const dt = Math.min(0.05, (now - last) / 1000);
  last = now;

  // Input handling
  if (consume('KeyF')) handleEnterExit();
  if (consume('KeyE')) handleInteract();
  if (consume('KeyR')) tryReload();
  if (consume('KeyP')) player.respawn();

  // Player / car
  if (player.inCar) {
    player.inCar.update(dt, playerCarInputs());
    // Car running over NPCs / cops
    for (const p of peds) {
      if (!p.alive) continue;
      if (Math.hypot(p.pos.x - player.inCar.pos.x, p.pos.z - player.inCar.pos.z) < 2.2 && Math.abs(player.inCar.speed) > 4) {
        p.takeDamage(100);
        if (!p.alive) police.addWanted(1);
      }
    }
    for (const c of police.cops) {
      if (!c.alive) continue;
      if (Math.hypot(c.pos.x - player.inCar.pos.x, c.pos.z - player.inCar.pos.z) < 2.2 && Math.abs(player.inCar.speed) > 4) {
        c.takeDamage(100);
        if (!c.alive) police.addWanted(2);
      }
    }
  } else {
    player.update(dt);
    // Shooting
    if (input.mouseDown) doPlayerShoot();
  }

  // NPCs
  for (const p of peds) p.update(dt);
  police.update(dt, player);

  // Other parked cars idle (do nothing)

  // Tracers fade
  for (let i = tracers.length - 1; i >= 0; i--) {
    tracers[i].life -= dt;
    if (tracers[i].life <= 0) {
      scene.remove(tracers[i].line);
      tracers[i].line.geometry.dispose();
      tracers[i].line.material.dispose();
      tracers.splice(i, 1);
    }
  }

  // Camera follow (third person)
  const camTarget = player.inCar ? player.inCar.pos : player.pos;
  const camHeight = player.inCar ? 4.0 : 2.2;
  const camDist = player.inCar ? 9.0 : 4.5;
  const offsetX = Math.sin(input.yaw) * camDist;
  const offsetZ = Math.cos(input.yaw) * camDist;
  const desired = new THREE.Vector3(camTarget.x + offsetX, camHeight - input.pitch * 2, camTarget.z + offsetZ);
  camera.position.lerp(desired, 0.2);
  camera.lookAt(camTarget.x, camTarget.y + 1.5, camTarget.z);

  // Mission check
  let missionText = '—';
  if (missionPhase === 'pickup') {
    missionText = 'Подойди к жёлтому маркеру и нажми E';
  } else if (missionPhase === 'deliver') {
    missionText = 'Довези груз до зелёного маркера';
    const d = Math.hypot(player.pos.x - missionDest.x, player.pos.z - missionDest.z);
    if (d < missionDest.r + 2) {
      missionPhase = 'done';
      const time = (performance.now() / 1000 - missionStartedAt).toFixed(1);
      const reward = 800;
      player.money += reward;
      setBanner(`Миссия выполнена за ${time}с. +$${reward}`, 4);
      sfx('cash');
    }
  } else {
    missionText = 'Выполнена. (Перезапусти страницу для повтора)';
  }

  updateHud({
    hp: player.hp,
    ammo: player.ammo,
    clip: player.clip,
    money: player.money,
    stars: police.wanted,
    mission: missionText,
    hasGun: player.hasGun,
  });
  tickPrompt(dt);

  // Prompts for context
  const dShop = Math.hypot(player.pos.x - shopZone.x, player.pos.z - shopZone.z);
  if (dShop < 4 && !player.inCar) setPrompt('E — оружейный магазин', 0.3);
  else if (missionPhase === 'pickup' && Math.hypot(player.pos.x - missionStart.x, player.pos.z - missionStart.z) < 3) {
    setPrompt('E — начать миссию', 0.3);
  } else if (!player.inCar && nearestDrivableCar()) {
    setPrompt('F — сесть в машину', 0.3);
  } else if (player.inCar) {
    setPrompt('F — выйти из машины', 0.3);
  }

  drawMinimap(player, peds, police.cops, missionDest);

  // Death / respawn
  if (!player.alive) {
    setBanner('Ты мёртв. Возрождение через 2с...', 2);
    setTimeout(() => {
      if (!player.alive) {
        player.respawn();
        player.money = Math.max(0, player.money - 200);
        police.wanted = 0;
      }
    }, 2000);
  }

  renderer.render(scene, camera);
  clearFrame();
  requestAnimationFrame(loop);
}
requestAnimationFrame(loop);
