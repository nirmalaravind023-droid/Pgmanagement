# StayNear PG Rentals 🏠

A location-based PG discovery and booking platform for students and working professionals, built with Java Spring Boot and a React frontend.

## 🚀 Advanced Architecture

StayNear is built with a focus on scalability, security, and real-time geospatial accuracy.

### 🏗️ Backend (Java Spring Boot 3)
*   **Official Google Maps Integration**: Real-time discovery using Places API, Details API, and Geocoding API.
*   **Intelligent Caching**: Automated 24-hour cache for Google metadata to optimize API costs.
*   **PostGIS Spatial Indexing**: Advanced geospatial queries using `ST_Distance` and `ST_DWithin` for millisecond performance at scale.
*   **Asynchronous Queuing**: RabbitMQ-powered background tasks for OTP delivery and notifications.
*   **Database Migrations**: Versioned schema management using **Flyway**.
*   **API Documentation**: Automated OpenAPI 3.0 specs via **Swagger UI** (`/swagger-ui.html`).
*   **Security & Stability**:
    *   **Rate Limiting**: IP-based throttling using Bucket4j (instance-local; Redis-backed for multi-instance deployments).
    *   **Stateless Auth**: JWT-based authentication.

### 🎨 Frontend (Next.js 15)
*   **Google Maps Autocomplete**: Seamless address search and coordinate detection.
*   **Live Geospatial UI**: Real-time distance calculation ("1.2 km away") and area name detection.
*   **Responsive Design**: Premium dark-mode-ready UI built with Tailwind CSS and Framer Motion.

## 🛠️ Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Core** | Java 17, Spring Boot 3.3.5, Hibernate |
| **Database** | PostgreSQL 16 + **PostGIS** |
| **Caching/Messaging** | Redis, RabbitMQ |
| **Frontend** | Next.js 15, React, Tailwind CSS |
| **Infrastructure** | Docker, Flyway, Swagger |

## 📈 Architecture Notes

1.  **Geospatial Performance**: PostGIS GIST indexes on `GEOGRAPHY(Point,4326)` columns make nearby-PG queries fast at moderate scale; `ST_DWithin` short-circuits on the index.
2.  **OTP Delivery**: Redis stores OTPs with 5-minute TTL; RabbitMQ delivers them asynchronously so the request returns immediately.
3.  **Rate Limiting**: Bucket4j enforces 10 req/min per IP. The current ConcurrentHashMap store is instance-local; replace with the Redis-backed Bucket4j store before running multiple backend instances.

## 📍 Request Flow
```text
Client (Web/Mobile) 
   → Nginx (Reverse Proxy) 
   → Spring Boot API (v1)
      → Redis (Cache Hit?)
      → PostgreSQL + PostGIS (DB)
      → RabbitMQ (Async Tasks)
      → Google Maps APIs (External Sync)
```

## 🗂️ Repository Structure

This repo contains multiple implementations. The **active application** is:

| Layer | Path | Notes |
| :--- | :--- | :--- |
| **Backend (active)** | `apps/backend-java/` | Spring Boot 3 — this is what `docker-compose` starts |
| **Frontend (active)** | `frontend_web/` | React + Vite, connects to the Java backend |
| `backend/` | Node.js prototype | Earlier exploration — not production-wired |

Run `apps/backend-java` + `frontend_web` together. The `backend/` directory is a separate Node.js prototype and is not connected to the Java backend or the active frontend.

## 🏁 Getting Started

### 1. Prerequisites
*   Docker & Docker Compose
*   Java 17+
*   Node.js 18+

### 2. Run with Docker
```bash
docker-compose up -d
```

### 3. Local Development
*   **Backend**: `npm run dev:backend-java` (Runs port 5000)
*   **Frontend**: `npm run dev:web` (Runs port 3000)

---

