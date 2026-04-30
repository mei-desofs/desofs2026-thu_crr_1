#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

: "${DB_URL:?DB_URL is required}"
: "${POSTGRES_USER:?POSTGRES_USER is required}"
: "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}"
: "${POSTGRES_DB:?POSTGRES_DB is required}"

OUTPUT_DIR="$PROJECT_ROOT/backups"
mkdir -p "$OUTPUT_DIR"

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
PRETTY_DATE="$(date +"%Y-%m-%d %H:%M:%S")"
OUTPUT_FILE="$OUTPUT_DIR/products_backup_$TIMESTAMP.csv"

PG_URL="${DB_URL#jdbc:}"

export PGPASSWORD="$POSTGRES_PASSWORD"
export PGUSER="$POSTGRES_USER"
export PGDATABASE="$POSTGRES_DB"

psql "$PG_URL" \
  --no-psqlrc \
  -v ON_ERROR_STOP=1 \
  -c "\\copy (SELECT p.product_name, p.description, p.money_value, c.category_name FROM products p JOIN categories c ON p.category_id = c.id ORDER BY 1) TO STDOUT WITH CSV HEADER" \
  > "$OUTPUT_FILE"

printf 'Backup created on %s!\n' "$PRETTY_DATE"
