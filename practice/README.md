# Practice Store API

Spring Boot REST API for users, products, categories, and orders. The runtime datasource remains Oracle; tests use an isolated in-memory H2 database.

## API documentation

Start the application with `./mvnw spring-boot:run`, then open:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Static OpenAPI document: `http://localhost:8080/openapi.yaml`

JWT-protected endpoints accept `Authorization: Bearer <access-token>`. Obtain a token from `POST /api/auth/login` and use Swagger UI's **Authorize** control.

## Endpoint groups

- `/api/auth`: register and login
- `/api/categories`: list categories and create them as an administrator
- `/api/products`: browse and administer products
- `/api/orders`: place, browse, and update orders
- `/api/users`: manage the current user and administer users

## Verification

Run `./mvnw test`. The build creates the JaCoCo HTML report at `target/site/jacoco/index.html`.

## Docker

Run the API and Oracle XE together with `docker compose up --build`. The API is available at `http://localhost:8080` and Swagger UI is at `http://localhost:8080/swagger-ui/index.html`. Docker Compose loads the Oracle password and application database credentials from `.env`; use `.env.example` as the safe template.

The Compose defaults are intended for local development. Before any shared deployment, provide strong values for `ORACLE_PASSWORD`, `DB_PASSWORD`, and `APP_JWT_SECRET` through the environment or a local `.env` file.
