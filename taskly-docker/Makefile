COMPOSE = docker compose -f docker-compose.local.yml
COMPOSE_OBS = docker compose -f docker-compose.local.yml --profile observability
COMPOSE_DEV = docker compose -f docker-compose.dev.yml

.PHONY: docker/env docker/up docker/up-obs docker/down docker/logs docker/ps docker/seed docker/smoke docker/clean docker/dev-up docker/dev-down docker/dev-logs

docker/env:
	@test -f .env || { \
		sed -e "s|^USER_DB_PASSWORD=.*|USER_DB_PASSWORD=$$(openssl rand -hex 16)|" \
			-e "s|^TASK_DB_PASSWORD=.*|TASK_DB_PASSWORD=$$(openssl rand -hex 16)|" \
			-e "s|^AUTH_SECRET=.*|AUTH_SECRET=$$(openssl rand -hex 32)|" \
			.env.example > .env && echo "Created .env with generated secrets"; \
	}

docker/up: docker/env
	$(COMPOSE) up -d --build --wait --wait-timeout 300

docker/up-obs: docker/env
	TRACING_ENABLED=true $(COMPOSE_OBS) up -d --build --wait --wait-timeout 300

docker/down:
	$(COMPOSE_OBS) down

docker/logs:
	$(COMPOSE) logs -f taskly-gateway taskly-user-service taskly-task-service

docker/ps:
	$(COMPOSE_OBS) ps

docker/seed:
	@set -a; . ./.env; set +a; sh scripts/seed.sh

docker/smoke:
	@set -a; . ./.env; set +a; sh scripts/smoke.sh

docker/clean:
	$(COMPOSE_OBS) down -v --rmi local

docker/dev-up:
	$(COMPOSE_DEV) up -d --wait

docker/dev-down:
	$(COMPOSE_DEV) down

docker/dev-logs:
	$(COMPOSE_DEV) logs -f
