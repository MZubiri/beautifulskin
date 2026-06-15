#!/bin/bash
set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
OUTPUT="${BASE_DIR}/BeautifulSkin_DAWII_entrega.zip"

rm -f "$OUTPUT"
cd "$BASE_DIR"
zip -r "$OUTPUT" . \
  -x '*/node_modules/*' '*/target/*' '*/dist/*' '*/bin/*' \
     '*/.angular/*' '*/.settings/*' '*/.vscode/*' \
     '*/.classpath' '*/.project' '*/.factorypath' '*.log' '*.zip'

echo "Entrega creada en: $OUTPUT"
