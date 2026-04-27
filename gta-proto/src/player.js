import * as THREE from 'three';
import { resolveXZ } from './world.js';
import { input } from './input.js';

export class Player {
  constructor(scene) {
    this.scene = scene;
    this.radius = 0.5;
    this.height = 1.8;
    this.pos = new THREE.Vector3(0, 0, 8);
    this.vel = new THREE.Vector3();
    this.onGround = true;
    this.hp = 100;
    this.alive = true;
    this.ammo = 0;
    this.maxAmmo = 0;
    this.clip = 0;
    this.clipSize = 12;
    this.reloadTimer = 0;
    this.money = 500;
    this.hasGun = false;
    this.shootCd = 0;
    this.inCar = null;
    this.walkAnim = 0;

    // body
    const body = new THREE.Group();
    const torso = new THREE.Mesh(
      new THREE.BoxGeometry(0.7, 0.9, 0.35),
      new THREE.MeshLambertMaterial({ color: 0x1e88e5 })
    );
    torso.position.y = 1.05;
    body.add(torso);
    const head = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, 0.4, 0.4),
      new THREE.MeshLambertMaterial({ color: 0xffccaa })
    );
    head.position.y = 1.7;
    body.add(head);
    const legL = new THREE.Mesh(
      new THREE.BoxGeometry(0.25, 0.8, 0.25),
      new THREE.MeshLambertMaterial({ color: 0x222222 })
    );
    legL.position.set(-0.18, 0.4, 0);
    const legR = legL.clone();
    legR.position.x = 0.18;
    body.add(legL); body.add(legR);
    const armL = new THREE.Mesh(
      new THREE.BoxGeometry(0.2, 0.8, 0.25),
      new THREE.MeshLambertMaterial({ color: 0x1e88e5 })
    );
    armL.position.set(-0.45, 1.05, 0);
    const armR = armL.clone();
    armR.position.x = 0.45;
    body.add(armL); body.add(armR);
    this.body = body;
    this.parts = { torso, head, legL, legR, armL, armR };
    scene.add(body);
  }

  shootRay(yawOffset = 0) {
    // Camera-aligned ray from head
    const origin = new THREE.Vector3(this.pos.x, this.pos.y + 1.7, this.pos.z);
    const dir = new THREE.Vector3(
      -Math.sin(input.yaw + yawOffset) * Math.cos(input.pitch),
      Math.sin(input.pitch),
      -Math.cos(input.yaw + yawOffset) * Math.cos(input.pitch)
    ).normalize();
    return { origin, dir };
  }

  update(dt) {
    if (!this.alive) { this.body.visible = true; return; }

    if (this.inCar) {
      this.body.visible = false;
      this.pos.copy(this.inCar.pos);
      return;
    }
    this.body.visible = true;

    // movement
    const speed = input.keys.has('ShiftLeft') || input.keys.has('ShiftRight') ? 7.5 : 4.2;
    let forward = 0, side = 0;
    if (input.keys.has('KeyW') || input.keys.has('ArrowUp')) forward += 1;
    if (input.keys.has('KeyS') || input.keys.has('ArrowDown')) forward -= 1;
    if (input.keys.has('KeyA') || input.keys.has('ArrowLeft')) side -= 1;
    if (input.keys.has('KeyD') || input.keys.has('ArrowRight')) side += 1;
    const len = Math.hypot(forward, side) || 1;
    forward /= len; side /= len;

    const yaw = input.yaw;
    const moveX = (-Math.sin(yaw) * forward + Math.cos(yaw) * side) * speed;
    const moveZ = (-Math.cos(yaw) * forward - Math.sin(yaw) * side) * speed;

    this.vel.x = moveX;
    this.vel.z = moveZ;

    // gravity / jump
    if (this.onGround && input.keys.has('Space')) {
      this.vel.y = 6.5;
      this.onGround = false;
    }
    this.vel.y -= 18 * dt;
    this.pos.x += this.vel.x * dt;
    this.pos.z += this.vel.z * dt;
    this.pos.y += this.vel.y * dt;
    if (this.pos.y <= 0) {
      this.pos.y = 0;
      this.vel.y = 0;
      this.onGround = true;
    }

    resolveXZ(this.pos, this.radius);

    // Orient body towards camera yaw
    this.body.position.copy(this.pos);
    this.body.rotation.y = yaw;

    // simple walk animation
    const moving = (Math.abs(forward) + Math.abs(side)) > 0.01;
    if (moving) {
      this.walkAnim += dt * speed;
      const s = Math.sin(this.walkAnim * 2);
      this.parts.legL.rotation.x = s * 0.8;
      this.parts.legR.rotation.x = -s * 0.8;
      this.parts.armL.rotation.x = -s * 0.6;
      this.parts.armR.rotation.x = s * 0.6;
    } else {
      this.parts.legL.rotation.x *= 0.8;
      this.parts.legR.rotation.x *= 0.8;
      this.parts.armL.rotation.x *= 0.8;
      this.parts.armR.rotation.x *= 0.8;
    }

    // gun cooldowns
    this.shootCd = Math.max(0, this.shootCd - dt);
    if (this.reloadTimer > 0) {
      this.reloadTimer -= dt;
      if (this.reloadTimer <= 0) {
        const need = this.clipSize - this.clip;
        const take = Math.min(need, this.ammo);
        this.clip += take;
        this.ammo -= take;
      }
    }
  }

  takeDamage(n) {
    if (!this.alive) return;
    this.hp -= n;
    if (this.hp <= 0) {
      this.hp = 0;
      this.alive = false;
    }
  }

  respawn() {
    this.hp = 100;
    this.alive = true;
    this.pos.set(0, 0, 8);
    this.vel.set(0, 0, 0);
    this.inCar = null;
    this.body.visible = true;
  }
}
