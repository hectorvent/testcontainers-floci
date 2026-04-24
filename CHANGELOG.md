# [2.4.0](https://github.com/floci-io/testcontainers-floci/compare/v2.3.0...v2.4.0) (2026-04-24)


### Features

* added missing configuration options and services from Floci v1.5.7 ([aebc8e9](https://github.com/floci-io/testcontainers-floci/commit/aebc8e9f87506b7c0b01571eb05455534928722a))



# [2.3.0](https://github.com/floci-io/testcontainers-floci/compare/v2.2.0...v2.3.0) (2026-04-23)


### Features

* added support for ElastiCache, OpenSearch, ECS and ECR. ([0f5c31a](https://github.com/floci-io/testcontainers-floci/commit/0f5c31ad6760896ef0a382c3567be62465b13123))



# [2.2.0](https://github.com/floci-io/testcontainers-floci/compare/v2.1.0...v2.2.0) (2026-04-23)


### Features

* added configuration support for default account id and default availability zone ([4739338](https://github.com/floci-io/testcontainers-floci/commit/473933817ce66e102ba9ee26cc040484d4854357))
* added support for configuring all services supported by Floci that don't startup child containers ([4b4c41f](https://github.com/floci-io/testcontainers-floci/commit/4b4c41f95da3081d8e245cbfe3a28a34c4f2de4a))
* floci docker image moved ([0203051](https://github.com/floci-io/testcontainers-floci/commit/0203051708671897bf4b010041967efa5a074d2d))



# [2.1.0](https://github.com/floci-io/testcontainers-floci/compare/v2.0.1...v2.1.0) (2026-04-11)


### Features

* added testcases for all services that are available in Floci but were not covered yet ([59fc3bc](https://github.com/floci-io/testcontainers-floci/commit/59fc3bc8f7c9a53a860074b280316126d01858e9))
* **lambda:** added support for lambdas ([4ec27cd](https://github.com/floci-io/testcontainers-floci/commit/4ec27cd60c7b5905e72d4a1477319752e26a5d09))
* **logging:** allow configuration of Floci's log level ([1d9fa78](https://github.com/floci-io/testcontainers-floci/commit/1d9fa7863d59a17e1414aef9ebf1c04357d9392b))
* **network:** added support for creating a dedicated Docker network for Floci and all its child containers ([8bae181](https://github.com/floci-io/testcontainers-floci/commit/8bae181ebdd4ee0c220bd2bae13936d6e0bea90c))
* **rds:** added support for creating and accessing RDS instances ([366a732](https://github.com/floci-io/testcontainers-floci/commit/366a7329c86d852ed8c5eb3b06ead15731106d2c))
* use Floci's health check endpoint to consider the container to be started up ([bbbdac4](https://github.com/floci-io/testcontainers-floci/commit/bbbdac4bb8cb344d79c2cc2ed3b4d47705dfd6a3))



## [2.0.1](https://github.com/floci-io/testcontainers-floci/compare/v2.0.0...v2.0.1) (2026-04-03)


### Bug Fixes

* do auto-configuration for S3Client only if AWS S3 sdk dependency is on classpath ([45ca8b3](https://github.com/floci-io/testcontainers-floci/commit/45ca8b399122ecb778115161cd446b469e3779ef))



