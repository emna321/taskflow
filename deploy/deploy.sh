#!/usr/bin/env bash
# Envoie taskflow.jar sur le VPS et redemarre le service.
# Usage : ./deploy/deploy.sh utilisateur@IP_DU_VPS
set -euo pipefail
VPS="${1:?Usage : ./deploy/deploy.sh utilisateur@IP_DU_VPS}"
JAR="backend/target/taskflow.jar"
[ -f "$JAR" ] || { echo "Lancez d'abord ./build.sh"; exit 1; }
scp "$JAR" "$VPS:/tmp/taskflow.jar"
ssh "$VPS" 'sudo mv /tmp/taskflow.jar /opt/taskflow/taskflow.jar && sudo chown taskflow:taskflow /opt/taskflow/taskflow.jar && sudo systemctl restart taskflow && sleep 3 && sudo systemctl --no-pager status taskflow | head -5'
