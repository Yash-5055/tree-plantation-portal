# 🌳 Ansible-Provisioned Tree Plantation Tracking Portal

A Spring Boot web portal to log tree plantation events, track survival status,
and view summary KPIs — built as a DevOps demonstration project covering
Git, Jenkins CI/CD, Selenium testing, Docker, and Ansible provisioning.

## Tech Stack
- Java 17 + Spring Boot 3.2 (Maven)
- Thymeleaf + plain HTML/CSS
- H2 (default, zero-setup) / PostgreSQL (production)
- Docker, Jenkins, Ansible

## Features (MVP)
1. Plantation event entry (`/events/new`) — with **GPS auto-capture** via the browser Geolocation API (tap "Use My Current Location" to auto-fill lat/long from the device's GPS instead of typing it manually)
2. Searchable dashboard (`/dashboard`)
3. Summary KPI indicators (`/dashboard/summary`)
4. Status drill-down (`/dashboard/status/{status}`)
5. Alert/exception view (`/alerts`)

### Note on GPS capture
Browsers only allow `navigator.geolocation` on **HTTPS or `localhost`** — it will silently fail on a plain `http://` IP address (e.g. testing from your phone against your laptop's LAN IP). For local dev on `localhost` this works out of the box; for a real deployed server, put it behind HTTPS (e.g. via Let's Encrypt/Nginx) for GPS to work on mobile.

## Run Locally
```bash
mvn spring-boot:run
# open http://localhost:8080/events/new
```
H2 console (optional, for inspecting data): `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:tppdb`)

## Run Tests
```bash
mvn test
```
Selenium tests in `src/test/java/com/tpp/EventEntryTest.java` require the app
running locally and a matching ChromeDriver on PATH.

## Build & Run with Docker
```bash
docker build -t tree-plantation-portal:1.0 .
docker run -d --name tpp-container -p 8080:8080 tree-plantation-portal:1.0
```

## Provision a Server with Ansible
```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/site.yml
```

## CI/CD
See `Jenkinsfile` — Checkout → Build → Test → Package → Docker Build/Push → Deploy.

## Folder Structure
```
tree-plantation-portal/
├── src/main/java/com/tpp/        # entities, repository, controllers
├── src/main/resources/templates/ # Thymeleaf views
├── src/test/java/com/tpp/        # Selenium/JUnit tests
├── ansible/                      # inventory + playbook
├── Dockerfile
├── Jenkinsfile
└── pom.xml
```

## Branching Policy
- `main` — protected, production-ready
- `develop` — integration branch
- `feature/<id>-desc`, `bugfix/<id>-desc`

## License
Academic project — MIT License.
