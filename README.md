# AI-Powered API Security & Threat Detection Platform

> **Final Year B.Tech CSE Project** — Saharshita Kommireddy

A full-stack platform that monitors API traffic in real time, detects security threats using rule-based and ML-based anomaly detection, and presents findings in a professional analyst dashboard.

---

## Features

| Feature | Description |
|---|---|
| **API Request Monitoring** | Logs all incoming requests (IP, endpoint, method, status, latency) via a Servlet filter |
| **Rule-Based Threat Detection** | Detects brute-force login, rate-limit abuse, and suspicious endpoint enumeration |
| **ML Anomaly Detection** | Isolation Forest (scikit-learn) classifies anomalous traffic patterns |
| **Risk Scoring** | Multi-signal risk score (0–100) mapped to LOW / MEDIUM / HIGH / CRITICAL severity |
| **Threat Dashboard** | React UI for analysts to review, investigate, and resolve incidents |
| **Traffic Simulator** | Generate test traffic directly from the dashboard |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, WebFlux |
| **Database** | MySQL 8 |
| **ML Service** | Python 3.11, FastAPI, scikit-learn (Isolation Forest), pandas, uvicorn |
| **Frontend** | React 19, Vite 8, Axios, Recharts, Lucide React |

---

## Prerequisites — Install These First

You must have all four installed before running the project.

### 1. Java 17 (JDK)
- Download: https://adoptium.net/ (Temurin JDK 17)
- Verify: `java -version` — must show `17.x.x`

### 2. Python 3.11
- Download: https://www.python.org/downloads/
- During install, check **"Add Python to PATH"**
- Verify: `python --version` — must show `3.11.x`

### 3. Node.js 18+ (with npm)
- Download: https://nodejs.org/ (LTS version)
- Verify: `node -v` and `npm -v`

### 4. MySQL 8
- Download: https://dev.mysql.com/downloads/installer/
- During install, set **root password to `root`** (or change `application.properties` to match yours)
- Verify: MySQL service is running on port 3306

> **No Maven install needed** — the project ships with `mvnw.cmd` (Maven Wrapper) which downloads Maven automatically on first use.

---

## One-Time Database Setup

Open **MySQL Workbench** (or any MySQL client) and run:

```sql
CREATE DATABASE IF NOT EXISTS api_security_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Spring Boot (`ddl-auto=update`) creates all tables automatically on first startup.

---

## Running the Project

### Option A — Automatic (Recommended)

1. Make sure MySQL is running
2. Double-click **`start.bat`** inside the `api-security-platform/` folder
3. Three terminal windows open automatically:
   - `ML Service - port 8000` (Python/FastAPI — trains model then starts server)
   - `Backend - port 8080` (Spring Boot Java)
   - `Frontend - port 5173` (React/Vite)
4. Wait ~60 seconds for Spring Boot to finish starting
5. Open: **http://localhost:5173**
6. Login: **`admin` / `admin123`**

---

### Option B — Manual Step-by-Step (VS Code Terminals)

Open **4 separate terminals** in VS Code (`Terminal → New Terminal` repeated).

#### Terminal 1 — Start MySQL
```powershell
# Run PowerShell as Administrator
net start MySQL80
```
Or: `Win+R` → `services.msc` → MySQL80 → Right-click → Start.

---

#### Terminal 2 — ML Service
```powershell
cd "d:\AI-POWERED API SECURITY & THREAT DETECTION PLATFORM\api-security-platform\ml-service"

# First time only: install dependencies
pip install -r requirements.txt

# First time only: train the model (~10 seconds)
python train_model.py

# Start the server
uvicorn main:app --port 8000 --reload
```
Ready when you see: `Uvicorn running on http://127.0.0.1:8000`

---

#### Terminal 3 — Spring Boot Backend
```powershell
cd "d:\AI-POWERED API SECURITY & THREAT DETECTION PLATFORM\api-security-platform\backend"

# Uses the bundled Maven Wrapper — no Maven install required
.\mvnw.cmd spring-boot:run
```
Ready when you see: `Started ApiSecurityPlatformApplication in X seconds`

> **First run** downloads all Maven dependencies (~200 MB). This takes 3–5 minutes. Subsequent runs are fast.

