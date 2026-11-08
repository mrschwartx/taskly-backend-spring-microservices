#!/bin/sh
set -eu

if [ "${APP_LEVEL:-development}" = "production" ] && [ "${SEED_ALLOW_PRODUCTION:-false}" != "true" ]; then
  echo "Refusing to seed when APP_LEVEL=production" >&2
  exit 1
fi

BASE_URL="${BASE_URL:-http://localhost:${PORT:-8080}}"
API="$BASE_URL/api/v1"
NOW="$(date -u +%Y-%m-%dT%H:%M:%S.000Z)"
JAR="$(mktemp)"
trap 'rm -f "$JAR"' EXIT

seed_user() {
  username="$1"
  email="$2"
  password="$3"
  task_name="$4"
  task_description="$5"
  task_priority="$6"
  : > "$JAR"
  code="$(curl -s -o /dev/null -w '%{http_code}' -c "$JAR" -X POST "$API/auth/signup" \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"$username\",\"email\":\"$email\",\"password\":\"$password\"}")"
  case "$code" in
    200)
      task_code="$(curl -s -o /dev/null -w '%{http_code}' -b "$JAR" -X POST "$API/tasks/create" \
        -H 'Content-Type: application/json' \
        -d "{\"name\":\"$task_name\",\"description\":\"$task_description\",\"priority\":\"$task_priority\",\"due\":\"$NOW\",\"status\":\"todo\"}")"
      [ "$task_code" = "200" ] || { echo "Creating task for $username failed with status $task_code" >&2; exit 1; }
      echo "Seeded user $username"
      ;;
    422)
      echo "Skipped existing user $username"
      ;;
    *)
      echo "Seeding $username failed with status $code" >&2
      exit 1
      ;;
  esac
}

seed_user "johndoe123" "johndoe123@mail.com" "password123" \
  "Read Atomic Habits" "Finish reading Atomic Habits by James Clear" "low"
seed_user "jane78" "jane@mail.com" "securepassword456" \
  "Learn MERN Stack" "Learn the MERN stack and build a full-stack application with it" "high"
echo "Seeding completed"
