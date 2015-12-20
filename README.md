# OpenConext-attribute-aggregation

[![Build Status](https://travis-ci.org/oharsta/OpenConext-attribute-aggregation.svg)](https://travis-ci.org/oharsta/OpenConext-attribute-aggregation)
[![codecov.io](https://codecov.io/github/oharsta/OpenConext-attribute-aggregation/coverage.svg)](https://codecov.io/github/oharsta/OpenConext-attribute-aggregation)

OpenConext Attribute Aggregation

## Getting started

### System Requirements

- Java 8
- Maven 3
- MySQL 5.5
- node.js

### Create database

Connect to your local mysql database: `mysql -uroot`

Execute the following:

```sql
CREATE DATABASE aaserver DEFAULT CHARACTER SET latin1;
grant all on aaserver.* to 'root'@'localhost';
```

## Building and running

### The aa-server

This project uses Spring Boot and Maven. To run locally, type:

`cd aa-server`

`mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=dev"`

When developing, it's convenient to just execute the applications main-method, which is in [Application](aa-server/src/main/java/aa/Application.java). Don't forget
to set the active profile to dev.

### The aa-gui

The client is build with react.js and to get initially started:

`cd aa-gui`

`brew install npm;`

To run locally:

`npm start`

Browse to the [application homepage](http://localhost:8000/).

When new npm dependencies are added:

`npm install`

## Testing

`cd aa-gui && npm test`
`cd aa-server && mvn test`

### Service Registry

The aa-server needs to access the metadata of Service providers from the Service Registry. In production modus the content is read (and periodically refreshed) from:
  
* https://tools.surfconext.nl/export/saml20-sp-remote.json

In any other modus the content is read from the file system:

* [saml20-sp-remote.json](aa-server/src/main/resources/service-registry/saml20-sp-remote.json)

To sync the data of the file system with the actual production data of `https://tools.surfconext.nl` run the [refreshEntityMetadata](aa-server/scripts/refreshEntityMetadata.sh) script.

### Configuration and deployment

On its classpath, the application has an [application.properties](aa-server/src/main/resources/application.properties) file that
contains configuration defaults that are convenient when developing.

When the application actually gets deployed to a meaningful platform, it is pre-provisioned with ansible and the application.properties depends on
environment specific properties in the group_vars. See the project OpenConext-deploy and the role aa for more information.

For details, see the [Spring Boot manual](http://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/).
