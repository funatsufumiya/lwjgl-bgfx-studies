#!/bin/bash

# check if shaderc exists
if ! [ -x "$(command -v shaderc)" ]; then
    echo "[Error] shaderc is not installed." >&2
    echo "        Please install shaderc from bgfx-tools." >&2
    exit 1
fi

PROJECT_DIR=$(dirname $(dirname $(realpath $0)))
echo "Project directory: $PROJECT_DIR"

SHADER_DIR=$PROJECT_DIR/res/shaders
echo "Shader directory: $SHADER_DIR"

SRC_DIR=$SHADER_DIR/src
METAL_DIR=$SHADER_DIR/metal
GLSL_DIR=$SHADER_DIR/glsl
SPIRV_DIR=$SHADER_DIR/spirv
DX_DIR=$SHADER_DIR/dx11

SHADERC=shaderc

function compile {
    # pass all args to shaderc, with printing the command
     
    echo "$SHADERC $@"
    $SHADERC $@
}

# SHADER_LIST=$(ls $SRC_DIR | grep .sc)
# echo "Shader list: $SHADER_LIST"

# if PLATFORM unspecified, determine platform
if [ -z "$PLATFORM" ]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        PLATFORM=osx
    elif [[ "$OSTYPE" == "linux-gnu" ]]; then
        PLATFORM=linux
    elif [[ "$OSTYPE" == "msys" ]]; then
        PLATFORM=windows
    else
        PLATFORM=windows # WORKAROUND
    fi
    echo "Platform: $PLATFORM (auto detected)"
else
    echo "Platform: $PLATFORM (specified by env)"
fi

echo "---"

function build {
    # NOTE: see detail from https://github.com/bkaradzic/bgfx/blob/master/scripts/shader.mk

    shader_name=$1

    # check shader name begins from fs, vs, or cs
    is_fs=$(echo $shader_name | grep -c "^fs")
    is_vs=$(echo $shader_name | grep -c "^vs")
    is_cs=$(echo $shader_name | grep -c "^cs")

    FLAGS="-i $SRC_DIR"

    if [ "$PLATFORM" == "osx" ]; then
        if [ $is_fs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type f -o $METAL_DIR/$shader_name.bin --platform osx -p metal --disasm
        elif [ $is_vs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type v -o $METAL_DIR/$shader_name.bin --platform osx -p metal --disasm
        elif [ $is_cs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type c -o $METAL_DIR/$shader_name.bin --platform osx -p metal --disasm
        fi
    elif [ "$PLATFORM" == "windows" ]; then
        if [ $is_fs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type f -o $DX_DIR/$shader_name.bin --platform windows -p s_5_0 -O 3 --disasm
        elif [ $is_vs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type v -o $DX_DIR/$shader_name.bin --platform windows -p s_5_0 -O 3 --disasm
        elif [ $is_cs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type c -o $DX_DIR/$shader_name.bin --platform windows -p s_5_0 -O 1 --disasm
        fi
    elif [ "$PLATFORM" == "linux" ]; then
        if [ $is_fs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type f -o $GLSL_DIR/$shader_name.bin --platform linux -p 120 --disasm
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type f -o $SPIRV_DIR/$shader_name.bin --platform linux -p spirv --disasm
        elif [ $is_vs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type v -o $GLSL_DIR/$shader_name.bin --platform linux -p 120 --disasm
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type v -o $SPIRV_DIR/$shader_name.bin --platform linux -p spirv --disasm
        elif [ $is_cs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type c -o $GLSL_DIR/$shader_name.bin --platform linux -p 430 --disasm
            compile -f $SRC_DIR/$shader_name.sc $FLAGS --type c -o $SPIRV_DIR/$shader_name.bin --platform linux -p spirv --disasm
        fi
    fi
}

build fs_basic
build vs_basic