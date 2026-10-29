#!/usr/bin/env bash
# Extracts the guides and images of a release into latest/ and assets/latest/images/, which docmd.config.js builds as
# the default version. The image paths are rewritten, so the release keeps its own screenshots when main changes them.
set -euo pipefail

tag="${1:?usage: scripts/prepare-latest.sh <release tag>}"
cd "$(dirname "$0")/.."

extract() {
    # git archive run in a subdirectory archives only that subdirectory of the tree, so it runs from the top level
    git -C "$(git rev-parse --show-toplevel)" archive "$(git rev-parse "$tag:./$1")" | tar -x -C "$2"
}

rm -rf latest assets/latest
mkdir -p latest assets/latest/images
extract docs latest
extract assets/images assets/latest/images
find latest -name '*.md' -exec perl -pi -e 's#/assets/images/#/assets/latest/images/#g' {} +
