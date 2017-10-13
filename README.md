# OpenConext-attribute-aggregation

[![Build Status](https://travis-ci.org/OpenConext/OpenConext-attribute-aggregation.svg)](https://travis-ci.org/OpenConext/OpenConext-attribute-aggregation)
[![codecov.io](https://codecov.io/github/OpenConext/OpenConext-attribute-aggregation/coverage.svg)](https://codecov.io/github/OpenConext/OpenConext-attribute-aggregation)

OpenConext Attribute Aggregation

## [Getting started](#getting-started)

### [System Requirements](#system-requirements)

- Java 8
- Maven 3
- MySQL 5.5
- npm
- node 7.10.0 (use for example nvm to manage it - latest version of node does not work)

If you have yarn installed, it will be used in the build by 3rd party libs. Ensure you are on versin 1.1.0, otherwise
the sass node will break.

### [Create database](#create-database)

Connect to your local mysql database: `mysql -uroot`

Execute the following:

```sql
CREATE DATABASE aaserver;
grant all on aaserver.* to 'root'@'localhost';
```

## [Building and running](#building-and-running)

### [The aa-server](#aa-server)

This project uses Spring Boot and Maven. To run locally, type:

`cd aa-server`

`mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=dev"`

When developing, it's convenient to just execute the applications main-method, which is in [Application](aa-server/src/main/java/aa/Application.java). Don't forget
to set the active profile to dev.

### [The aa-gui](#aa-gui)

The client is build with react.js and to get initially started:

`cd aa-gui`

To run locally:

`npm run local`

Browse to the [application homepage](http://localhost:8000/).

When new npm dependencies are added:

`npm install`

## [Miscellaneous](#miscellaneous)

### [Testing](#testing)

When manually testing the aggregations in the Playground you have to provide input attributes for retrieving values from the attribute authorities.

* The attribute `urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified` with the value `urn:collab:person:example.com:admin` returns groups / isMemberOfs from VOOT
* The attribute `urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified` with the value `urn:collab:person:surfnet.nl:henny` returns SAB roles
* The attribute `urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified` with value `urn:collab:person:example.com:admin` returns a valid ORCID.

### [cUrl](#curl)

To mimic the behaviour of attribute aggregation for an internal client - e.g. EngineBlock - we need to post form data:

```bash
curl -X POST -H "Content-Type: application/json" --data-binary @./aa-server/src/test/resources/json/eb/request.json -u eb:secret https://aa.test2.surfconext.nl/aa/api/internal/attribute/aggregation
```

if you want to test all of the above curl commands against your locally running AttributeAggregation application then replace `https://aa.test2.surfconext.nl` with `http://localhost:8080`.

There is also an API for trusted clients to obtain account information based on the urn of the person:

```bash
curl -u eb:secret https://aa.test2.surfconext.nl/aa/api/internal/accounts/urn:collab:person:example.com:admin
```

And the API offers a end-point to delete accounts:

```bash
curl -u eb:secret -X "DELETE" "https://aa.test2.surfconext.nl/aa/api/internal/disconnect/${account_id}
```

### [New Attribute Authority](#new-attribute-authority)

New Attribute Authorities first must be added and configured in `attributeAuthoritiesProductionTemplate.yml`. Then add the new authority implementation to `AttributeAggregatorConfiguration#attributeAggregatorById`.

To actually use the new authority in the test/acc/prod environment it also needs to be configured in OpenConext-deploy [attributeAuthorities.yml.j2](https://github.com/OpenConext/OpenConext-deploy/blob/master/roles/attribute-aggregation-server/templates/attributeAuthorities.yml.j2).

### [Configuration and deployment](#configuration-and-deployment)

On its classpath, the application has an [application.yml](aa-server/src/main/resources/application.yml) file that
contains configuration defaults that are convenient when developing.

When the application actually gets deployed to a meaningful platform, it is pre-provisioned with ansible and the application.yml depends on
environment specific properties in the group_vars. See the project OpenConext-deploy and the role aa for more information.

For details, see the [Spring Boot manual](http://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/).

When you want to run Attribute-Aggregator in a non-OpenConext environment you can use the [aa](aa-server/scripts/aa) script to stop / restart and start the application.
