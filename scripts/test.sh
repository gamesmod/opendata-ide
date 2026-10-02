#!/usr/bin/env bash
# Тесты DB-слоя на реальных СУБД: ./scripts/test.sh [--with-docker]
# --with-docker поднимает docker/docker-compose.yml (PostgreSQL, ClickHouse, Dremio) и задаёт OPENDATA_*_URL.
source "$(dirname "$0")/common.sh"
assert_jdk; get_oss_platform
if [[ "${1:-}" == "--with-docker" ]]; then
  docker compose -f "$PROJECT_ROOT/docker/docker-compose.yml" up -d --wait || fail "Не удалось поднять стенд"
  bash "$PROJECT_ROOT/docker/dremio/init.sh"
  export OPENDATA_PG_URL="${OPENDATA_PG_URL:-jdbc:postgresql://localhost:54329/opendata}"
  export OPENDATA_CH_URL="${OPENDATA_CH_URL:-jdbc:clickhouse://localhost:8123/opendata}"
  export OPENDATA_DREMIO_URL="${OPENDATA_DREMIO_URL:-jdbc:arrow-flight-sql://localhost:32010/?useEncryption=false}"
fi
run_gradle "-PossIdePath=$PLATFORM" --console=plain :opendata:db:test
echo "TESTS OK"
