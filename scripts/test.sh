#!/usr/bin/env bash
# Тесты: ./scripts/test.sh [--with-postgres] [IDE_HOME]
source "$(dirname "$0")/common.sh"
PG=""; [[ "${1:-}" == "--with-postgres" ]] && { PG=1; shift; }
assert_jdk; find_ide "${1:-}"; run_research --check >/dev/null || true; assert_compatibility
if [[ -n "$PG" ]]; then
  docker compose -f "$PROJECT_ROOT/docker/docker-compose.yml" up -d --wait || fail "Не удалось запустить PostgreSQL"
  export OPENDATA_PG_URL="${OPENDATA_PG_URL:-jdbc:postgresql://localhost:54329/opendata}"
fi
gradle_args_base
run_gradle "${GRADLE_ARGS[@]}" :opendata:integration:test
echo "TESTS OK"
