#!/usr/bin/env bash
# Запуск песочницы: ./scripts/run.sh [--poc] [IDE_HOME]
source "$(dirname "$0")/common.sh"
POC=""; [[ "${1:-}" == "--poc" ]] && { POC=1; shift; }
assert_jdk; find_ide "${1:-}"; run_research --check >/dev/null || true; assert_compatibility
gradle_args_base
REPORT="$PROJECT_ROOT/build/poc/diagnostics.txt"
if [[ -n "$POC" ]]; then
  mkdir -p "$PROJECT_ROOT/build/poc/project"; cp "$PROJECT_ROOT"/docs/poc/*.sql "$PROJECT_ROOT/build/poc/project/" 2>/dev/null || true
  rm -f "$REPORT"; GRADLE_ARGS+=("-PopenProject=$PROJECT_ROOT/build/poc/project")
fi
run_gradle "${GRADLE_ARGS[@]}" :opendata:integration:runIde
if [[ -n "$POC" ]]; then
  [[ -f "$REPORT" ]] || fail "Отчёт $REPORT не создан"
  cat "$REPORT"; grep -q 'RESULT: OK' "$REPORT" || fail "POC: диагностика не пройдена"
  echo "POC (автоматическая часть): OK"
fi
