## LWJGL BGFX Studies

My studies of the BGFX library using the LWJGL3.

(Mainly scratches for understanding and implementations of [processing-lwjgl-bgfx](https://github.com/funatsufumiya/processing-lwjgl-bgfx))

## Compiling Shaders

- firstly, install bgfx-tools from [for mac](https://www.lwjgl.org/browse/release/3.3.5/macosx/arm64/bgfx-tools), [for win](https://www.lwjgl.org/browse/release/3.3.5/windows/x64/bgfx-tools) or [for linux](https://www.lwjgl.org/browse/release/3.3.5/linux/x64/bgfx-tools)
    - after downloading, please copy `shaderc` to `PATH` directory

```bash
bash ./scripts/compile_shaders.sh
```

### (On Windows)

```bash
# on git bash
PLATFORM=windows bash ./scripts/compile_shaders.sh
```

## Run

### Basic

```bash
./gradlew buildExample
./gradlew runHelloBGFX
```

### Sketch

```bash
./gradlew buildExample
./gradlew runHelloSketch
```