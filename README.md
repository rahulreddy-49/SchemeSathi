# SchemeSathi (योजना साथी)

> **AI-Assisted Citizen Welfare Discovery & Deterministic Eligibility Platform**  
> Bridging the gap between 1.4 billion citizens and Indian public welfare policies through verifiable, hallucination-free entitlement matching.

---

## 🏛️ Project Overview

**SchemeSathi** is a modern GovTech platform designed to help Indian citizens discover and claim statutory government welfare benefits across Central and State ministries. 

### Core Architectural Philosophy:
> **"LLM UNDERSTANDS. DETERMINISTIC ENGINE DECIDES."**  
> AI and natural language processing are used solely for conversation, profile entity extraction, and multilingual translation. All eligibility assessments are computed strictly by a deterministic rule engine executing codified statutory logic.

---

## 🏗️ System Architecture

```mermaid
graph TD
    User([Citizen / User]) <--> Frontend[React 19 + Vite Frontend\nhttp://localhost:5173]
    Frontend <-->|API Proxy /api/*| Backend[Spring Boot 3.2 Backend\nhttp://localhost:8080]
    
    subgraph Backend Architecture
        Backend --> Security[Spring Security 6 + JWT]
        Backend --> Controllers[REST Controllers\nAuth, Schemes, Profile, AI, Demo]
        Controllers --> Orchestrator[Multi-Agent AI Orchestrator]
        Controllers --> RuleEngine[Deterministic Eligibility Engine]
        Controllers --> Services[Business Services]
        RuleEngine --> JPA[Spring Data JPA]
        Services --> JPA
        JPA --> Database[(PostgreSQL Database / Flyway Migrations)]
    end
```

---

## 🚀 Key Features

1. **Deterministic Eligibility Rule Engine**:
   - Evaluates citizen criteria (Age, Gender, State, Occupation, Income, Social Category, Landholding, BPL/Ration Card) against statutory rules.
   - Outputs match scores (0–100%), verified pass/fail criterion breakdowns, missing profile details, and clear policy explanations.

2. **Official Government Schemes Database**:
   - Pre-seeded with 10 flagship welfare schemes across Central and State ministries:
     - **PM-KISAN** (Pradhan Mantri Kisan Samman Nidhi)
     - **Ayushman Bharat PM-JAY** (National Health Protection)
     - **Post-Matric Scholarship for SC Students**
     - **Pradhan Mantri Awas Yojana - Gramin (PMAY-G)**
     - **Sukanya Samriddhi Yojana**
     - **Pradhan Mantri Ujjwala Yojana (PMUY)**
     - **MGNREGA** (Rural Employment Guarantee)
     - **Telangana Kalyana Lakshmi / Shaadi Mubarak**
     - **Atal Pension Yojana (APY)**
     - **Pradhan Mantri MUDRA Yojana (PMMY)**

3. **Multi-Agent AI Sahayak (Chatbot)**:
   - Natural language interface that understands citizen queries.
   - Automatically extracts profile details (e.g., occupation, employment status) into structured profile attributes.
   - Cites official sources and suggests 1-click follow-up actions.

4. **Synthetic Sandbox Demo Mode**:
   - Test the system without creating an account or providing real data.
   - 3 pre-configured synthetic personas:
     - **Lakshmi Devi** (35, Female, Small Farmer, Telangana)
     - **Ravi Kumar** (20, Male, SC Student, Rajasthan)
     - **Meena Sharma** (28, Female, General, Urban Professional, Maharashtra)
   - One-click profile switching demonstrating real-time rule adaptation.

5. **Citizen Profile & Completeness Tracker**:
   - Dynamic completeness progress meter (0–100%).
   - Preserves demographic and socioeconomic attributes.
   - Saved schemes bookmarking to track deadlines and application status.

6. **Document Preparation Checklist & Official Portals**:
   - Detailed statutory documents required per scheme (Aadhaar, Land records, Income certificates, Caste certificates).
   - Valid alternatives where permitted.
   - Direct links to official government application portals (`.gov.in` / `.nic.in`) to prevent middleman exploitation.

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.3.6 (Java 17+)
- **Security**: Spring Security 6 with stateless JWT (jjwt 0.12.5) & BCrypt password hashing
- **Persistence**: Spring Data JPA / Hibernate 6
- **Database**: PostgreSQL 15+ / 16+ (Production / Development)
- **Database Migrations**: Flyway 10+ (`db/migration/V1__initial_schema.sql`)
- **Testing**: JUnit 5, Spring Security Test, AssertJ, H2 in-memory (PostgreSQL-compatibility mode)

### Frontend
- **Framework**: React 19 + Vite 8
- **Routing**: React Router DOM 7
- **Icons**: Lucide React
- **Design System**: Custom GovTech theme (Saffron, Cyan, Emerald accents, dark glassmorphism, responsive grid)
- **HTTP Client**: Axios with JWT interceptor and automatic 401 handling

