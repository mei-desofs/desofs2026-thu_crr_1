#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_DIR="$PROJECT_ROOT/backups"

if [[ $# -lt 1 ]]; then
  echo "Error: Missing date argument. Usage: ./$(basename "$0") YYYY-MM-DD (e.g., ./$(basename "$0") 2026-04-01)" >&2
  exit 1
fi

CUTOFF_DATE="$1"

if [[ ! "$CUTOFF_DATE" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]]; then
  echo "Invalid date format. Use YYYY-MM-DD." >&2
  exit 1
fi

CUTOFF_TIMESTAMP="${CUTOFF_DATE//-/}000000"

shopt -s nullglob
removed_count=0

for backup_file in "$BACKUP_DIR"/products_backup_*.csv; do
  file_name="$(basename "$backup_file")"
  file_timestamp="${file_name#products_backup_}"
  file_timestamp="${file_timestamp%.csv}"

  if [[ "$file_timestamp" < "$CUTOFF_TIMESTAMP" ]]; then
    rm -f "$backup_file"
    removed_count=$((removed_count + 1))
  fi
done

printf 'Removed %d backup(s) before %s\n' "$removed_count" "$CUTOFF_DATE"
