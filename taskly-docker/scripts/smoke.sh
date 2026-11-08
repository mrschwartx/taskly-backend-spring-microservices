#!/bin/sh
set -eu

BASE_URL="${BASE_URL:-http://localhost:${PORT:-8080}}"
API="$BASE_URL/api/v1"
SUFFIX="$(date +%s)"
USERNAME="smoke_$SUFFIX"
EMAIL="smoke_$SUFFIX@mail.com"
PASSWORD="SmokePassword123"
CORRELATION_ID="smoke-$SUFFIX-0001"
TASK='{"name":"Smoke task","description":"end to end","priority":"low","due":"2030-01-01T00:00:00.000Z","status":"todo"}'
JAR="$(mktemp)"
trap 'rm -f "$JAR"' EXIT

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

expect_status() {
  expected="$1"
  shift
  actual="$(curl -s -o /dev/null -w '%{http_code}' "$@")"
  [ "$actual" = "$expected" ] || fail "expected status $expected but got $actual for: $*"
}

task_count() {
  curl -s -H "Cookie: taskly_token=$TOKEN" "$API/tasks/user/$USER_ID" | sed -n 's/.*"taskCount":\([0-9]*\).*/\1/p'
}

curl -fsS "$BASE_URL/" > /dev/null
expect_status 404 "$BASE_URL/api/v1/unknown"
expect_status 401 "$API/tasks/user/00000000-0000-0000-0000-000000000000"

USER_JSON="$(curl -fsS -c "$JAR" -X POST "$API/auth/signup" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")"
USER_ID="$(printf '%s' "$USER_JSON" | sed -n 's/.*"_id":"\([^"]*\)".*/\1/p')"
[ -n "$USER_ID" ] || fail "signup did not return a user id"
TOKEN="$(awk '$6 == "taskly_token" { print $7 }' "$JAR")"
[ -n "$TOKEN" ] || fail "signup did not set the auth cookie"

expect_status 200 -b "$JAR" -X POST "$API/tasks/create" -H 'Content-Type: application/json' -d "$TASK"
[ "$(task_count)" = "1" ] || fail "expected one task after creation"
expect_status 200 -b "$JAR" "$API/users/$USER_ID"

expect_status 200 -b "$JAR" -X DELETE "$API/users/delete/$USER_ID" -H "X-Correlation-Id: $CORRELATION_ID"

attempt=0
count="$(task_count)"
while [ "$count" != "0" ] && [ "$attempt" -lt 30 ]; do
  attempt=$((attempt + 1))
  sleep 1
  count="$(task_count)"
done
[ "$count" = "0" ] || fail "tasks were not removed after the user was deleted"

expect_status 401 -H "Cookie: taskly_token=$TOKEN" -X POST "$API/tasks/create" -H 'Content-Type: application/json' -d "$TASK"

if command -v docker > /dev/null 2>&1 && docker inspect taskly-gateway > /dev/null 2>&1; then
  for container in taskly-gateway taskly-user-service taskly-task-service; do
    docker logs "$container" 2>&1 | grep -q "$CORRELATION_ID" || fail "correlation id $CORRELATION_ID not found in $container logs"
  done
fi

echo "Smoke test passed for $USERNAME"
