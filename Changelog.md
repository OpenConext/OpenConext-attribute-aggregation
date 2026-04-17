# Release notes

Starting from version 5.0.0, we note changes and new features per release in this file.

## 5.2.0

- Performance improvement: Attribute aggregation from multiple sources now runs fully in parallel, reducing response times when multiple attribute authorities are consulted
- Support for logging in JSON format for Elastic/Graylog (#148)
- Added additional monitoring information to the info actuator (#148):
  - Added application version, artifact, java_version, name, time, spring_boot_version, and group to the build info
  - Added `days_since_release` to help determine when to build a new release
- Added OpenConext Invite configuration to YAML
- Added regression tests for Invite aggregator with SURF authorizations
- Maintenance:
  - Configuration of full path in the application.yml for SAB REST attribute aggregator (#149)

## 5.1.2

- Fix for YAML configuration parsing of attribute authorities (#147), resolving annoying snakeyaml warnings during startup
- Support for ARM64 Docker images

## 5.1.1

- Moved static content (logos, fonts, HTML) to `/aa/api` to ensure they are accessible under the new API path structure

## 5.1.0

- Bugfixes for the instituion aggregator:
  - Allow mapping of `urn:mace:dir:attribute-def:uid` attribute in institution aggregator (#140)
  - Gracefully handle the case that no InstitutionEndpoint is configured for an SP
- Make backend-connections less persistent; lifetime of a connection is now max 60s (#141)
- Change the paths for the info and health endpoints to support removal of the (url-rewriting) client
  Specifically, `/aa/api/internal/health` has been renamed to `/internal/health`,
  and `/aa/api/internal/info` has been renamed to `/internal/info`
- Fix dockerfile in order for the aa-server component to expose port 8080 (was previously handled by aa-client)
- Maintenance:
  - Update Readme to remove references to aa-gui
  - Add Changelog
  - Add editorconfig
  - Remove obsolete Travis config
