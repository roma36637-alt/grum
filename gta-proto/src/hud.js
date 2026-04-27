export const hudEls = {
  hp: document.getElementById('hp'),
  ammo: document.getElementById('ammo'),
  money: document.getElementById('money'),
  stars: document.getElementById('stars'),
  mission: document.getElementById('mission'),
  prompt: document.getElementById('prompt'),
  banner: document.getElementById('banner'),
};

let promptTimer = 0;
export function setPrompt(text, seconds = 0.5) {
  hudEls.prompt.textContent = text;
  hudEls.prompt.style.display = text ? 'block' : 'none';
  promptTimer = seconds;
}

export function setBanner(text, seconds = 2.0) {
  hudEls.banner.textContent = text;
  hudEls.banner.style.display = text ? 'block' : 'none';
  if (text) {
    setTimeout(() => {
      hudEls.banner.style.display = 'none';
    }, seconds * 1000);
  }
}

export function updateHud({ hp, ammo, clip, money, stars, mission, hasGun }) {
  hudEls.hp.textContent = Math.max(0, Math.round(hp));
  hudEls.ammo.textContent = hasGun ? `${clip}/${ammo}` : '—';
  hudEls.money.textContent = money;
  hudEls.stars.textContent = stars > 0 ? '★'.repeat(stars) + '☆'.repeat(5 - stars) : '—';
  hudEls.mission.textContent = 'Миссия: ' + mission;
}

export function tickPrompt(dt) {
  if (promptTimer > 0) {
    promptTimer -= dt;
    if (promptTimer <= 0) hudEls.prompt.style.display = 'none';
  }
}
