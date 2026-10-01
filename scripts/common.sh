#!/usr/bin/env bash
# Общие функции скриптов OpenData IDE для Linux / macOS (аналог common.ps1).
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REQUIRED_JDK=21

step() { printf '\n\033[36m==> %s\033[0m\n' "$1"; }
fail() { printf '\n\033[31mERROR: %s\033[0m\n' "$1" >&2; exit 1; }
gradle_prop() { sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*//p" "$PROJECT_ROOT/gradle.properties" | head -1; }
state_prop() { sed -n "s/^$1=//p" "$PROJECT_ROOT/build/research/ide.properties" | head -1 | sed 's/\\\(.\)/\1/g'; }
wrapper_gradle_version() { sed -n 's/.*gradle-\([0-9.]*\)-\(bin\|all\)\.zip/\1/p' "$PROJECT_ROOT/gradle/wrapper/gradle-wrapper.properties"; }

java_major() {
  "$1" -XshowSettings:properties -version 2>&1 | sed -n 's/.*java\.specification\.version = \([0-9]*\).*/\1/p' | head -1
}

assert_jdk() {
  local c candidates=()
  [[ -n "${JAVA_HOME:-}" ]] && candidates+=("$JAVA_HOME/bin/java")
  command -v java >/dev/null 2>&1 && candidates+=("$(command -v java)")
  for c in "${candidates[@]}"; do
    [[ -x "$c" ]] || continue
    local m; m="$(java_major "$c")"
    if [[ -n "$m" && "$m" -ge $REQUIRED_JDK && -x "$(dirname "$c")/javac" ]]; then
      JAVA="$c"; echo "JDK: $JAVA (Java $m)"; return
    fi
  done
  fail "Не найден JDK ${REQUIRED_JDK}+ (с javac). Установите JDK 21 и задайте JAVA_HOME."
}

is_ide_home() { [[ -f "$1/product-info.json" || -f "$1/Contents/Resources/product-info.json" ]]; }

# Порядок поиска (ТЗ 8.2): аргумент → JETBRAINS_IDE_HOME → DATAGRIP_HOME → стандартные каталоги.
find_ide() {
  local p
  for p in "${1:-}" "${JETBRAINS_IDE_HOME:-}" "${DATAGRIP_HOME:-}"; do
    if [[ -n "$p" ]]; then
      is_ide_home "$p" || fail "Каталог IDE '$p' не содержит product-info.json"
      IDE_HOME="$(cd "$p" && pwd)"; echo "IDE_HOME: $IDE_HOME"; return
    fi
  done
  local roots=(/opt "$HOME/.local/share/JetBrains/Toolbox/apps" /Applications "$HOME/Applications")
  local best="" d
  for r in "${roots[@]}"; do
    [[ -d "$r" ]] || continue
    while IFS= read -r d; do
      [[ "$d" =~ [Dd]ata[Gg]rip|[Ii][Dd][Ee][Aa]|[Ii]ntelli[Jj] ]] || continue
      if is_ide_home "$d" && [[ -d "$d/plugins/DatabaseTools" || -d "$d/Contents/plugins/DatabaseTools" ]]; then
        echo "  найдено: $d"; [[ -z "$best" || "$d" =~ [Dd]ata[Gg]rip ]] && best="$d"
      fi
    done < <(find "$r" -maxdepth 4 -type d 2>/dev/null)
  done
  [[ -n "$best" ]] || fail "Не найдена IDE/DataGrip с Database Tools. Укажите путь аргументом или JETBRAINS_IDE_HOME."
  IDE_HOME="$best"; echo "IDE_HOME: $IDE_HOME"
}

jbr_home() {
  local p
  for p in "$IDE_HOME/jbr" "$IDE_HOME/Contents/jbr/Contents/Home"; do [[ -x "$p/bin/java" ]] && { echo "$p"; return; }; done
}

run_research() {  # run_research [--check] [--community dir]
  [[ " $* " == *" --check "* ]] && rm -f "$PROJECT_ROOT/build/research/ide.properties"
  "$JAVA" "$PROJECT_ROOT/tools/research/Research.java" --ide "$IDE_HOME" --out "$PROJECT_ROOT/docs" \
    --state "$PROJECT_ROOT/build/research" --gradle "$(wrapper_gradle_version)" "$@"
}

assert_compatibility() {
  [[ -f "$PROJECT_ROOT/build/research/ide.properties" ]] || fail "Нет build/research/ide.properties"
  echo "IDE: $(state_prop ide.name) $(state_prop ide.version), build $(state_prop ide.productCode)-$(state_prop ide.buildNumber), JBR $(state_prop jbr.version)"
  [[ "$(state_prop database.plugin.found)" == "true" ]] || fail "В установке нет Database Tools and SQL (com.intellij.database)."
  echo "Database Tools: $(state_prop database.plugin.id) $(state_prop database.plugin.version)"
  local since baseline; since="$(gradle_prop pluginSinceBuild)"; baseline="$(state_prop ide.baseline)"
  [[ "$baseline" =~ ^[0-9]+$ ]] || fail "Не удалось разобрать build number IDE"
  (( baseline >= since )) || fail "Несовместимая сборка: IDE baseline $baseline < pluginSinceBuild $since"
}

gradle_args_base() {
  GRADLE_ARGS=("-PlocalIdePath=$IDE_HOME" --console=plain)
  local j; j="$(jbr_home || true)"
  [[ -n "$j" ]] && GRADLE_ARGS+=("-Porg.gradle.java.installations.paths=$j")
  return 0
}

run_gradle() { echo "> gradlew $*"; (cd "$PROJECT_ROOT" && ./gradlew "$@") || fail "Gradle завершился с ошибкой"; }
