#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
./build.sh
mkdir -p build/tests
javac -encoding UTF-8 -cp build/classes -d build/tests ChessGameTest.java
java -ea -cp build/classes:build/tests ChessGameTest
