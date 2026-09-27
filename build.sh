#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
mkdir -p build/classes dist
javac -encoding UTF-8 -d build/classes ChessGame.java ChessApp.java
cp ./*.png build/classes/
jar --create --file dist/ChessGame.jar --main-class ChessApp -C build/classes .
echo "Built dist/ChessGame.jar"