---

## 📁 Repository Structure

```text
SchemeSathi/
├── README.md                          # Combined Full-Stack Documentation
├── backend/
│   ├── pom.xml                        # Maven configuration
│   ├── src/main/
│   │   ├── java/com/schemesathi/
│   │   │   ├── SchemeSathiApplication.java
│   │   │   ├── controller/            # REST API Endpoints
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   ├── SchemeController.java
│   │   │   │   ├── AiController.java
│   │   │   │   ├── DemoController.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── dto/                   # Request/Response Data Objects
│   │   │   ├── entity/                # JPA Database Entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Scheme.java
│   │   │   │   ├── EligibilityRule.java
│   │   │   │   ├── RequiredDocument.java
│   │   │   │   ├── CitizenProfile.java
│   │   │   │   ├── CitizenDocument.java
│   │   │   │   └── SavedScheme.java
│   │   │   ├── repository/            # Spring Data JPA Repositories
│   │   │   ├── security/              # JWT & Security Configuration
│   │   │   └── service/               # Core Logic & Engines
│   │   │       ├── EligibilityEngine.java       # Deterministic rule engine
│   │   │       ├── AiAgentOrchestrator.java     # Multi-agent AI pipeline
│   │   │       ├── SchemeService.java
│   │   │       ├── CitizenProfileService.java
│   │   │       ├── AuthService.java
│   │   │       └── DataSeeder.java              # 10 verified statutory schemes
│   │   └── resources/
│   │       ├── application.properties # PostgreSQL & application configuration
│   │       └── db/migration/
│   │           └── V1__initial_schema.sql # Flyway baseline schema migration
│   ├── src/test/
│   │   ├── java/com/schemesathi/
│   │   │   ├── database/FlywayMigrationAndSchemaTest.java
│   │   │   ├── security/SecurityRouteRulesTest.java
│   │   │   ├── security/JwtTokenProviderTest.java
│   │   │   └── service/DataSeederSecurityTest.java
│   │   └── resources/
│   │       └── application.properties # Isolated test config (Flyway + validation)
│   └── .mvn/ / mvnw.cmd               # Maven build wrapper
└── frontend/
    ├── package.json                   # React dependencies
    ├── vite.config.js                 # Vite bundler & API proxy configuration
    ├── index.html                     # HTML root template with SEO meta
    └── src/
        ├── api.js                     # Axios client & endpoint groups
        ├── AuthContext.jsx            # Authentication state & JWT persistence
        ├── index.css                  # GovTech Design System CSS
        ├── App.jsx                    # Application layout & React routes
        ├── components/
        │   ├── Navbar.jsx             # Responsive navigation header
        │   └── Footer.jsx             # National helplines & legal disclaimer
        └── pages/
            ├── HomePage.jsx           # Landing hero, sectors, live stats
            ├── SchemesPage.jsx        # Catalog with search & multi-filters
            ├── SchemeDetailPage.jsx   # Criteria breakdown & live test widget
            ├── EligibilityCheckerPage.jsx # Multi-scheme assessment wizard
            ├── AiAssistantPage.jsx    # AI Sahayak chat interface
            ├── DemoPage.jsx           # Sandbox with 3 synthetic personas
            ├── ProfilePage.jsx        # Citizen profile & saved schemes
            ├── LoginPage.jsx          # Secure login form
            └── RegisterPage.jsx       # Citizen registration form
```

---

## 🗄️ Database Setup & Flyway Migrations

SchemeSathi uses **PostgreSQL** as its enterprise-grade production relational database, managed via **Flyway** version-controlled migrations.

### 1. Local PostgreSQL Setup
Install PostgreSQL 15+ locally or launch via Docker:
```bash
# Using Docker:
docker run --name schemesathi-postgres -e POSTGRES_DB=schemesathi -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16-alpine

# Or using local psql CLI:
createdb schemesathi
```

### 2. Environment Variables Configuration
Configure datasource credentials via environment variables:

