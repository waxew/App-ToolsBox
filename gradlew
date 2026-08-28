#!/usr/bin/env sh
set -eu

# Lightweight Gradle bootstrap for the source bundle.
GRADLE_VERSION="9.5.0"
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DIST_ROOT="$BASE_DIR/.gradle-dist"
GRADLE_HOME="$DIST_ROOT/gradle-$GRADLE_VERSION"
ZIP_FILE="$DIST_ROOT/gradle-$GRADLE_VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$DIST_ROOT"
  if [ ! -f "$ZIP_FILE" ]; then
    if command -v curl >/dev/null 2>&1; then curl -fL "$URL" -o "$ZIP_FILE"
    elif command -v wget >/dev/null 2>&1; then wget "$URL" -O "$ZIP_FILE"
    else echo "curl or wget is required to download Gradle." >&2; exit 1
    fi
  fi
  if command -v unzip >/dev/null 2>&1; then unzip -q -o "$ZIP_FILE" -d "$DIST_ROOT"
  else echo "unzip is required to extract Gradle." >&2; exit 1
  fi
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
