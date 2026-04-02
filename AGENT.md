# AGENT.md

## Project

Testcontainers module for [Floci](https://github.com/hectorvent/floci) — a local AWS emulator (port 4566, 19+ services).

## Structure

- `testcontainers-floci/` — Core module: `FlociContainer` extending `GenericContainer`
- `spring-boot-testcontainers-floci/` — Spring Boot 4 + Spring Cloud AWS 4 integration via `@ServiceConnection`

## Build

```
mvn verify
```

Requires Docker running for integration tests.

## Key Tech

- Java 17, Maven multi-module
- Testcontainers 2.x, Spring Boot 4.x, Spring Cloud AWS 4.x
- Conventional commits → softprops/action-gh-release for versioning
- Publishes to Maven Central (GPG signed)

## Conventions

- Use conventional commits (`feat:`, `fix:`, `chore:`, etc.)
- Tests use a shared singleton `FlociContainer` via `AbstractFlociContainerServiceTest`
- Spring Boot auto-config registered in `spring.factories` and `AutoConfiguration.imports`
