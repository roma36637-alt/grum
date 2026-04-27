import * as THREE from 'three';
import { resolveXZ } from './world.js';
import { input } from './input.js';

export class Car {
  constructor(scene, x, z, color = 0xff2a2a, ai = false) {
    this.scene = scene;
    this.pos = new THREE.Vector3(x, 0, z);
    this.yaw = 0;
    this.speed = 0;       // forward m/s
    this.maxSpeed = 30;
    this.driver = null;   // Player or AI
    this.ai = ai;
    this.radius = 2.1;
    this.hp = 200;
    this.alive = true;

    const body = new THREE.Group();
    const chassis = new THREE.Mesh(
      new THREE.BoxGeometry(2.0, 0.9, 4.2),
      new THREE.MeshLambertMaterial({ color })
    );
    chassis.position.y = 0.9;
    body.add(chassis);
    const cabin = new THREE.Mesh(
      new THREE.BoxGeometry(1.7, 0.7, 2.0),
      new THREE.MeshLambertMaterial({ color: 0x222233 })
    );
    cabin.position.y = 1.6;
    cabin.position.z = -0.1;
    body.add(cabin);
    // wheels
    const wheelGeo = new THREE.CylinderGeometry(0.4, 0.4, 0.35, 16);
    const wheelMat = new THREE.MeshLambertMaterial({ color: 0x111111 });
    const positions = [
      [-1.05, 0.4, 1.4],
      [1.05, 0.4, 1.4],
      [-1.05, 0.4, -1.4],
      [1.05, 0.4, -1.4],
    ];
    this.wheels = [];
    for (const [x, y, z] of positions) {
      const w = new THREE.Mesh(wheelGeo, wheelMat);
      w.rotation.z = Math.PI / 2;
      w.position.set(x, y, z);
      body.add(w);
      this.wheels.push(w);
    }
    // headlights
    const hl = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, 0.3, 0.2),
      new THREE.MeshBasicMaterial({ color: 0xfff6b0 })
    );
    hl.position.set(-0.65, 1.0, 2.05);
    const hr = hl.clone();
    hr.position.x = 0.65;
    body.add(hl); body.add(hr);
    this.body = body;
    body.position.copy(this.pos);
    scene.add(body);
  }

  update(dt, driverInputs) {
    if (!this.alive) return;

    let accel = 0, steer = 0, brake = 0, handbrake = 0;
    if (driverInputs) {
      accel = driverInputs.accel || 0;
      steer = driverInputs.steer || 0;
      brake = driverInputs.brake || 0;
      handbrake = driverInputs.handbrake ? 1 : 0;
    }

    const engineForce = accel * 22;
    // simple drag + engine
    this.speed += engineForce * dt;
    this.speed -= brake * 40 * Math.sign(this.speed || 1) * dt;
    // drag
    this.speed *= Math.max(0, 1 - dt * 0.45);
    // handbrake
    if (handbrake) this.speed *= Math.max(0, 1 - dt * 3);
    this.speed = Math.max(-this.maxSpeed * 0.5, Math.min(this.maxSpeed, this.speed));

    // steer affected by speed
    if (Math.abs(this.speed) > 0.2) {
      const factor = 1.6 * Math.sign(this.speed);
      this.yaw += steer * factor * dt * Math.min(1.0, Math.abs(this.speed) / 8);
    }

    const dx = -Math.sin(this.yaw) * this.speed * dt;
    const dz = -Math.cos(this.yaw) * this.speed * dt;
    this.pos.x += dx;
    this.pos.z += dz;
    // collide (lose speed if hit wall)
    const before = { x: this.pos.x, z: this.pos.z };
    resolveXZ(this.pos, this.radius);
    if (Math.abs(before.x - this.pos.x) + Math.abs(before.z - this.pos.z) > 0.01) {
      this.speed *= 0.3;
      this.hp -= Math.abs(this.speed) * 0.3;
    }

    this.body.position.copy(this.pos);
    this.body.rotation.y = this.yaw;
    // wheel spin
    for (const w of this.wheels) {
      w.rotation.x += this.speed * dt * 2;
    }
  }
}

export function playerCarInputs() {
  let accel = 0, steer = 0, brake = 0;
  if (input.keys.has('KeyW') || input.keys.has('ArrowUp')) accel += 1;
  if (input.keys.has('KeyS') || input.keys.has('ArrowDown')) accel -= 1;
  if (input.keys.has('KeyA') || input.keys.has('ArrowLeft')) steer += 1;
  if (input.keys.has('KeyD') || input.keys.has('ArrowRight')) steer -= 1;
  if (input.keys.has('Space')) brake += 1;
  const handbrake = input.keys.has('ShiftLeft') || input.keys.has('ShiftRight');
  return { accel, steer, brake, handbrake };
}
