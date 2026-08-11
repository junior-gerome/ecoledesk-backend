# IdentityAccess DTO and Mapper Audit

## Summary
- Verified existing `identityaccess` DTOs and MapStruct mapper architecture.
- Found DTOs and mappers already implemented for `Person`, `Permission`, `Role`, and `UserAccount`.
- Identified missing DTO mappings for `RefreshToken` and `AuthenticationAuditEvent` domain models.

## Changes made
- Added DTO classes for `RefreshToken`:
  - `application.dto.refreshtoken.RefreshTokenBasicDTO`
  - `application.dto.refreshtoken.RefreshTokenMediumDTO`
  - `application.dto.refreshtoken.RefreshTokenFullDTO`
- Added DTO classes for `AuthenticationAuditEvent`:
  - `application.dto.audit.AuthenticationAuditEventBasicDTO`
  - `application.dto.audit.AuthenticationAuditEventMediumDTO`
  - `application.dto.audit.AuthenticationAuditEventFullDTO`
- Added MapStruct mappers:
  - `application.mapper.RefreshTokenMapper`
  - `application.mapper.AuthenticationAuditEventMapper`
- Fixed `PersonMapper` MapStruct configuration for `Person` embedded address fields.

## Validation
- `backend/gradlew.bat compileJava` completed successfully.
- Existing backend REST endpoint conventions in `AuthController`, `UserManagementController`, `RoleController`, `PermissionController`, and `PersonController` are preserved.

## Notes
- The `identityaccess` module already uses DDD layering with separate `application.interfaces`, `application.impl`, `web`, and `infrastructure.persistence` packages.
- There are still legacy service classes present in `backend/src/main/java/com/school/platform/identityaccess/application/` alongside the newer interface-based `application.impl` implementations. This may be a candidate for future cleanup but was not modified in this task.
