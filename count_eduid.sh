#!/usr/bin/env bash
# Count occurrences of "eduid" (any capitalisation) in the whole project.
# Usage: ./count_eduid.sh [path]
set -euo pipefail

root="${1:-.}"

# Per-file counts
echo "== Occurrences per file =="
grep -rnoIi 'eduid' --exclude-dir=node_modules --exclude-dir=target --exclude-dir=.git "$root" | cut -d: -f1 | sort | uniq -c | sort -rn

# Total
total=$(grep -rhoIi 'eduid' --exclude-dir=node_modules --exclude-dir=target --exclude-dir=.git "$root" | wc -l)
echo
echo "Total occurrences: $total"
