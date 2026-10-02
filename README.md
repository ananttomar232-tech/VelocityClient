# Velocity Client

A lightweight Minecraft Java Edition client shell built on Fabric. It uses a clean, compact control panel inspired by the general usability of modern clients without copying their artwork.

## Included

- Right Shift opens the Velocity menu in-game.
- A Velocity button is added to the vanilla title screen.
- Performance profile for lower-end hardware: Fast graphics, clouds off, reduced particles, AO off, entity shadows off, view distance <= 8, simulation distance <= 5.
- Compact FPS / coordinates / sprint HUD toggles.
- Loaded-mod browser and mods-folder opener.
- Small, dependency-free properties config in `config/velocity-client.properties`.
- Fabric Loader compatibility means normal Fabric mods continue to work alongside Velocity.

## Target

Minecraft 1.21.1 + Fabric, Java 21.

The build file intentionally does **not** bundle Sodium or other third-party performance mods. Add the compatible versions you choose in the normal `mods` folder. The client shell remains lightweight on its own.

## Build

1. Install JDK 21 and either Gradle 8.x or IntelliJ IDEA with Gradle support.
2. Open this folder as a Gradle project.
3. Run `./gradlew build` (Windows: `gradlew.bat build`).
4. Put the produced jar from `build/libs/` into a Fabric 1.21.1 instance.

## Notes for a 15 GB RAM / 10th-gen i3 / integrated graphics PC

Start with 6–8 render distance, 4–5 simulation distance, Fast graphics, particles Decreased, clouds Off, entity shadows Off, and no shaders. The bundled profile applies the conservative settings above. Performance will still depend on the exact Dell integrated GPU, driver, world, and other installed mods.

## Next expansion ideas

The project is structured so a proper launcher can later sit beside the client core. Suggested modules are: account/profile UI, per-version mod instances, preset management, mod update checks, keybind profiles, and a lightweight cosmetics layer.
