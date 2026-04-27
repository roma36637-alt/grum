import * as THREE from 'three';

export const CITY = {
  size: 200,          // half-extent of playable area
  block: 20,          // size of a city block
  road: 6,            // road width
};

export const colliders = []; // axis-aligned boxes: {min:Vector3, max:Vector3}

function addBuilding(scene, x, z, w, d, h, color) {
  const geo = new THREE.BoxGeometry(w, h, d);
  const mat = new THREE.MeshLambertMaterial({ color });
  const m = new THREE.Mesh(geo, mat);
  m.position.set(x, h / 2, z);
  m.castShadow = false;
  m.receiveShadow = true;
  scene.add(m);
  const min = new THREE.Vector3(x - w / 2, 0, z - d / 2);
  const max = new THREE.Vector3(x + w / 2, h, z + d / 2);
  colliders.push({ min, max, mesh: m });

  // Lit windows strip (cheap look)
  const wGeo = new THREE.BoxGeometry(w * 0.9, h * 0.6, 0.1);
  const wMat = new THREE.MeshBasicMaterial({ color: 0xfff1b3 });
  for (const side of [-1, 1]) {
    const wm = new THREE.Mesh(wGeo, wMat);
    wm.position.set(x, h * 0.55, z + (d / 2 + 0.06) * side);
    scene.add(wm);
  }
  return m;
}

export const shopZone = { x: 0, z: 0, r: 4 };
export const missionStart = { x: 0, z: 0, r: 3 };
export const missionDest = { x: 0, z: 0, r: 4 };

export function buildWorld(scene) {
  // Sky
  scene.background = new THREE.Color(0x8bbcff);
  scene.fog = new THREE.Fog(0x8bbcff, 60, 380);

  // Ground (grass surround + asphalt inner)
  const groundGeo = new THREE.PlaneGeometry(CITY.size * 4, CITY.size * 4);
  const groundMat = new THREE.MeshLambertMaterial({ color: 0x3a6b3a });
  const ground = new THREE.Mesh(groundGeo, groundMat);
  ground.rotation.x = -Math.PI / 2;
  ground.receiveShadow = true;
  scene.add(ground);

  // Asphalt square (city core)
  const asphaltGeo = new THREE.PlaneGeometry(CITY.size * 2, CITY.size * 2);
  const asphaltMat = new THREE.MeshLambertMaterial({ color: 0x3a3a3a });
  const asphalt = new THREE.Mesh(asphaltGeo, asphaltMat);
  asphalt.rotation.x = -Math.PI / 2;
  asphalt.position.y = 0.01;
  scene.add(asphalt);

  // Road markings: white stripes every block
  const stripeMat = new THREE.MeshBasicMaterial({ color: 0xffffff });
  for (let x = -CITY.size; x <= CITY.size; x += CITY.block) {
    const s = new THREE.Mesh(new THREE.PlaneGeometry(0.3, CITY.size * 2), stripeMat);
    s.rotation.x = -Math.PI / 2;
    s.position.set(x, 0.02, 0);
    scene.add(s);
  }
  for (let z = -CITY.size; z <= CITY.size; z += CITY.block) {
    const s = new THREE.Mesh(new THREE.PlaneGeometry(CITY.size * 2, 0.3), stripeMat);
    s.rotation.x = -Math.PI / 2;
    s.position.set(0, 0.02, z);
    scene.add(s);
  }

  // Sidewalks (slightly raised light grey blocks) and buildings inside grid cells
  const rng = mulberry32(12345);
  const colors = [0x94a0b2, 0x6a7b8a, 0xc2b280, 0x8a6d5b, 0x577590, 0x43a047, 0xb08968, 0x6b5b95];
  for (let gx = -CITY.size + CITY.block / 2; gx < CITY.size; gx += CITY.block) {
    for (let gz = -CITY.size + CITY.block / 2; gz < CITY.size; gz += CITY.block) {
      const inner = CITY.block - CITY.road;
      // sidewalk
      const swGeo = new THREE.BoxGeometry(inner, 0.2, inner);
      const swMat = new THREE.MeshLambertMaterial({ color: 0xb7b7b7 });
      const sw = new THREE.Mesh(swGeo, swMat);
      sw.position.set(gx, 0.1, gz);
      scene.add(sw);

      // 1-3 buildings per block
      const nBuild = 1 + Math.floor(rng() * 2.5);
      for (let i = 0; i < nBuild; i++) {
        const bw = 3 + rng() * (inner - 6);
        const bd = 3 + rng() * (inner - 6);
        const bh = 6 + rng() * 28;
        const bx = gx + (rng() - 0.5) * (inner - bw);
        const bz = gz + (rng() - 0.5) * (inner - bd);
        const c = colors[Math.floor(rng() * colors.length)];
        addBuilding(scene, bx, bz, bw, bd, bh, c);
      }
    }
  }

  // Weapon shop — replace a central block with a distinctive red building
  const sx = CITY.block, sz = 0;
  addBuilding(scene, sx, sz, 8, 8, 8, 0xff5a5f);
  const neon = new THREE.Mesh(
    new THREE.BoxGeometry(6, 0.6, 0.2),
    new THREE.MeshBasicMaterial({ color: 0xffea00 })
  );
  neon.position.set(sx, 5.5, sz - 4.2);
  scene.add(neon);
  shopZone.x = sx;
  shopZone.z = sz - 6;

  // Mission marker cylinders
  const msStart = new THREE.Mesh(
    new THREE.CylinderGeometry(1.4, 1.4, 0.1, 24),
    new THREE.MeshBasicMaterial({ color: 0xffd54f, transparent: true, opacity: 0.8 })
  );
  msStart.position.set(-CITY.block, 0.06, -CITY.block);
  scene.add(msStart);
  missionStart.x = -CITY.block;
  missionStart.z = -CITY.block;

  const msDest = new THREE.Mesh(
    new THREE.CylinderGeometry(2, 2, 0.1, 24),
    new THREE.MeshBasicMaterial({ color: 0x66ff99, transparent: true, opacity: 0.8 })
  );
  msDest.position.set(CITY.block * 4, 0.06, CITY.block * 3);
  scene.add(msDest);
  missionDest.x = CITY.block * 4;
  missionDest.z = CITY.block * 3;

  // Lighting
  const sun = new THREE.DirectionalLight(0xfff7e0, 1.0);
  sun.position.set(60, 120, 40);
  scene.add(sun);
  scene.add(new THREE.HemisphereLight(0xbbdfff, 0x334433, 0.6));

  return { missionStartMesh: msStart, missionDestMesh: msDest };
}

