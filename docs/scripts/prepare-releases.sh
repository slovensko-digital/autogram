#!/usr/bin/env bash
# Extracts the guides and images of each release, the latest first, into releases/<tag>/ and
# assets/releases/<tag>/images/, which docmd.config.js builds as versions. The image paths are rewritten, so a release
# keeps its own screenshots when main changes them. The changelog comes from main, see scripts/add-changelog.js.
set -euo pipefail

[ $# -gt 0 ] || { echo "usage: scripts/prepare-releases.sh <release tag>..." >&2; exit 1; }
cd "$(dirname "$0")/.."

extract() {
    # git archive run in a subdirectory archives only that subdirectory of the tree, so it runs from the top level
    git -C "$(git rev-parse --show-toplevel)" archive "$(git rev-parse "$1:./$2")" | tar -x -C "$3"
}

rm -rf releases assets/releases
for tag in "$@"; do
    mkdir -p "releases/$tag" "assets/releases/$tag/images"
    extract "$tag" docs "releases/$tag"
    extract "$tag" assets/images "assets/releases/$tag/images"
    find "releases/$tag" -name '*.md' -exec perl -pi -e "s#/assets/images/#/assets/releases/$tag/images/#g" {} +
    node scripts/add-changelog.js "$1" "releases/$tag"
done
