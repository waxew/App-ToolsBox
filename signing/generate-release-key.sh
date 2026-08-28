#!/usr/bin/env bash
set -euo pipefail

KEYSTORE="signing/App-ToolsBox-release.jks"
ALIAS="app-toolsbox"

if [[ -e "$KEYSTORE" ]]; then
  echo "Refusing to overwrite existing $KEYSTORE"
  exit 1
fi

read -rsp "Store password: " STORE_PASSWORD
echo
read -rsp "Key password: " KEY_PASSWORD
echo

keytool -genkeypair \
  -v \
  -keystore "$KEYSTORE" \
  -storepass "$STORE_PASSWORD" \
  -keypass "$KEY_PASSWORD" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -dname "CN=as Team, OU=Android, O=as Team"

echo "Created $KEYSTORE"
echo "Now create keystore.properties from keystore.properties.example."
