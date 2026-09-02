# CI/CD Pipeline

## Purpose

The Sunrise Dental Clinic application uses GitHub Actions to provide a repeatable build, automated regression checks, and a deployable WAR artifact. This protects the `development` branch by checking changes before they are merged.

## CI pipeline

The workflow is `.github/workflows/ci.yml` and runs on Ubuntu with Eclipse Temurin Java 17, matching the Maven compiler configuration in `pom.xml`.

The pipeline performs these steps:

1. Checks out the repository.
2. Installs Java 17.
3. Uses the built-in Maven dependency cache from `actions/setup-java`.
4. Runs `mvn --batch-mode --no-transfer-progress clean verify`.
5. Confirms that exactly one WAR file was created in `target/`.
6. Uploads the WAR as the `sunrise-dental-clinic-war` workflow artifact for 14 days.

A failed dependency resolution, compilation, test, packaging, or WAR check fails the job.

## Trigger conditions

The workflow runs for:

- pushes to `development`
- pushes to `main`
- pull requests targeting `development`

This supports the repository flow from feature branches into `development` without modifying `main` directly.

## Testing and database handling

The current Maven test suite contains `EmailServiceImplTest`. It does not provision or require a PostgreSQL service container as part of its assertions. The test suite therefore runs without database credentials in CI.

The application runtime still requires PostgreSQL configuration through environment variables such as `DB_URL`, `DB_USER`, and `DB_PASSWORD`. These are not needed for the current CI test/build job and are not placed in the workflow.

If database integration tests are added later, they should use a separate test profile and an isolated PostgreSQL service with non-production credentials.

## Security

The workflow declares `contents: read` as its only GitHub token permission. It contains no database passwords, email API keys, deployment tokens, or private keys. Runtime values such as `DB_PASSWORD`, `EMAIL_API_KEY`, and `EMAIL_FROM` must be configured through the deployment environment or GitHub Actions secrets when a deployment job is introduced.

The Maven build uses the dependencies already declared by the project and does not disable tests to make CI pass.

## Continuous Delivery status

Continuous Integration is fully implemented. Continuous Delivery is intentionally limited to producing and storing the WAR artifact because this repository has no configured hosting platform, Tomcat server endpoint, deployment token, or deployment secret.

The uploaded WAR can be downloaded from a successful Actions run and deployed to the existing Tomcat environment manually. A future deployment job can be added with environment-scoped secrets after a hosting target is selected.

## Benefits

- Defects are detected before changes enter `development`.
- Every build uses a known Java version and the same Maven command.
- Tests provide automated regression coverage.
- WAR packaging is verified consistently.
- The artifact is available for review or manual deployment.
- Feature branch integration becomes more predictable.

## Limitations

- The current tests do not exercise live PostgreSQL functions, triggers, or database connectivity.
- Email delivery is not tested against a real provider in CI.
- Deployment remains manual because no hosting target is configured.
- The pipeline depends on GitHub Actions and Maven Central availability.
- Environment-specific database, Tomcat, and email configuration remains outside the repository.

## Evidence for the assignment

Capture evidence from a real successful GitHub Actions run, without fabricating results:

- the workflow file in `.github/workflows/ci.yml`
- the green workflow summary
- the Maven test output
- the successful WAR confirmation step
- the uploaded `sunrise-dental-clinic-war` artifact
- the pull request check on `development`

## Local verification

Run the same build locally from the project root:

```text
mvn --batch-mode --no-transfer-progress clean verify
```

The generated WAR is located under `target/` and can be deployed to Tomcat manually.
