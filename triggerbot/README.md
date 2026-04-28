# stb — stealth trigger bot

Scoped client-only Fabric mod for Minecraft 1.21.8 (singleplayer). No visible
traces in Minecraft's UI.

## Features

- Mixin-based key handling — no entries in the vanilla `Options → Controls` menu
- No HUD, no chat messages, no logger calls, no toast notifications
- Config file stored as `.minecraft/config/.opts.dat` (dotfile, hidden by
  default on most OSes; unusual extension so it blends in)
- Wait-cooldown, crit-only, weapon-only, line-of-sight, FOV filter
- Random interval jitter, switch-target delay, occasional skipped attacks to
  humanise timing
- Target filters: hostile / passive / players / utility (armor stands & item
  frames ignored by default)
- ProGuard obfuscation on the built jar — internal classes renamed to single
  letters at the root package, source file names stripped

## Hotkeys

| Combo | Action |
|---|---|
| `Right Shift` | Toggle enabled / disabled |
| `Right Shift` + `Right Ctrl` | Open hidden config screen |

None of these are registered as `KeyMapping`s — they are intercepted in a
Mixin to `KeyboardHandler`, so they cannot be rebound or spotted in the
controls menu.

## Build

```bash
./gradlew build
```

Produces `build/libs/stb-1.0.0.jar` (obfuscated with ProGuard).

Drop the jar into `.minecraft/mods` together with Fabric API
(≥ 0.136.1+1.21.8). Works with Fabric Loader ≥ 0.19.2 on Minecraft 1.21.8.

## Security notes

The ProGuard step renames all internal classes/methods/fields, strips line
numbers and source file names, and repackages everything at the root. Only
the Fabric entrypoint classes and the Mixin class keep their original names
(they are referenced by string in `fabric.mod.json` / `stb.client.mixins.json`
and cannot be renamed without updating those files). Decompiling the jar
will produce a pile of single-letter helpers that are difficult to follow.

This is a client-side helper intended for offline / singleplayer use.
