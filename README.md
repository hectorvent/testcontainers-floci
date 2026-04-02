# Testcontainers Floci

[![CI](https://github.com/floci-io/testcontainers-floci/actions/workflows/ci.yml/badge.svg)](https://github.com/floci-io/testcontainers-floci/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.floci/testcontainers-floci)](https://central.sonatype.com/artifact/io.floci/testcontainers-floci)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

[Testcontainers](https://testcontainers.com/) module for [Floci](https://github.com/hectorvent/floci) — a free, open-source local AWS emulator.

Floci provides a single Docker container that emulates 19+ AWS services (S3, SQS, DynamoDB, Lambda, and more) on a single endpoint, making it ideal for integration testing.

## Modules

| Module | Description |
|--------|-------------|
| [`testcontainers-floci`](#testcontainers-floci-1) | Core Testcontainers module for starting a Floci container |
| [`spring-boot-testcontainers-floci`](#spring-boot-testcontainers-floci-1) | Spring Boot auto-configuration with `@ServiceConnection` support |

## Requirements

- Java 17+
- Docker

## Version Compatibility

| testcontainers-floci | Spring Boot | Spring Cloud AWS | Testcontainers |
|----------------------|-------------|------------------|----------------|
| **2.x**              | 4.0.x       | 4.0.x            | 2.x            |
| **1.x**              | 3.5.x       | 3.4.x            | 1.x            |

---

## testcontainers-floci

The core module provides a `FlociContainer` class that starts and manages a Floci Docker container for use in integration tests.

### Installation

**Maven:**

```xml
<dependency>
    <groupId>io.floci</groupId>
    <artifactId>testcontainers-floci</artifactId>
    <version>${testcontainers-floci.version}</version>
    <scope>test</scope>
</dependency>
```

**Gradle (Kotlin DSL):**

```kotlin
testImplementation("io.floci:testcontainers-floci:${testcontainersFlociVersion}")
```

**Gradle (Groovy DSL):**

```groovy
testImplementation 'io.floci:testcontainers-floci:${testcontainersFlociVersion}'
```

### Usage

#### Java

```java
import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class S3IntegrationTest {

    @Container
    static FlociContainer floci = new FlociContainer();

    @Test
    void shouldCreateBucket() {
        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(floci.getEndpoint()))
                .region(Region.of(floci.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())))
                .forcePathStyle(true)
                .build();

        s3.createBucket(b -> b.bucket("my-bucket"));

        var buckets = s3.listBuckets().buckets();
        assertThat(buckets).anyMatch(b -> b.name().equals("my-bucket"));
    }
}
```

#### Kotlin

```kotlin
import io.floci.testcontainers.FlociContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Testcontainers
class S3IntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val floci = FlociContainer()
    }

    @Test
    fun `should create bucket`() {
        val s3 = S3Client.builder()
            .endpointOverride(URI.create(floci.getEndpoint()))
            .region(Region.of(floci.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(floci.accessKey, floci.secretKey)
                )
            )
            .forcePathStyle(true)
            .build()

        s3.createBucket { it.bucket("my-bucket") }

        val buckets = s3.listBuckets().buckets()
        assertThat(buckets).anyMatch { it.name() == "my-bucket" }
    }
}
```

### Configuration

| Method | Description | Default |
|--------|-------------|---------|
| `FlociContainer()` | Creates a container with the default image | `hectorvent/floci:latest` |
| `FlociContainer(String)` | Creates a container with a custom image tag | — |
| `withRegion(String)` | Sets the AWS region | `us-east-1` |

### Container Properties

| Method | Description | Default |
|--------|-------------|---------|
| `getEndpoint()` | HTTP endpoint URL (e.g. `http://localhost:32781`) | — |
| `getRegion()` | Configured AWS region | `us-east-1` |
| `getAccessKey()` | AWS access key | `test` |
| `getSecretKey()` | AWS secret key | `test` |

### Supported AWS Services

Floci emulates the following AWS services on a single endpoint:

S3, SQS, SNS, DynamoDB, DynamoDB Streams, KMS, Kinesis, Secrets Manager, IAM, STS, SSM Parameter Store, EventBridge, CloudWatch Logs, CloudWatch Metrics, Cognito, Step Functions, Lambda, API Gateway, CloudFormation, ElastiCache, RDS

> **Note:** Although Floci supports **ElastiCache (Redis)** and **RDS (PostgreSQL/MySQL)**, this library does not yet fully support these services. Contributions are welcome!

---

## spring-boot-testcontainers-floci

This module integrates `FlociContainer` with [Spring Boot](https://spring.io/projects/spring-boot) and [Spring Cloud AWS](https://awspring.io/) via the `@ServiceConnection` annotation. When a `FlociContainer` is declared as a service connection, **all Spring Cloud AWS clients are automatically configured** to use the Floci instance — no manual endpoint, credentials, or region configuration needed.

### What it does

- Produces `AwsConnectionDetails` from `FlociContainer`, which Spring Cloud AWS uses to auto-configure endpoint, region, and credentials on all AWS SDK clients
- Automatically enables S3 path-style access on your `S3Client` (required for `Floci`)

### Installation

**Maven:**

```xml
<dependency>
    <groupId>io.floci</groupId>
    <artifactId>spring-boot-testcontainers-floci</artifactId>
    <version>${testcontainers-floci.version}</version>
    <scope>test</scope>
</dependency>
```

You also need a Spring Cloud AWS starter for the services you want to test, for example:

```xml
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-s3</artifactId>
    <scope>test</scope>
</dependency>
```

**Gradle (Kotlin DSL):**

```kotlin
testImplementation("io.floci:spring-boot-testcontainers-floci:${testcontainersFlociVersion}")
testImplementation("io.awspring.cloud:spring-cloud-aws-starter-s3")
```

**Gradle (Groovy DSL):**

```groovy
testImplementation 'io.floci:spring-boot-testcontainers-floci:${testcontainersFlociVersion}'
testImplementation 'io.awspring.cloud:spring-cloud-aws-starter-s3'
```

### Usage

#### Java

```java
import io.floci.testcontainers.FlociContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class S3IntegrationTest {

    @Container
    @ServiceConnection
    static FlociContainer floci = new FlociContainer();

    @Autowired
    private S3Client s3Client;

    @Test
    void shouldCreateBucket() {
        s3Client.createBucket(b -> b.bucket("my-bucket"));

        var buckets = s3Client.listBuckets().buckets();
        assertThat(buckets).anyMatch(b -> b.name().equals("my-bucket"));
    }
}
```

#### Kotlin

```kotlin
import io.floci.testcontainers.FlociContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.services.s3.S3Client

@SpringBootTest
@Testcontainers
class S3IntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val floci = FlociContainer()
    }

    @Autowired
    private lateinit var s3Client: S3Client

    @Test
    fun `should create bucket`() {
        s3Client.createBucket { it.bucket("my-bucket") }

        val buckets = s3Client.listBuckets().buckets()
        assertThat(buckets).anyMatch { it.name() == "my-bucket" }
    }
}
```

#### Using `@Bean` configuration

You can also declare the container as a `@Bean` in a test configuration class:

**Java:**

```java
@TestConfiguration
class FlociTestConfig {

    @Bean
    @ServiceConnection
    FlociContainer flociContainer() {
        return new FlociContainer();
    }
}
```

**Kotlin:**

```kotlin
@TestConfiguration
class FlociTestConfig {

    @Bean
    @ServiceConnection
    fun flociContainer() = FlociContainer()
}
```

---

## Conventional Commits

This project uses [Conventional Commits](https://www.conventionalcommits.org/). Commit messages determine the release version automatically:

| Prefix | Version bump | Example                                     |
|--------|-------------|---------------------------------------------|
| `fix:` | Patch (0.1.0 → 0.1.1) | `fix: handle null region gracefully`        |
| `feat:` | Minor (0.1.0 → 0.2.0) | `feat: add withServices() configuration`    |
| `feat!:` or `BREAKING CHANGE:` | Major (0.1.0 → 1.0.0) | `feat!: use next Spring Boot major version` |
| `chore:`, `docs:`, `ci:` | No release | `docs: update README examples`              |

## License

This project is licensed under the [MIT License](LICENSE).
