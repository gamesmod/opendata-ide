#!/usr/bin/env bash
# Сборка OpenData IDE для Linux: ./scripts/build.sh   (SKIP_TESTS=1 — без тестов, OPENDATA_PLATFORM_HOME — своя платформа)
source "$(dirname "$0")/common.sh"
step "1/4 JDK"; assert_jdk
step "2/4 Открытая платформа IntelliJ"; get_oss_platform
echo "Платформа: $PLATFORM ($(sed -n 's/.*"buildNumber": *"\([^"]*\)".*/\1/p' "$PLATFORM/product-info.json" | head -1))"
step "3/4 DB-плагин opendata-db"
args=("-PossIdePath=$PLATFORM" --console=plain :opendata:db:buildPlugin)
[[ -z "${SKIP_TESTS:-}" ]] && args+=(:opendata:db:test)
run_gradle "${args[@]}"
step "4/4 Продукт OpenData IDE"
run_gradle "-PossIdePath=$PLATFORM" -PproductOs=linux --console=plain assembleProduct
version="$(gradle_prop version)"
mkdir -p "$PROJECT_ROOT/build/distributions"
tar -C "$PROJECT_ROOT/build/product/linux" -czf "$PROJECT_ROOT/build/distributions/OpenData-IDE-$version-linux-x64.tar.gz" \
  --transform "s|^OpenData-IDE|OpenData-IDE-$version|" OpenData-IDE
echo "BUILD OK"
echo "IDE:         $(product_dir)/bin/opendata"
echo "Дистрибутив: $PROJECT_ROOT/build/distributions/OpenData-IDE-$version-linux-x64.tar.gz"