| Variable | Description | Default (Local Dev) |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/schemesathi` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | 256-bit HMAC signing secret | Default dev secret (change in prod) |
| `APP_SEED_DEMO_USERS` | Seed demo accounts on startup | `false` (strictly disabled in prod) |
| `PORT` | Web server port | `8080` (dynamically assigned on Render) |

### 3. Flyway Migration Lifecycle
- On application startup, Flyway automatically discovers and applies versioned scripts located in `src/main/resources/db/migration/`.
- `V1__initial_schema.sql` creates all relational tables (`users`, `schemes`, `eligibility_rules`, `required_documents`, `citizen_profiles`, `citizen_documents`, `saved_schemes`), foreign keys with cascading actions, and database indexes.
- Hibernate runs in validation mode (`spring.jpa.hibernate.ddl-auto=validate`) to verify entity mappings against the Flyway schema without competing for DDL modifications.

### 4. Running Backend Tests
Execute the comprehensive test suite (including Flyway migrations, JPA entity validation, security route access, and demo user restrictions):
```bash
cd backend
mvn test
```
Tests run against an in-memory test database in PostgreSQL-compatibility mode (`MODE=PostgreSQL`), executing `V1__initial_schema.sql` and performing full JPA validation.

### 5. Production Deployment (e.g., Render)
1. In your Render Dashboard, create a **PostgreSQL Database** service.
2. In your SchemeSathi Web Service settings, set the following environment variables:
   - `SPRING_DATASOURCE_URL`: Use the external or internal JDBC connection string provided by Render (e.g., `jdbc:postgresql://<hostname>:5432/<dbname>`).
   - `SPRING_DATASOURCE_USERNAME`: Render database user.
   - `SPRING_DATASOURCE_PASSWORD`: Render database password.
   - `JWT_SECRET`: A secure, random 256-bit string.
   - `APP_SEED_DEMO_USERS`: `false`
3. Deploy the service. Flyway will execute the schema migrations automatically on initial startup.

### 6. Database Backups
Before deploying future schema migrations, always capture a point-in-time backup:
```bash
pg_dump -h <host> -U <user> -d schemesathi -F c -b -v -f "schemesathi_backup_$(date +%Y%m%d).dump"
```

---

## ⚡ Quick Start Guide

### Prerequisites
- **Java**: JDK 17 or higher
- **Node.js**: v18.0 or higher with `npm`
- **Maven**: Bundled Maven wrapper or Maven 3.8+
- **PostgreSQL**: Local PostgreSQL 15+ or Docker

---

### Step 1: Run the Backend

Open a terminal in `backend/`:

```powershell
cd backend
# Using Maven wrapper (Windows)
.\mvnw.cmd spring-boot:run

# Or using installed Maven:
mvn spring-boot:run
```

The backend server starts on **`http://localhost:8080`**.  
On initial startup, Flyway runs `V1__initial_schema.sql`, and `DataSeeder` initializes statutory scheme definitions.

---

### Step 2: Run the Frontend

Open a second terminal in `frontend/`:

```powershell
cd frontend
# Install dependencies (if not already installed)
npm install

# Start the Vite development server
npm run dev
```

The frontend server starts on **`http://localhost:5173`** with an automated proxy forwarding all `/api/*` calls to `http://localhost:8080`.

---

### Step 3: Access the Application

Open your browser and navigate to:
```
http://localhost:5173
```

---

## 🔑 Default Credentials & Demo Access

### Administrator Account
- **Email**: `admin@schemesathi.in`
- **Password**: `Admin@SchemeSathi2024`
- *(Quick fill available on the Login screen)*

### Citizen Account
- Register any citizen profile via the **Register** button, or test without an account using the **Demo Mode** tab.

---

## 📡 REST API Reference

| Endpoint | Method | Access | Description |
|---|---|---|---|
| `/api/auth/register` | `POST` | Public | Register a new citizen account |
| `/api/auth/login` | `POST` | Public | Authenticate user & return JWT token |
| `/api/schemes` | `GET` | Public | List schemes (supports `?search=`, `?category=`, `?state=`) |
| `/api/schemes/{id}` | `GET` | Public | Get full statutory details & rules for a scheme |
| `/api/schemes/categories` | `GET` | Public | List all available welfare categories |
| `/api/schemes/{id}/eligibility` | `GET` | Authenticated | Deterministic eligibility check for logged-in profile |
| `/api/schemes/eligibility/all` | `GET` | Authenticated | Comprehensive assessment across all active schemes |
| `/api/schemes/{id}/save` | `POST` | Authenticated | Bookmark scheme to user's saved list |
| `/api/schemes/{id}/save` | `DELETE` | Authenticated | Remove scheme from saved list |
| `/api/schemes/saved` | `GET` | Authenticated | Retrieve all bookmarked schemes |
| `/api/profile` | `GET` | Authenticated | Retrieve current citizen's criteria profile |
| `/api/profile` | `PUT` | Authenticated | Save full profile criteria |
| `/api/profile` | `PATCH` | Authenticated | Partial update of profile attributes |
| `/api/ai/chat` | `POST` | Authenticated | Multi-agent AI assistant query |
| `/api/demo/schemes` | `GET` | Public | Schemes for unauthenticated demo sandbox |
| `/api/demo/profiles` | `GET` | Public | Retrieve 3 synthetic citizen personas |
| `/api/demo/eligibility/{id}` | `POST` | Public | Test dynamic criteria against a scheme without account |

---

## 🛡️ Public Welfare Disclaimer

SchemeSathi is an independent informational civic technology application. The platform's eligibility evaluations are automated advisory tools based on publicly available policy notifications. Final approval and disbursement of welfare benefits are governed solely by the respective Government of India ministries, State departments, and authorized welfare officers.
