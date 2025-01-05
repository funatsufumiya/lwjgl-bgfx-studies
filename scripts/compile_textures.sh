#!/bin/bash

# check if shaderc exists
if ! [ -x "$(command -v texturec)" ]; then
    echo "[Error] texturec is not installed." >&2
    echo "        Please install shaderc from bgfx-tools." >&2
    exit 1
fi

TEXTUREC=texturec
# TEXTUREC_FLAGS="-m"
TEXTUREC_FLAGS=""
TEXTURE_OUT_EXT="ktx"
# TEXTURE_OUT_EXT="dds"

PROJECT_DIR=$(dirname $(dirname $(realpath $0)))
echo "Project directory: $PROJECT_DIR"

SRC_DIR=$PROJECT_DIR/res/textures/src
OUT_DIR=$PROJECT_DIR/res/textures
echo "Source directory: $SRC_DIR"
echo "Output directory: $OUT_DIR"

function compile {
    # pass all args to shaderc, with printing the command
     
    echo "$TEXTUREC $@"
    $TEXTUREC $@
}

function build_impl {
    compile -t $1 -o $2 -f $3 $TEXTUREC_FLAGS
}

function build {
    file_dir=$(dirname $1)
    file_base_name=$(basename $1)
    file_without_ext=${file_base_name%.*}
    format=$2
    build_impl $format $OUT_DIR/$file_without_ext.$TEXTURE_OUT_EXT $file_dir/$file_base_name
}

echo "---"

build $SRC_DIR/bgfx_logo.png bc7