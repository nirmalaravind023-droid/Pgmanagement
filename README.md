# StayNear PG Rentals 🏠

A premium, location-based PG discovery and booking SaaS platform designed for students and working professionals. This project features a high-performance Java Spring Boot architecture integrated with official Google Maps APIs and professional-grade infrastructure.

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
    *   **Rate Limiting**: IP-based throttling using Bucket4j.
    *   **Stateless Auth**: JWT-based authentication with refresh tokens.

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

## 📈 Scalability & Systems Design

StayNear is designed with a **production-first** mindset:

1.  **Horizontal Scaling**: The stateless Java backend can be scaled to N instances behind a Load Balancer (Nginx/AWS ELB).
2.  **Geospatial Performance**: By using **PostGIS GIST indexes** instead of raw Haversine calculations, the system maintains O(log N) search performance even with millions of PGs.
3.  **Distributed Caching**: Using Redis for OTPs and session data allows for seamless scaling without losing user state.
4.  **Event-Driven Workers**: Heavy tasks (OTP, Image Processing, Analytics) are offloaded to **RabbitMQ workers**, preventing API latency.

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

