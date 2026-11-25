#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ENV_FILE:-$DIR/.env}"
DB_CONTAINER="${DB_CONTAINER:-projetoresgate_db}"
API_CONTAINER="${API_CONTAINER:-projetoresgate_api}"
DB_NAME="${DB_NAME:-projetoresgatedb}"

FILE="${1:?uso: ./restore.sh <arquivo.sql.gz>}"
[ -f "$FILE" ] || { echo "Arquivo não encontrado: $FILE" >&2; exit 1; }
gzip -t "$FILE"

read_env() {
    grep -E "^$1=" "$ENV_FILE" 2>/dev/null | head -n1 | cut -d= -f2- || true
}

DB_USERNAME="${DB_USERNAME:-$(read_env DB_USERNAME)}"

echo "Isto APAGA o banco $DB_NAME e restaura o conteúdo de $FILE."
read -rp "Digite 'restaurar' para continuar: " ANSWER
[ "$ANSWER" = "restaurar" ] || { echo "Cancelado."; exit 1; }

API_WAS_RUNNING=0
if [ -n "$(docker ps -q -f "name=^${API_CONTAINER}$")" ]; then
    docker stop "$API_CONTAINER" >/dev/null
    API_WAS_RUNNING=1
fi

docker exec "$DB_CONTAINER" psql -U "$DB_USERNAME" -d postgres -v ON_ERROR_STOP=1 \
    -c "DROP DATABASE IF EXISTS \"$DB_NAME\" WITH (FORCE)" \
    -c "CREATE DATABASE \"$DB_NAME\""

gunzip -c "$FILE" | docker exec -i "$DB_CONTAINER" psql -U "$DB_USERNAME" -d "$DB_NAME" -v ON_ERROR_STOP=1 -q

if [ "$API_WAS_RUNNING" -eq 1 ]; then
    docker start "$API_CONTAINER" >/dev/null
fi

echo "Restauração concluída."
