# System Architecture

## Overview
The API Security Platform is a 3-tier application designed to monitor, analyze, and report on API traffic to identify malicious behavior.

## Components

### 1. React Frontend
- **Technology**: React, Vite, React Router, Lucide Icons
- **Purpose**: Provides a professional dashboard for security analysts to monitor threats, update incident status, and trigger traffic simulations.
- **Key Modules**: Login, Dashboard Overview, Threat Management, Traffic Simulator.

### 2. Spring Boot Backend
- **Technology**: Java 17, Spring Boot, Spring Security, Spring Data JPA
- **Purpose**: The core engine. It exposes APIs for the frontend, handles authentication via JWT, and intercepts all incoming API requests using `ApiMonitoringFilter`.
- **Key Modules**:
  - **Auth**: JWT based authentication.
  - **Filter**: Intercepts requests, logs them to the DB.
  - **Rule Engine**: Evaluates thresholds for rate limiting, brute force, and enumeration.
  - **Threat Management**: APIs to list, update, and annotate threats.

### 3. Machine Learning Service
- **Technology**: Python 3.11, FastAPI, scikit-learn
- **Purpose**: Evaluates API request metadata asynchronously to detect anomalous patterns using an Isolation Forest model.
- **Workflow**: The Spring Boot backend makes HTTP POST requests to the FastAPI endpoint (`/predict`) with traffic metrics.

### 4. MySQL Database
- **Purpose**: Persistent storage for Users, ApiRequests, Threats, and Investigation Notes.

## Data Flow
1. Client makes an HTTP request to the Spring Boot API.
2. `ApiMonitoringFilter` intercepts the request, notes the start time, and lets it proceed.
3. Once completed, the filter calculates response time and logs the request to the `api_requests` table.
4. `ThreatDetectionService` evaluates the request against static rules (e.g., > 5 auth failures = Brute Force).
5. If no rule is triggered, the service aggregates recent metrics for the IP and queries the ML Service.
6. If the ML Service flags an anomaly, a new `Threat` is persisted.
7. An analyst logs into the React frontend, views the threat, and updates its status.
