# LWJGL BGFX Studies

![screenshot_triangle.png](./docs/screenshot_triangle.png)

My studies of the BGFX library using the LWJGL3.

(Mainly scratches for understanding and implementations of [processing-lwjgl-bgfx](https://github.com/funatsufumiya/processing-lwjgl-bgfx))

## Compiling Shaders

NOTE: Most of precompiled shaders are already included in this repository. (The process below is only needed when you want to update the shaders.)

- firstly, install bgfx-tools from [for mac](https://www.lwjgl.org/browse/release/3.3.5/macosx/arm64/bgfx-tools), [for win](https://www.lwjgl.org/browse/release/3.3.5/windows/x64/bgfx-tools) or [for linux](https://www.lwjgl.org/browse/release/3.3.5/linux/x64/bgfx-tools)
    - after downloading, please copy `shaderc` to `PATH` directory
    - (for windows, please use git bash)

```bash
bash ./scripts/compile_shaders.sh
```

## Build

```bash
./gradlew buildExample
```

## Run

- Basic, plain BGFX (`./gradlew runHelloBGFX`)
- Sketch style hello world (`./gradlew runHelloSketch`)
- Background (`./gradlew runBGSketch`)
- Triangle (`./gradlew runTriangleSketch`)
- Rectangle (`./gradlew runRectSketch`)

## Acknowledgements

This project is mainly based on the following projects (mixtures of them):

- https://github.com/funatsufumiya/hello_bgfx_triangle (applying latest update from https://thatonegamedev.com/cpp/hello-bgfx/, based on https://github.com/Paper-Cranes-Ltd/big2-stack)
- https://github.com/LWJGL/lwjgl3-demos

## Known Issues

- Triangle and Rectangle example, not working on Vulkan backend. (researching.)