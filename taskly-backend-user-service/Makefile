MVN = ./mvnw
JAR = target/taskly-user-service.jar

.PHONY: env secret-key dev build run test verify clean

env:
	@test -f .env || { cp .env.example .env && echo "Created .env from .env.example"; }

secret-key:
	@openssl rand -base64 32

dev:
	$(MVN) spring-boot:run

build:
	$(MVN) -DskipTests package

run: build
	java -jar $(JAR)

test:
	$(MVN) test

verify:
	$(MVN) verify

clean:
	$(MVN) clean
