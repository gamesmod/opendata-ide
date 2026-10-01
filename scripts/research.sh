#!/usr/bin/env bash
# Этап 0: ./scripts/research.sh [IDE_HOME] [INTELLIJ_COMMUNITY_HOME]
source "$(dirname "$0")/common.sh"
step "JDK"; assert_jdk
step "JetBrains IDE / DataGrip"; find_ide "${1:-}"
COMMUNITY="${2:-${INTELLIJ_COMMUNITY_HOME:-}}"
step "Research"
args=(); [[ -n "$COMMUNITY" ]] && args=(--community "$COMMUNITY")
set +e; run_research "${args[@]}"; rc=$?; set -e
[[ $rc -eq 3 ]] && fail "Database Tools and SQL не найден — см. docs/jetbrains-db-analysis.md"
[[ $rc -eq 0 ]] || fail "Research завершился с кодом $rc"
echo "Готово: docs/jetbrains-db-analysis.md"
