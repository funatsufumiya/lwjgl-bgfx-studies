# LWJGL BGFX Studies

![screenshot_triangle.png](./docs/screenshot_triangle.png)

My studies of the BGFX library using the LWJGL3. Working on Win/Mac/Linux, DX12/Metal/Vulkan.

(Mainly scratches for understanding and implementations of [processing-lwjgl-bgfx](https://github.com/funatsufumiya/processing-lwjgl-bgfx) and [minimax](https://github.com/funatsufumiya/minimax))

## Build

```bash
./gradlew buildExample
```

**NOTE**: This command is optional when execute [Run](#run) commands. (Run commands `dependsOn(buildExample)`.)

## Run

### Basic

- Basic, plain BGFX (`./gradlew runHelloBGFX`)
- Sketch style hello world (`./gradlew runHelloSketch`)
- Background (`./gradlew runBGSketch`)

### 2D

- Triangle 2D (`./gradlew runTriangleSketch`)
- Rectangle 2D (`./gradlew runRectSketch`)
- Texture 2D (`./gradlew runTextureSketch`)

### 3D

- Triangle 3D (`./gradlew runTriangle3DSketch`)
- Rectangle 3D (`./gradlew runRect3DSketch`)
- Texture 3D (`./gradlew runTexture3DSketch`)

### FrameBuffer

- FrameBufer 3D (`./gradlew runFrameBufer3DSketch`)

## Compiling Shaders

**NOTE**: Most of precompiled shaders are already included in this repository. (The process below is only needed when you want to update the shaders.)

- firstly, install bgfx-tools from [for mac](https://www.lwjgl.org/browse/release/3.3.5/macosx/arm64/bgfx-tools), [for win](https://www.lwjgl.org/browse/release/3.3.5/windows/x64/bgfx-tools) or [for linux](https://www.lwjgl.org/browse/release/3.3.5/linux/x64/bgfx-tools)
    - after downloading, please copy `shaderc` to `PATH` directory
    - (for windows, please use git bash)

```bash
bash ./scripts/compile_shaders.sh
```

## Compiling Textures

- Installation process is the same as [Compiling Shaders](#compiling-shaders)

```bash
bash ./scripts/compile_textures.sh
```

## Acknowledgements

This project is mainly based on the following projects (mixtures of them):

- https://github.com/funatsufumiya/hello_bgfx_triangle (applying latest update from https://thatonegamedev.com/cpp/hello-bgfx/, based on https://github.com/Paper-Cranes-Ltd/big2-stack)
- https://github.com/LWJGL/lwjgl3-demos

## Known Issues

- Triangle and Rectangle examples are not working on LWJGL 3.3.4 / 3.3.5 Vulkan backend. (researching reasons...)
    - For this reason, using LWJGL 3.3.3 for now.
