import * as THREE from 'three';
import { CITY, resolveXZ, segmentClear } from './world.js';
import { sfx } from './audio.js';

export class Cop {
  constructor(scene, x, z) {
    this.scene = scene;
    this.pos = new THREE.Vector3(x, 0, z);
    this.yaw = 0;
    this.radius = 0.5;
    this.hp = 70;
    this.alive = true;
    this.shootCd = 0;
    this.walkAnim = 0;

    const body = new THREE.Group();
    const torso = new THREE.Mesh(
      new THREE.BoxGeometry(0.7, 0.9, 0.35),
      new THREE.MeshLambertMaterial({ color: 0x0d47a1 })
    );
    torso.position.y = 1.05;
    body.add(torso);
    const head = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, 0.4, 0.4),
      new THREE.MeshLambertMaterial({ color: 0xffccaa })
    );
    head.position.y = 1.7;
    body.add(head);
    const cap = new THREE.Mesh(
      new THREE.BoxGeometry(0.45, 0.15, 0.45),
      new THREE.MeshLambertMaterial({ color: 0x000000 })
    );
    cap.position.y = 1.97;
    body.add(cap);
    const legL = new THREE.Mesh(
      new THREE.BoxGeometry(0.25, 0.8, 0.25),
      new THREE.MeshLambertMaterial({ color: 0x1a2c4c })
    );
    legL.position.set(-0.18, 0.4, 0);
    const legR = legL.clone();
    legR.position.x = 0.18;
    body.add(legL); body.add(legR);
    this.body = body;
    this.parts = { torso, legL, legR };
    body.position.copy(this.pos);
    scene.add(body);
  }

  update(dt, target) {
    if (!this.alive) {
      this.body.rotation.z = Math.min(Math.PI / 2, this.body.rotation.z + dt * 2);
      return;
    }
    if (!target || !target.alive) return;

    const tgtPos = target.inCar ? target.inCar.pos : target.pos;
    const dir = tgtPos.clone().sub(this.pos);
    dir.y = 0;
    const dist = dir.length();
    dir.normalize();
    this.yaw = Math.atan2(-dir.x, -dir.z);
    // move towards target until within range
    if (dist > 14) {
      const speed = 4.2;
      this.pos.x += dir.x * speed * dt;
      this.pos.z += dir.z * speed * dt;
      resolveXZ(this.pos, this.radius);
    }

    // shoot if LOS & in range
    this.shootCd -= dt;
    const head = new THREE.Vector3(this.pos.x, 1.7, this.pos.z);
    const tgtHead = new THREE.Vector3(tgtPos.x, 1.7, tgtPos.z);
    if (dist < 25 && this.shootCd <= 0 && segmentClear(head, tgtHead)) {
      this.shootCd = 0.5 + Math.random() * 0.4;
      sfx('shoot');
      const hit = Math.random() < 0.45;
      if (hit) {
        target.takeDamage(8);
        sfx('hit');
      }
    }

    this.body.position.copy(this.pos);
    this.body.rotation.y = this.yaw;
    this.walkAnim += dt * 4;
    const s = Math.sin(this.walkAnim);
    this.parts.legL.rotation.x = s * 0.7;
    this.parts.legR.rotation.x = -s * 0.7;
  }

  takeDamage(n) {
    if (!this.alive) return;
    this.hp -= n;
    if (this.hp <= 0) this.alive = false;
  }
}

export class PoliceSystem {
  constructor(scene) {
    this.scene = scene;
    this.cops = [];
    this.wanted = 0;
    this.wantedDecayAt = 0;
    this.spawnCd = 0;
  }

  addWanted(n) {
    this.wanted = Math.min(5, this.wanted + n);
    this.wantedDecayAt = performance.now() / 1000 + 30;
    sfx('siren');
  }

  update(dt, target) {
    // Decay wanted level over time if no crime
    if (this.wanted > 0 && performance.now() / 1000 > this.wantedDecayAt) {
      this.wanted = Math.max(0, this.wanted - 1);
      this.wantedDecayAt = performance.now() / 1000 + 30;
    }

    // Spawn cops proportional to wanted level
    this.spawnCd -= dt;
    const liveCops = this.cops.filter(c => c.alive).length;
    const targetCount = this.wanted === 0 ? 0 : Math.min(10, this.wanted * 2);
    if (liveCops < targetCount && this.spawnCd <= 0) {
      this.spawnCd = 1.5;
      const angle = Math.random() * Math.PI * 2;
      const dist = 40 + Math.random() * 30;
      const px = (target.inCar ? target.inCar.pos.x : target.pos.x) + Math.cos(angle) * dist;
      const pz = (target.inCar ? target.inCar.pos.z : target.pos.z) + Math.sin(angle) * dist;
      this.cops.push(new Cop(this.scene, px, pz));
    }

    for (const c of this.cops) c.update(dt, target);

    // Cull dead cops after a while
    for (const c of this.cops) {
      if (!c.alive) c._deadT = (c._deadT || 0) + dt;
    }
    this.cops = this.cops.filter(c => {
      if (c.alive || (c._deadT || 0) < 15) return true;
      this.scene.remove(c.body);
      return false;
    });
  }
}
