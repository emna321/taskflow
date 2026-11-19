#!/usr/bin/env bash
# Construit taskflow.jar : build Angular -> backend/src/main/resources/static, puis package Spring Boot.
set -euo pipefail
cd "$(dirname "$0")"
echo "==> Build Angular (production)"
(cd frontend && npm install && npx ng build)
echo "==> Build Spring Boot + tests"
(cd backend && mvn clean package)
echo "==> OK : backend/target/taskflow.jar"
