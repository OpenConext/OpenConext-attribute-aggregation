# OpenConext-attribute-aggregation

[![Build Status](https://travis-ci.org/OpenConext/OpenConext-attribute-aggregation.svg)](https://travis-ci.org/OpenConext/OpenConext-attribute-aggregation)
[![codecov.io](https://codecov.io/github/OpenConext/OpenConext-attribute-aggregation/coverage.svg)](https://codecov.io/github/OpenConext/OpenConext-attribute-aggregation)

OpenConext Attribute Aggregation

## [Getting started](#getting-started)

### [System Requirements](#system-requirements)

- Java 11
- Maven 3
- MySQL 5.5
- npm
- node 7.10.0 (use for example nvm to manage it - latest version of node does not work)

If you have yarn installed, it will be used in the build by 3rd party libs. Ensure you are on version 1.1.0, otherwise
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

Browse to the [application homepage](http://localhost:8001/).

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
curl -u eb:secret -X "DELETE" "https://aa.test2.surfconext.nl/aa/api/internal/disconnect/${account_id}"
```

Which will return Json `{"status": "OK"}` on success.

### [Orcid](#orcid)

You can locally test the account linking with ORCID. You will need a valid orcid client id and secret. Copy & paste
the application.yml to application.local.yml and fill in the properties `orcid.client_id` and `orcid.secret`. Then use
this condiguration to start the server application:

`mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=dev" -Dspring.config.name=application.dev`

If you go to the [connected page](http://localhost:8080/aa/api/client/information.html?redirectUrl=https://www.google.com) you
can link the dummy institutional user provided by the `MockShibbolethFilter` with an ORCID account.

If you don't specify a redirectUrl, then you will be redirected to the [information page](http://localhost:8080/aa/api/client/connected.html).

### [New Attribute Authority](#new-attribute-authority)

New Attribute Authorities first must be added and configured in `attributeAuthoritiesProductionTemplate.yml`. Then add the new authority implementation to `AttributeAggregatorConfiguration#attributeAggregatorById`.

To actually use the new authority in the test/acc/prod environment it also needs to be configured in OpenConext-deploy [attributeAuthorities.yml.j2](https://github.com/OpenConext/OpenConext-deploy/blob/master/roles/attribute-aggregation-server/templates/attributeAuthorities.yml.j2).

#### REST attribute aggregator
This reusable aggregator can be used for retrieving data from a REST endpoint and supports various options for configuring a HTTP request to that endpoint e.g. an API key.
Using this type does not require adding an authority implementation.

New entries are added in attributeAuthorities.yml.

Below is an example of the full configuration with explanations for the options:

    - {
        id: "<id>",
        description: "<description>",
        endpoint: "<endpoint>",
        type: "rest",
        // Username for basic authentication
        user: "",
        // Password for basic authentication
        password: "",
        // Headers to add in the HTTP request
        headers: [
            {
              "key": "<key>",
              "value": "<value>",
            }
        ],
        // Path parameters to use in the value for 'endpoint'
        // Wildcards can be added with %s e.g. https://endpoint/%s/subpath/%s...
        // The index below will correspond to the order in which the wildcards are replaced
        // sourceAttribute refers to the attribute received from EngineBlock to use as the substitute
        pathParams: [
        {
            "index": 0,
            "sourceAttribute": "urn:mace:terena.org:attribute-def:schacHomeOrganization"
        },
        {
            "index": 1,
            "sourceAttribute": "urn:mace:dir:attribute-def:uid"
        }
        ],
        // Options are 'GET', 'POST', 'PUT', 'DELETE', default is 'GET'
        requestMethod: 'GET',
        // Request parameters to use in the HTTP request, will be appended as ?name=value&...
        // sourceAttribute refers to the attribute received from EngineBlock to use as the substitute
        requestParams: [
            {
                "name": "<name>",
                "sourceAttribute": "urn:mace:terena.org:attribute-def:schacHomeOrganization"
            }
        ],x
        // Optional: specify node from API response for which to apply mapping for, by default the root node is used.
        // Nesting is possible using dot notation e.g field.nestedField1[0].nestedField2 etc. Any paths navigations
        // are possible from GPath
        rootListName: "<node name>",
        // Mapping to apply to the response received from the HTTP request
        // responseKey corresponds to the field in the response object of which to retrieve the value
        // targetAttribute corresponds to the attribute to send the value as in the result of aggregation
        mappings: [
        {
            "responseKey": "myResponseKey",
            "targetAttribute": "myTargetAttribute"
            // Optional: filter to search for specific node relative to the node used as root 
            // (after applying rootListName if present). Useful in case of searching for value 
            // where other field equals another value. 
            "filter": {
                "key": "<key>",
                "value": "<value>"
            }
        }
        ],
        timeOut: 15000,
        attributes: [],
        requiredInputAttributes: [
            {
                name: "urn:mace:terena.org:attribute-def:schacHomeOrganization",
            }
        ],
        validationRegExp: "[a-zA-Z0-9]*",
        // Optional: specify caching which will be used in case an error occurs when retrieving data.
        // Cache endpoint is configured separately but should return the same response structure as the primary endpoint.
        cache: {
            enabled: true,
            endpoint: "<endpoint>",
            headers: [
                {
                  "key": "<key>",
                  "value": "<value>",
                }
            ],
            // Specify path to relevant record. Caution: if an entire list is retrieved then it is advised to 
            // define a filter to the relevant record e.g. findAll{record->record.%s == \"%s\"}[0]. In this example
            // attributes are used from the 'filters' section so that it is possible to filter by properties. Filters
            // are optional. Path navigations and filter are possible from GPath
            rootListName: "<node name>", 
            filters: {
                index: 0,
                key: "<key>",
                sourceAttribute: "<attribute name>"
            },
            requestMethod: 'GET',
            // Specify frequency at which data is retrieved from the cache endpoint
            refreshCron: "* * * * * *"
        }
    }

### [Configuration and deployment](#configuration-and-deployment)

On its classpath, the application has an [application.yml](aa-server/src/main/resources/application.yml) file that
contains configuration defaults that are convenient when developing.

When the application actually gets deployed to a meaningful platform, it is pre-provisioned with ansible and the application.yml depends on
environment specific properties in the group_vars. See the project OpenConext-deploy and the role aa for more information.

For details, see the [Spring Boot manual](http://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/).

When you want to run Attribute-Aggregator in a non-OpenConext environment you can use the [aa](aa-server/scripts/aa) script to stop / restart and start the application.

### [LifeCycle Deprovisioning](#life-cycle-deprovisioning)

There is a LifeCycle API to deprovision users. The preview endpoint:
```
curl -u life:secret http://localhost:8080/aa/api/deprovision/saml2_user.com | jq
```
And the actual `Deprovisioning` of the user:
```
curl -X DELETE -u life:secret http://localhost:8080/deprovision/aa/api/saml2_user.com | jq
```
