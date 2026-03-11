#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# dev-run.sh — Load .env and start the Spring Boot backend locally
#
# Usage:  ./dev-run.sh
#
# This script:
#  1. Looks for .env in the same directory (copy from .env.example first)
#  2. Exports all KEY=VALUE lines as environment variables
#  3. Runs the Spring Boot app with the dev profile
# ─────────────────────────────────────────────────────────────────────────────

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

if [ ! -f "$ENV_FILE" ]; then
  echo "❌  .env not found at $ENV_FILE"
  echo "    Run:  cp .env.example .env  and fill in your values."
  exit 1
fi

# Export all non-comment, non-empty lines from .env
set -a
# shellcheck source=/dev/null
source "$ENV_FILE"
set +a

echo "✅  Loaded .env (DB_HOST=${DB_HOST:-localhost}, DB_USER=$DB_USER, DB_NAME=$DB_NAME)"

# Default to localhost when running outside Docker
export DB_HOST="${DB_HOST:-127.0.0.1}"
export DB_PORT="${DB_PORT:-3306}"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"

echo "🚀  Starting Spring Boot (profile: $SPRING_PROFILES_ACTIVE) ..."
cd "$SCRIPT_DIR"
./mvnw spring-boot:run
