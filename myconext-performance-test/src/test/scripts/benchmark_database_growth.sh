#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
LOG_FILE="${SCRIPT_DIR}/benchmark_database_growth.log"

if [[ -n "${BENCHMARK_SIZES:-}" ]]; then
  IFS=',' read -r -a BENCHMARK_SIZES_ARRAY <<< "${BENCHMARK_SIZES}"
else
  BENCHMARK_SIZES_ARRAY=(1000 10000 100000 1000000 2000000 4000000)
fi

BENCHMARK_USERS_PER_RUN="${BENCHMARK_USERS_PER_RUN:-100}"
BENCHMARK_RAMP_SECONDS="${BENCHMARK_RAMP_SECONDS:-30}"
SIMULATION_CLASS="myconext.simulations.DatabaseGrowthBenchmarkSimulation"

cd "${PROJECT_ROOT}"

timestamp() {
  date +"%Y-%m-%d %H:%M:%S"
}

log() {
  echo "[$(timestamp)] $*" | tee -a "${LOG_FILE}"
}

: > "${LOG_FILE}"

log "Starting database growth benchmark series"
log "Running mvn clean"
mvn clean | tee -a "${LOG_FILE}"

for size in "${BENCHMARK_SIZES_ARRAY[@]}"; do
  log ""
  log "=============================="
  log "Benchmark size: ${size} users"
  log "=============================="

  (
    cd "${SCRIPT_DIR}"
    TOTAL_USERS="${size}" \
    DROP_EXISTING=true \
    NON_INTERACTIVE=true \
    bash ./seed_script.sh
  ) 2>&1 | tee -a "${LOG_FILE}"

  mvn gatling:test \
    -Dgatling.simulationClass="${SIMULATION_CLASS}" \
    -Damountusers="${size}" \
    -DbenchmarkUsers="${BENCHMARK_USERS_PER_RUN}" \
    -DbenchmarkRampSeconds="${BENCHMARK_RAMP_SECONDS}" | tee -a "${LOG_FILE}"
done

log ""
log "Benchmark series completed"
log "Open: ${PROJECT_ROOT}/target/gatling/compare.html"
log "Log file: ${LOG_FILE}"
