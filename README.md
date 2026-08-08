# The Ladders: Take-home assignment

A job listings application with a Spring Boot backend (REST API backed by SQLite) and a React + Vite frontend.

## Project structure

- `backend/` — Spring Boot API server (Java, Gradle)
- `frontend/` — React + TypeScript SPA (Vite)

## Prerequisites

- Java 25 (JDK)
- Node.js 22+ and npm

The backend uses the included Gradle wrapper (`gradlew` / `gradlew.bat`), so a separate Gradle install isn't required.

## Backend

The API runs on <http://localhost:8080> and stores data in a SQLite database at `backend/data/backend.db`. Database migrations (Liquibase) run automatically on startup.

From the `backend/` directory:

```
# Windows
gradlew.bat bootRun

# macOS/Linux
chmod 755 gradlew # (only needs to be done once to ensure file is executable)
./gradlew bootRun
```

API docs (Swagger UI) are available at <http://localhost:8080/swagger-ui.html> once the server is running.

## Frontend

From the `frontend/` directory, install dependencies and start the dev server:

```
npm install
npm run dev
```

The dev server runs on <http://localhost:5173> and proxies `/api` requests to the backend at <http://localhost:8080>, so start the backend first.

## Running the full app

1. Start the backend (`gradlew.bat bootRun` from `backend/`).
2. Start the frontend (`npm run dev` from `frontend/`).
3. Open <http://localhost:5173> in your browser.
