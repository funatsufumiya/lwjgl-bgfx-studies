#!/bin/bash

PROJECT_DIR=$(dirname $(dirname $(realpath $0)))
echo "Project directory: $PROJECT_DIR"

SHADER_DIR=$PROJECT_DIR/res/shaders
echo "Shader directory: $SHADER_DIR"
echo "---"

SRC_DIR=$SHADER_DIR/src
METAL_DIR=$SHADER_DIR/metal
GLSL_DIR=$SHADER_DIR/glsl
SPIRV_DIR=$SHADER_DIR/spirv
HLSL_DIR=$SHADER_DIR/hlsl

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
    else
        PLATFORM=windows
    fi
fi

function build {
    # NOTE: see detail from https://github.com/bkaradzic/bgfx/blob/master/scripts/shader.mk

    shader_name=$1

    # check shader name begins from fs, vs, or cs
    is_fs=$(echo $shader_name | grep -c "^fs")
    is_vs=$(echo $shader_name | grep -c "^vs")
    is_cs=$(echo $shader_name | grep -c "^cs")

    if [ "$PLATFORM" == "osx" ]; then
        if [ $is_fs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type f -o $METAL_DIR/$shader_name.bin --platform osx -p metal
        elif [ $is_vs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type v -o $METAL_DIR/$shader_name.bin --platform osx -p metal
        elif [ $is_cs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type c -o $METAL_DIR/$shader_name.bin --platform osx -p metal
        fi
    elif [ "$PLATFORM" == "windows" ]; then
        if [ $is_fs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type f -o $HLSL_DIR/$shader_name.bin --platform windows -p s_5_0 -O 3
        elif [ $is_vs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type v -o $HLSL_DIR/$shader_name.bin --platform windows -p s_5_0 -O 3
        elif [ $is_cs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type c -o $HLSL_DIR/$shader_name.bin --platform windows -p s_5_0 -O 1
        fi
    elif [ "$PLATFORM" == "linux" ]; then
        if [ $is_fs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type f -o $GLSL_DIR/$shader_name.bin --platform linux -p 120
            compile -f $SRC_DIR/$shader_name.sc --type f -o $SPIRV_DIR/$shader_name.bin --platform linux -p spirv
        elif [ $is_vs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type v -o $GLSL_DIR/$shader_name.bin --platform linux -p 120
            compile -f $SRC_DIR/$shader_name.sc --type v -o $SPIRV_DIR/$shader_name.bin --platform linux -p spirv
        elif [ $is_cs -eq 1 ]; then
            compile -f $SRC_DIR/$shader_name.sc --type c -o $GLSL_DIR/$shader_name.bin --platform linux -p 430
            compile -f $SRC_DIR/$shader_name.sc --type c -o $SPIRV_DIR/$shader_name.bin --platform linux -p spirv
        fi
    fi
}

build fs_basic
build vs_basic