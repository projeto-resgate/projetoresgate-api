#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${ENV_FILE:-$DIR/.env}"
DB_CONTAINER="${DB_CONTAINER:-projetoresgate_db}"
DB_NAME="${DB_NAME:-projetoresgatedb}"

read_env() {
    grep -E "^$1=" "$ENV_FILE" 2>/dev/null | head -n1 | cut -d= -f2- || true
}

DB_USERNAME="${DB_USERNAME:-$(read_env DB_USERNAME)}"
BACKUP_REMOTE="${BACKUP_REMOTE:-$(read_env BACKUP_REMOTE)}"
BACKUP_DIR="${BACKUP_DIR:-$(read_env BACKUP_DIR)}"
BACKUP_KEEP="${BACKUP_KEEP:-$(read_env BACKUP_KEEP)}"
BACKUP_REMOTE_KEEP_DAYS="${BACKUP_REMOTE_KEEP_DAYS:-$(read_env BACKUP_REMOTE_KEEP_DAYS)}"

BACKUP_DIR="${BACKUP_DIR:-$HOME/backups}"
BACKUP_KEEP="${BACKUP_KEEP:-7}"
BACKUP_REMOTE_KEEP_DAYS="${BACKUP_REMOTE_KEEP_DAYS:-14}"

if [ -z "$DB_USERNAME" ]; then
    echo "DB_USERNAME não encontrado em $ENV_FILE" >&2
    exit 1
fi

mkdir -p "$BACKUP_DIR"
STAMP="$(date +%F_%H%M%S)"
OUT="$BACKUP_DIR/projetoresgate_$STAMP.sql.gz"
TMP="$OUT.partial"
trap 'rm -f "$TMP"' EXIT

docker exec "$DB_CONTAINER" pg_dump -U "$DB_USERNAME" --no-owner --no-privileges "$DB_NAME" | gzip > "$TMP"
gzip -t "$TMP"

if [ "$(stat -c %s "$TMP")" -lt 1024 ]; then
    echo "Backup suspeito: arquivo menor que 1 KB" >&2
    exit 1
fi

mv "$TMP" "$OUT"
echo "$(date '+%F %T') backup criado: $OUT ($(du -h "$OUT" | cut -f1))"

ls -1t "$BACKUP_DIR"/projetoresgate_*.sql.gz | tail -n +"$((BACKUP_KEEP + 1))" | xargs -r rm --

if [ -n "$BACKUP_REMOTE" ]; then
    rclone copy "$OUT" "$BACKUP_REMOTE"
    echo "$(date '+%F %T') enviado para $BACKUP_REMOTE"
    rclone delete "$BACKUP_REMOTE" --include "projetoresgate_*.sql.gz" --min-age "${BACKUP_REMOTE_KEEP_DAYS}d"
fi