// deterministic rng
function mulberry32(a) {
  return function () {
    let t = (a += 0x6d2b79f5);
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

// Horizontal AABB resolution (sliding) for a circle of radius r at position p.
// p is a Vector3 (y stays untouched). Mutates p.
export function resolveXZ(p, r) {
  for (const c of colliders) {
    const cx = Math.max(c.min.x, Math.min(p.x, c.max.x));
    const cz = Math.max(c.min.z, Math.min(p.z, c.max.z));
    const dx = p.x - cx;
    const dz = p.z - cz;
    const d2 = dx * dx + dz * dz;
    if (d2 < r * r && d2 > 1e-8) {
      const d = Math.sqrt(d2);
      const push = (r - d);
      p.x += (dx / d) * push;
      p.z += (dz / d) * push;
    } else if (d2 === 0) {
      // inside: push out on smaller axis
      const leftD = p.x - c.min.x;
      const rightD = c.max.x - p.x;
      const frontD = p.z - c.min.z;
      const backD = c.max.z - p.z;
      const minD = Math.min(leftD, rightD, frontD, backD);
      if (minD === leftD) p.x = c.min.x - r;
      else if (minD === rightD) p.x = c.max.x + r;
      else if (minD === frontD) p.z = c.min.z - r;
      else p.z = c.max.z + r;
    }
  }
  // Keep inside world bounds
  const lim = CITY.size - 1;
  p.x = Math.max(-lim, Math.min(lim, p.x));
  p.z = Math.max(-lim, Math.min(lim, p.z));
}

// Returns true if a segment from a to b (XZ plane) is clear of building colliders.
export function segmentClear(a, b) {
  for (const c of colliders) {
    if (rayAabbXZ(a, b, c.min, c.max)) return false;
  }
  return true;
}

function rayAabbXZ(a, b, min, max) {
  const dx = b.x - a.x;
  const dz = b.z - a.z;
  let tmin = 0, tmax = 1;
  if (Math.abs(dx) < 1e-6) {
    if (a.x < min.x || a.x > max.x) return false;
  } else {
    const tx1 = (min.x - a.x) / dx;
    const tx2 = (max.x - a.x) / dx;
    tmin = Math.max(tmin, Math.min(tx1, tx2));
    tmax = Math.min(tmax, Math.max(tx1, tx2));
  }
  if (Math.abs(dz) < 1e-6) {
    if (a.z < min.z || a.z > max.z) return false;
  } else {
    const tz1 = (min.z - a.z) / dz;
    const tz2 = (max.z - a.z) / dz;
    tmin = Math.max(tmin, Math.min(tz1, tz2));
    tmax = Math.min(tmax, Math.max(tz1, tz2));
  }
  return tmax >= tmin;
}
