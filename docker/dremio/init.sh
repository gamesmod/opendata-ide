#!/usr/bin/env bash
# Начальная настройка тестового Dremio: первый пользователь, пространство opendata и представление events_v.
# Использование: docker/dremio/init.sh [http://localhost:9047]
set -euo pipefail
URL="${1:-http://localhost:9047}"

for i in $(seq 1 120); do
  [ "$(curl -s "$URL/apiv2/server_status" || true)" = '"OK"' ] && break
  sleep 2
done

# Первый пользователь (повторный вызов вернёт ошибку — это нормально).
curl -s -X PUT "$URL/apiv2/bootstrap/firstuser" -H 'Authorization: _dremionull' -H 'Content-Type: application/json' \
  -d '{"userName":"dremio","firstName":"Open","lastName":"Data","email":"dremio@example.com","password":"dremio123"}' >/dev/null || true

TOKEN=$(curl -s -X POST "$URL/apiv2/login" -H 'Content-Type: application/json' \
  -d '{"userName":"dremio","password":"dremio123"}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
[ -n "$TOKEN" ] || { echo "Dremio login failed" >&2; exit 1; }
AUTH="Authorization: _dremio$TOKEN"

curl -s -X POST "$URL/api/v3/catalog" -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"entityType":"space","name":"opendata"}' >/dev/null || true
curl -s -X POST "$URL/api/v3/sql" -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"sql\":\"CREATE OR REPLACE VIEW opendata.events_v AS SELECT * FROM (VALUES (1,'alpha'),(2,'beta'),(3,'gamma')) AS t(id, name)\"}" >/dev/null

# Ждём, пока представление станет видимым через Flight SQL (задание SQL выполняется асинхронно).
sleep 5
echo "Dremio ready: $URL (dremio/dremio123), space opendata, view events_v"
