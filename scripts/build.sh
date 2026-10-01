#!/usr/bin/env bash
# Сборка: ./scripts/build.sh [IDE_HOME]   (SKIP_TESTS=1, SKIP_RESEARCH=1, INTELLIJ_COMMUNITY_HOME=...)
source "$(dirname "$0")/common.sh"
step "1/5 JDK"; assert_jdk
step "2/5 JetBrains IDE / DataGrip"; find_ide "${1:-}"
step "3/5 Database Tools и совместимость"; run_research --check >/dev/null || true; assert_compatibility
if [[ -z "${SKIP_RESEARCH:-}" ]]; then
  step "4/5 Research"; args=(); [[ -n "${INTELLIJ_COMMUNITY_HOME:-}" ]] && args=(--community "$INTELLIJ_COMMUNITY_HOME")
  run_research "${args[@]}"
fi
step "5/5 Gradle"; gradle_args_base
GRADLE_ARGS+=(:opendata:integration:buildPlugin); [[ -z "${SKIP_TESTS:-}" ]] && GRADLE_ARGS+=(:opendata:integration:test)
run_gradle "${GRADLE_ARGS[@]}"
echo "BUILD OK"
