#!/usr/bin/env bash
# Запуск собранной OpenData IDE:
#   ./scripts/run.sh                                   — обычный запуск
#   ./scripts/run.sh --check                           — проверка запуска (плагины загружены, окна есть): RESULT=OK
#   ./scripts/run.sh --smoke <id источника> [schema.table]
# Самопроверка раскрывает источник в Database Explorer, выполняет запрос из консоли (DataGrid), открывает таблицу
# и пишет отчёт в build/poc/diagnostics.txt. Свой запрос — переменная OPENDATA_SMOKE_SQL. Без дисплея — xvfb-run.
source "$(dirname "$0")/common.sh"
exe="$(product_dir)/bin/opendata"
[[ -x "$exe" ]] || fail "IDE не собрана: ./scripts/build.sh"
mode="${1:-}"
if [[ "$mode" != "--smoke" && "$mode" != "--check" ]]; then
  exec "$exe"
fi
ds=""; table=""
if [[ "$mode" == "--smoke" ]]; then ds="${2:?id источника данных}"; table="${3:-}"; fi
report="$PROJECT_ROOT/build/poc/diagnostics.txt"; mkdir -p "$(dirname "$report")"; rm -f "$report"
vm="$PROJECT_ROOT/build/poc/smoke.vmoptions"
{
  echo "-Dopendata.diagnostics.file=$report"
  [[ -n "$ds" ]] && echo "-Dopendata.smoke=$ds"
  [[ -n "$table" ]] && echo "-Dopendata.smoke.table=$table"
  [[ -n "${OPENDATA_SMOKE_SQL:-}" ]] && echo "-Dopendata.smoke.sql=$OPENDATA_SMOKE_SQL"
  true
} > "$vm"
OPENDATA_VM_OPTIONS="$vm" "$exe" >/dev/null 2>&1 &
pid=$!
for _ in $(seq 1 120); do [[ -f "$report" ]] && break; sleep 2; done
[[ -f "$report" ]] || { kill "$pid" 2>/dev/null; fail "Отчёт самопроверки не создан"; }
sleep 2; cat "$report"; kill "$pid" 2>/dev/null || true
marker="RESULT=OK"; [[ -n "$ds" ]] && marker="SMOKE=OK"
grep -q "$marker" "$report" || fail "Самопроверка не пройдена"
echo "$marker"
