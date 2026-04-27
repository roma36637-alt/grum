import * as THREE from 'three';
import { CITY, resolveXZ, segmentClear } from './world.js';

const PED_COLORS = [0xe57373, 0x81c784, 0x64b5f6, 0xffd54f, 0xba68c8, 0x4db6ac, 0xa1887f, 0xf06292];

export class Pedestrian {
  constructor(scene, x, z) {
    this.scene = scene;
    this.pos = new THREE.Vector3(x, 0, z);
    this.vel = new THREE.Vector3();
    this.yaw = Math.random() * Math.PI * 2;
    this.target = this._pickTarget();
    this.radius = 0.45;
    this.hp = 30;
    this.alive = true;
    this.deadTimer = 0;
    this.walkAnim = 0;

    const body = new THREE.Group();
    const torso = new THREE.Mesh(
      new THREE.BoxGeometry(0.6, 0.9, 0.3),
      new THREE.MeshLambertMaterial({ color: PED_COLORS[Math.floor(Math.random() * PED_COLORS.length)] })
    );
    torso.position.y = 1.0;
    body.add(torso);
    const head = new THREE.Mesh(
      new THREE.BoxGeometry(0.36, 0.36, 0.36),
      new THREE.MeshLambertMaterial({ color: 0xffccaa })
    );
    head.position.y = 1.6;
    body.add(head);
    const legL = new THREE.Mesh(
      new THREE.BoxGeometry(0.22, 0.75, 0.22),
      new THREE.MeshLambertMaterial({ color: 0x333333 })
    );
    legL.position.set(-0.16, 0.37, 0);
    const legR = legL.clone();
    legR.position.x = 0.16;
    body.add(legL); body.add(legR);
    this.body = body;
    this.parts = { torso, legL, legR };
    this.body.position.copy(this.pos);
    scene.add(body);
  }

  _pickTarget() {
    return new THREE.Vector3(
      (Math.random() - 0.5) * CITY.size * 1.6,
      0,
      (Math.random() - 0.5) * CITY.size * 1.6
    );
  }

  update(dt) {
    if (!this.alive) {
      this.deadTimer += dt;
      // ragdoll: tip over
      this.body.rotation.z = Math.min(Math.PI / 2, this.body.rotation.z + dt * 2);
      return;
    }
    const dir = this.target.clone().sub(this.pos);
    dir.y = 0;
    const d = dir.length();
    if (d < 1.2) this.target = this._pickTarget();
    dir.normalize();
    const speed = 2.2;
    this.pos.x += dir.x * speed * dt;
    this.pos.z += dir.z * speed * dt;
    resolveXZ(this.pos, this.radius);
    this.yaw = Math.atan2(-dir.x, -dir.z);
    this.body.position.copy(this.pos);
    this.body.rotation.y = this.yaw;
    this.walkAnim += dt * speed;
    const s = Math.sin(this.walkAnim * 2);
    this.parts.legL.rotation.x = s * 0.6;
    this.parts.legR.rotation.x = -s * 0.6;
  }

  takeDamage(n) {
    if (!this.alive) return;
    this.hp -= n;
    if (this.hp <= 0) {
      this.alive = false;
    }
  }

  dispose() {
    this.scene.remove(this.body);
  }
}

export function spawnPeds(scene, count) {
  const peds = [];
  for (let i = 0; i < count; i++) {
    const x = (Math.random() - 0.5) * CITY.size * 1.7;
    const z = (Math.random() - 0.5) * CITY.size * 1.7;
    peds.push(new Pedestrian(scene, x, z));
  }
  return peds;
}
