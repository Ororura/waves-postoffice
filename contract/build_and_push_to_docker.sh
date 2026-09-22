#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 || -z "$1" ]]; then
    echo "Usage: $0 <image:tag>" >&2
    exit 2
fi

image="$1"
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$root/gradlew" -p "$root" \
    :contract:spotlessCheck :contract:clean :contract:build :contract:shadowJar --no-daemon

shopt -s nullglob
jars=("$root"/contract/build/libs/*-all.jar)
if (( ${#jars[@]} != 1 )); then
    echo "Expected one *-all.jar, found ${#jars[@]}" >&2
    exit 1
fi

docker build --platform linux/amd64 \
    --file "$root/contract/Dockerfile" \
    --tag "$image" \
    "$root/contract"

docker push "$image"
image_id="$(docker image inspect --format '{{.Id}}' "$image")"
printf 'image - %s\nimageHash - %s\n' "$image" "${image_id#sha256:}"
