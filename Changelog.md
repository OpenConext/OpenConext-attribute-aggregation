# Release notes

Starting from version 5.0.0, we note changes and new features per release in this file.

## 5.1.0

- Bugfixes for the instituion aggregator:
  - Allow mapping of `urn:mace:dir:attribute-def:uid` attribute in institution aggregator (#140)
  - Gracefully handle the case that no InstitutionEndpoint is configured for an SP
- Make backend-connections less persistent; lifetime of a connection is now max 60s (#141)
- Change the paths for the info and health endpoints to support removal of the (url-rewriting) client
  Specifically, `/aa/api/internal/health` has been renamed to `/internal/health`,
  and `/aa/api/internal/info` has been renamed to `/internal/info`
  TODO: check
- Fix dockerfile in order for the aa-server component to expose port 8080 (was previously handled by aa-client)
- Maintenance:
  - Update Readme to remove references to aa-gui
  - Add Changelog
  - Add editorconfig
  - Remove obsolete Travis config
