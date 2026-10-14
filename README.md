# 🌳 Tree Plantation Tracking Portal

> A full-stack **Spring Boot** web portal for logging tree plantation events, tracking survival status, and viewing summary KPIs — wrapped in an end-to-end **DevOps** pipeline covering Git, Jenkins CI/CD, Selenium testing, Docker, Nginx reverse proxy, and Ansible provisioning.

---

## 📑 Table of Contents

- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Features](#-features-mvp)
- [Prerequisites](#-prerequisites)
- [Run Locally](#-run-locally)
- [Run Tests](#-run-tests)
- [Build & Run with Docker](#-build--run-with-docker)
- [CI/CD Pipeline (Jenkins)](#-cicd-pipeline-jenkins)
- [Provision a Server with Ansible](#-provision-a-server-with-ansible)
- [API & Endpoints](#-api--endpoints)
- [Folder Structure](#-folder-structure)
- [Branching Policy](#-branching-policy)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2 (Maven, WAR packaging) |
| **View Engine** | Thymeleaf + HTML/CSS |
| **Database** | H2 (default, zero-setup) / PostgreSQL (production) |
| **Testing** | JUnit 5 + Selenium 4.20 (browser automation) |
| **Containerisation** | Docker (multi-stage build → Tomcat 10.1 runtime) |
| **CI/CD** | Jenkins Declarative Pipeline |
| **Provisioning** | Ansible (Ubuntu 22.04 target) |
| **Reverse Proxy** | Nginx |
| **Firewall** | UFW |
| **Monitoring** | Spring Boot Actuator (`/actuator/health`) |

---

## 🏗 Architecture

```
┌──────────┐      ┌──────────────┐      ┌────────────────────────┐
│  Client  │─────▶│   Nginx :80  │─────▶│  Docker (tpp-container)│
│ (Browser)│      │ reverse proxy│      │  Tomcat :8080          │
└──────────┘      └──────────────┘      │  Spring Boot App       │
                                        │    └─ H2 / PostgreSQL  │
                                        └────────────────────────┘
                        ▲
            Provisioned by Ansible
            Built & pushed by Jenkins
```

---

## ✨ Features (MVP)

1. **Plantation Event Entry** (`/events/new`) — log species, planter name, date, region, photo URL, and GPS coordinates with **browser geolocation auto-capture** (tap _"Use My Current Location"_ to auto-fill lat/long).
2. **Searchable Dashboard** (`/dashboard`) — browse and search all recorded plantation events.
3. **Summary KPI Indicators** (`/dashboard/summary`) — at-a-glance metrics on plantation activity.
4. **Status Drill-Down** (`/dashboard/status/{status}`) — filter events by status (`ALIVE`, `DEAD`, `UNHEALTHY`).
5. **Alert / Exception View** (`/alerts`) — view trees flagged as dead or unhealthy.

### 📍 Note on GPS Capture

Browsers only allow `navigator.geolocation` on **HTTPS or `localhost`**. It will silently fail on a plain `http://` IP address (e.g. testing from your phone against your laptop's LAN IP). For local development on `localhost`, GPS works out of the box. For a deployed server, put it behind HTTPS (e.g. via Let's Encrypt / Nginx) for GPS to function on mobile devices.

---

## 📋 Prerequisites

| Requirement | Minimum Version |
|---|---|
| Java JDK | 17+ |
| Apache Maven | 3.9+ |
| Docker | 20.10+ |
| Ansible | 2.14+ (for server provisioning) |
| ChromeDriver | matching your Chrome version (for Selenium tests) |

---

## 🚀 Run Locally

```bash
# Clone the repository
git clone https://github.com/Yash-5055/tree-plantation-portal.git
cd tree-plantation-portal

# Start the application (H2 in-memory database, zero config)
mvn spring-boot:run
```

Open [http://localhost:8080/events/new](http://localhost:8080/events/new) to log a plantation event.

**H2 Console** (optional — inspect data at runtime):
- URL: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- JDBC URL: `jdbc:h2:mem:tppdb`
- Username: `sa` / Password: _(empty)_

### Switch to PostgreSQL

Edit `src/main/resources/application.properties` — comment out the H2 block and uncomment the PostgreSQL block:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tpp_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

## 🧪 Run Tests

```bash
mvn test
```

Selenium end-to-end tests live in `src/test/java/com/tpp/EventEntryTest.java`. They require:
1. The application running locally (default port `8080`, or pass `-DbaseUrl=http://localhost:<port>`).
2. A matching **ChromeDriver** on your `PATH`.

Test reports are generated at `target/surefire-reports/`.

---

## 🐳 Build & Run with Docker

The project uses a **multi-stage Dockerfile**:
- **Stage 1 (build):** `maven:3.9-eclipse-temurin-17` — compiles and packages the WAR.
- **Stage 2 (runtime):** `tomcat:10.1-jdk21-temurin` — serves the application.

```bash
# Build the image
docker build -t tree-plantation-portal:1.0 .

# Run the container
docker run -d --name tpp-container -p 8080:8080 tree-plantation-portal:1.0
```

Open [http://localhost:8080](http://localhost:8080).

### Push to Docker Hub

```bash
docker tag tree-plantation-portal:1.0 yashlokare/tree-plantation-portal:1.0
docker push yashlokare/tree-plantation-portal:1.0
```

---

## ⚙ CI/CD Pipeline (Jenkins)

The [`Jenkinsfile`](Jenkinsfile) defines a **declarative pipeline** with the following stages:

```
Checkout → Build → Test (Selenium) → Publish Test Report → Package → Archive Artifact
    → Docker Build → Docker Push → Deploy Fresh Container
```

| Stage | Description |
|---|---|
| **Checkout** | Clones `main` branch from GitHub |
| **Build** | `mvn clean compile` |
| **Test** | Starts the app on port `8081`, runs Selenium tests, then tears down |
| **Publish Test Report** | Archives JUnit XML results from `target/surefire-reports/` |
| **Package** | `mvn package -DskipTests` (produces WAR) |
| **Archive Artifact** | Stores `target/*.war` in Jenkins |
| **Docker Build** | Builds image tagged with `BUILD_NUMBER` and `latest` |
| **Docker Push** | Pushes to Docker Hub (`yashlokare/tree-plantation-portal`) |
| **Deploy** | Removes old container and starts a fresh one on port `8081` |

### Jenkins Credentials Required

| ID | Type | Purpose |
|---|---|---|
| `dockerhub-creds` | Username / Password | Docker Hub push access |

### Jenkins Tools Required

| Tool | Label |
|---|---|
| Maven | `Maven3` |
| JDK | `JDK-21` |

---

## 📦 Provision a Server with Ansible

The Ansible playbook (`ansible/site.yml`) performs a **complete production setup** on Ubuntu 22.04:

```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/site.yml
```

### What the Playbook Does

| Step | Description |
|---|---|
| **System Packages** | Installs prerequisites + OpenJDK 21 |
| **Docker CE** | Adds Docker APT repo, installs Docker + Compose + Buildx |
| **OS Users** | Creates `deploy` user, adds to `docker` group |
| **Directories** | Sets up `/opt/tpp`, `/var/log/tpp`, `/etc/tpp` |
| **Configuration** | Templates `application.properties` from Jinja2 |
| **PostgreSQL** _(optional)_ | Installs PostgreSQL 15, creates DB + user (when `db_engine: postgresql`) |
| **Firewall (UFW)** | Allows SSH, HTTP (80), HTTPS (443); denies direct app port |
| **Nginx** | Installs and configures reverse proxy to the app container |
| **Docker Deploy** | Pulls image, removes old container, starts new one with log rotation |
| **Systemd** | Registers a systemd unit for auto-restart on boot |
| **Health Check** | Polls `/actuator/health` until the app is confirmed UP |
| **Jenkins** _(optional)_ | Installs Jenkins on the same node (set `install_jenkins: true`) |

### Key Variables (`ansible/vars/main.yml`)

| Variable | Default | Description |
|---|---|---|
| `docker_image` | `yashlokare/tree-plantation-portal` | Docker Hub image |
| `app_version` | `latest` | Image tag (override with `-e app_version=17`) |
| `db_engine` | `h2` | `h2` or `postgresql` |
| `host_port` | `8081` | Port exposed on the host |
| `install_jenkins` | `false` | Set to `true` to install Jenkins |

---

## 🔌 API & Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/events/new` | Plantation event entry form |
| `POST` | `/events/new` | Submit a new plantation event |
| `GET` | `/dashboard` | Searchable event listing |
| `GET` | `/dashboard/summary` | KPI summary view |
| `GET` | `/dashboard/status/{status}` | Filter by status (`ALIVE`, `DEAD`, `UNHEALTHY`) |
| `GET` | `/alerts` | Events flagged as dead or unhealthy |
| `GET` | `/h2-console` | H2 database web console (dev only) |
| `GET` | `/actuator/health` | Spring Boot health endpoint |

---

## 📂 Folder Structure

```
tree-plantation-portal/
├── src/
│   ├── main/
│   │   ├── java/com/tpp/
│   │   │   ├── TreePlantationPortalApplication.java   # Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   ├── EventController.java               # Event CRUD endpoints
│   │   │   │   └── DashboardController.java           # Dashboard & KPI endpoints
│   │   │   ├── model/
│   │   │   │   └── PlantationEvent.java               # JPA entity (species, planter, GPS, status)
│   │   │   └── repository/
│   │   │       └── PlantationEventRepository.java     # Spring Data JPA repository
│   │   └── resources/
│   │       ├── application.properties                 # App configuration (H2 / PostgreSQL)
│   │       ├── static/                                # CSS, JS, images
│   │       └── templates/                             # Thymeleaf views
│   │           ├── event-form.html                    #   └─ New event form
│   │           ├── event-success.html                 #   └─ Submission confirmation
│   │           ├── event-detail.html                  #   └─ Single event view
│   │           ├── dashboard.html                     #   └─ Event listing
│   │           ├── summary.html                       #   └─ KPI summary
│   │           └── alerts.html                        #   └─ Dead/unhealthy alerts
│   └── test/java/com/tpp/
│       └── EventEntryTest.java                        # Selenium end-to-end tests
├── ansible/
│   ├── site.yml                                       # Master playbook (13 stages)
│   ├── docker-target.yml                              # Docker-focused playbook
│   ├── local-test.yml                                 # Local testing playbook
│   ├── reliability-validation.yml                     # Reliability checks
│   ├── ansible.cfg                                    # Ansible configuration
│   ├── requirements.yml                               # Galaxy collection dependencies
│   ├── inventory/
│   │   └── hosts.ini                                  # Target hosts
│   ├── vars/
│   │   └── main.yml                                   # Variables (image, ports, DB, etc.)
│   └── templates/
│       ├── application.properties.j2                  # Jinja2 → app config
│       ├── nginx-tpp.conf.j2                          # Jinja2 → Nginx reverse proxy
│       └── docker-tpp.service.j2                      # Jinja2 → systemd unit
├── .github/
│   └── ISSUE_TEMPLATE/
│       ├── bug_report.md                              # Bug report template
│       └── feature_request.md                         # Feature request template
├── Dockerfile                                         # Multi-stage build (Maven → Tomcat)
├── Jenkinsfile                                        # Declarative CI/CD pipeline
├── pom.xml                                            # Maven project descriptor
└── .gitignore
```

---

## 🌿 Branching Policy

| Branch | Purpose |
|---|---|
| `main` | Protected — production-ready releases |
| `develop` | Integration branch for ongoing work |
| `feature/<id>-desc` | New feature development |
| `bugfix/<id>-desc` | Bug fixes |

---

## 🤝 Contributing

1. **Fork** the repository
2. Create your feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a **Pull Request**

Please use the provided [issue templates](.github/ISSUE_TEMPLATE/) for bug reports and feature requests.

---

## 📄 License

Academic project — MIT License.
