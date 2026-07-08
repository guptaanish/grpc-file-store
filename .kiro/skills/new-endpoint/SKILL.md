---
name: new-endpoint
description: Add a new REST API endpoint to the backend with full documentation and test coverage.
---

# New Endpoint

Add a new REST API endpoint to the backend with full documentation and test coverage.

## Procedure

1. **Determine placement**: Decide which controller the endpoint belongs in (`MockDiscoverApiController`, `FileController`, or a new controller).
2. **Add response DTO** (if needed): Add a record to `ApiResponses.java`.
3. **Add service method** (if needed): Implement business logic in the appropriate service class.
4. **Add controller method**:
   - Add `@GetMapping`/`@PostMapping`/etc. with the endpoint path.
   - Add Javadoc with `@param` and `@return`.
   - Add entry/exit logging (`log.info` or `log.debug`).
   - Delegate to service layer — no business logic in the controller.
5. **Add test**: Write a unit or integration test for the new endpoint.
6. **Update README.md**: Add the endpoint to the API Endpoints table.
7. **Verify build**: Run `./gradlew build` to confirm compilation, tests, checkstyle, and spotless pass.

## Checklist

- [ ] Response DTO added (if new shape)
- [ ] Service method implemented (if business logic needed)
- [ ] Controller method with Javadoc, logging, and validation
- [ ] Test written and passing
- [ ] README.md API table updated
- [ ] Build passes (`./gradlew build`)