---

#### Terminal 4 — React Frontend
```powershell
cd "d:\AI-POWERED API SECURITY & THREAT DETECTION PLATFORM\api-security-platform\frontend"

# First time only
npm install

# Start dev server
npm run dev
```
Ready when you see: `Local: http://localhost:5173/`

---

#### Open in Browser
Go to: **http://localhost:5173**

```
Username: admin
Password: admin123
```

---

## Project Structure

```
api-security-platform/
├── start.bat                    ← Double-click to auto-start everything
├── backend/                     ← Spring Boot (port 8080)
│   ├── mvnw.cmd                 ← Maven Wrapper (replaces needing mvn installed)
│   ├── pom.xml
│   └── src/main/java/com/apisecurity/
│       ├── config/              ← SecurityConfig, DataInitializer
│       ├── controller/          ← Auth, Threats, Dashboard, Simulator REST APIs
│       ├── dto/                 ← Request/Response DTOs
│       ├── model/               ← JPA Entities (User, Threat, ApiRequest, etc.)
│       ├── repository/          ← Spring Data JPA repositories
│       ├── security/            ← JWT filter, monitoring filter, UserDetailsService
│       └── service/             ← ThreatDetectionService, MlServiceClient
├── ml-service/                  ← Python FastAPI (port 8000)
│   ├── main.py                  ← /predict and /health endpoints
│   ├── train_model.py           ← Trains Isolation Forest, saves to model/
│   ├── requirements.txt
│   └── model/                   ← isolation_forest.joblib (created after training)
├── frontend/                    ← React + Vite (port 5173)
│   ├── src/
│   │   ├── pages/               ← Dashboard, Threats, Login
│   │   ├── components/          ← Reusable UI components
│   │   └── services/            ← Axios API client
│   └── package.json
├── database/
│   └── schema.sql               ← Reference schema (Spring auto-creates tables)
└── docs/                        ← Project documentation
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/login` | No | Get JWT token |
| GET | `/api/dashboard/summary` | JWT | Dashboard stats |
| GET | `/api/threats` | JWT | Paginated threat list |
| PATCH | `/api/threats/{id}/status` | JWT | Update threat status |
| GET | `/api/threats/{id}/notes` | JWT | Get investigation notes |
| POST | `/api/threats/{id}/notes` | JWT | Add investigation note |
| GET | `/health` _(ML service)_ | No | ML service health |
| POST | `/predict` _(ML service)_ | No | Run anomaly prediction |

---

## Troubleshooting

### `mvn is not recognized`
**Cause:** Maven not installed globally.  
**Fix:** Use `.\mvnw.cmd` instead of `mvn`. The wrapper is in `backend/`. `start.bat` already uses it.

### `Cannot connect to MySQL` / Spring Boot won't start
**Fix:**
```powershell
# PowerShell as Admin
net start MySQL80
```

### `Access denied for user 'root'@'localhost'`
**Fix:** Your MySQL password doesn't match. Edit:
`backend/src/main/resources/application.properties`
```properties
spring.datasource.password=YOUR_ACTUAL_PASSWORD
```

### `uvicorn not recognized` / `pip not found`
**Cause:** Python not on PATH.  
**Fix:** Reinstall Python from python.org — on first screen, check **"Add Python to PATH"**.

### Frontend shows "Backend server is offline"
**Fix:** Spring Boot hasn't started yet. Check the backend terminal. Wait for:  
`Started ApiSecurityPlatformApplication in X seconds`

### Red underlines in VS Code Java files
**Cause:** VS Code Language Server hasn't indexed dependencies yet.  
**Fix:** `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace` → Restart.  
These errors do **not** block `mvnw.cmd spring-boot:run` — run from terminal to verify.

### ML model not found warning
**Fix:** Train the model first:
```powershell
cd ml-service
python train_model.py
```
Restart ML service after training. Without the model file, the service uses a simple fallback rule.

---

## Ports Summary

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| ML Service | http://localhost:8000 |

---

## Default Login

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |

Created automatically on first startup by `DataInitializer.java`.

---

## Author

**Saharshita Kommireddy** — Final Year B.Tech CSE Project

