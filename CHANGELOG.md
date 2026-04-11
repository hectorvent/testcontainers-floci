# [1.1.0](https://github.com/floci-io/testcontainers-floci/compare/v1.0.1...v1.1.0) (2026-04-11)


### Features

* added testcases for all services that are available in Floci but were not covered yet ([c2677b5](https://github.com/floci-io/testcontainers-floci/commit/c2677b56e4802498d5e58dd6e038a7b3e99b8121))
* **lambda:** added support for lambdas ([c10a1d2](https://github.com/floci-io/testcontainers-floci/commit/c10a1d22b85e64a3995791d083fbc43ecd596940))
* **logging:** allow configuration of Floci's log level ([ced1d04](https://github.com/floci-io/testcontainers-floci/commit/ced1d041bbf174457f36fd6ba3ba2a95919deca0))
* **network:** added support for creating a dedicated Docker network for Floci and all its child containers ([a720af2](https://github.com/floci-io/testcontainers-floci/commit/a720af23845a034a59678952e2a64c0d83e040de))
* **rds:** added support for creating and accessing RDS instances ([4c456d8](https://github.com/floci-io/testcontainers-floci/commit/4c456d8193b1b0045c98b1c917ae332c10e43d58))
* use Floci's health check endpoint to consider the container to be started up ([adbe079](https://github.com/floci-io/testcontainers-floci/commit/adbe079bf7d080afefa4a06edf69b865abdbc290))



## [1.0.1](https://github.com/floci-io/testcontainers-floci/compare/v1.0.0...v1.0.1) (2026-04-03)


### Bug Fixes

* do auto-configuration for S3Client only if AWS S3 sdk dependency is on classpath ([b59a432](https://github.com/floci-io/testcontainers-floci/commit/b59a432b0fff8c0fb98ab21a18967b7df068f291))



# [1.0.0](https://github.com/floci-io/testcontainers-floci/compare/5f1305cb5823ff6fe90793df1fa2682ad204e961...v1.0.0) (2026-04-03)


### Bug Fixes

* commitlint should not fail-on-error ([085cc1b](https://github.com/floci-io/testcontainers-floci/commit/085cc1b8e715fa5224e0d1a6c28f6d6bd033e7a6))


### Features

* initial implementation of testcontainers-floci ([5f1305c](https://github.com/floci-io/testcontainers-floci/commit/5f1305cb5823ff6fe90793df1fa2682ad204e961))
* updated version to 2.x to show compatibility to Spring Boot 4.0.x / Spring Cloud AWS 4.0.x / Testcontainers 2.x ([57e840e](https://github.com/floci-io/testcontainers-floci/commit/57e840e462055d7b86b643c48ccd8bdb6cd3a00e))



