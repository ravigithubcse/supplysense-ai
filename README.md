<div align="center">

<img src="https://img.shields.io/badge/SupplySense_AI-v1.0.0-2563eb?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0id2hpdGUiIGQ9Ik05IDE5di02YTIgMiAwIDAwLTItMkg1YTIgMiAwIDAwLTIgMnY2YTIgMiAwIDAwMiAyaDJhMiAyIDAgMDAyLTJ6bTAgMFY5YTIgMiAwIDAxMi0yaDJhMiAyIDAgMDEyIDJ2MTBtLTYgMGEyIDIgMCAwMDIgMmgyYTIgMiAwIDAwMi0ybTAtMFY1YTIgMiAwIDAxMi0yaDJhMiAyIDAgMDEyIDJ2MTRhMiAyIDAgMDEtMiAyaC0yYTIgMiAwIDAxLTItMnoiLz48L3N2Zz4=" />

# SupplySense AI
### Predictive Supply Chain Risk Intelligence Platform

*Enterprise-grade, cloud-native platform that predicts supply chain disruptions before they happen using advanced ML*

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-17-DD0031?style=flat-square&logo=angular)](https://angular.io)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?style=flat-square&logo=python)](https://python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.109-009688?style=flat-square&logo=fastapi)](https://fastapi.tiangolo.com)
[![Kafka](https://img.shields.io/badge/Kafka-3.6-231F20?style=flat-square&logo=apachekafka)](https://kafka.apache.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://docker.com)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.29-326CE5?style=flat-square&logo=kubernetes)](https://kubernetes.io)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

[🚀 Quick Start](#quick-start) · [📐 Architecture](#architecture) · [✨ Features](#features) · [📡 API Docs](#api-documentation) · [👨‍💻 Author](#author)

</div>

---

## 🎯 What is SupplySense AI?

SupplySense AI is a **production-grade supply chain risk intelligence platform** that combines real-time event streaming, predictive machine learning, and an intuitive Angular dashboard to give procurement and supply chain teams a 7–30 day warning before disruptions hit.

> Built as a portfolio project to demonstrate full-stack enterprise engineering across microservices, ML integration, and cloud-native deployment.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔴 **Real-time Risk Scoring** | Composite 0–100 risk score updated every 5 minutes via Kafka + WebSocket |
| 🤖 **Predictive ML** | 7-day and 30-day disruption forecasts using LSTM + Prophet ensemble |
| 📰 **News Sentiment Analysis** | RoBERTa NLP scanning 100,000+ news sources per supplier |
| 🔍 **Anomaly Detection** | Isolation Forest + Autoencoder for shipping metric anomalies |
| 🗺️ **Interactive Risk Map** | Global supplier heatmap with live score bubbles |
| 🔔 **Multi-channel Alerts** | Email, SMS, Slack, and in-app notifications with severity routing |
| 🎭 **What-If Scenarios** | Simulate port closures, tariff hikes, natural disasters and their impact |
| 📊 **Analytics Dashboard** | Risk distribution charts, trend lines, and supplier drill-down |
| 🔐 **Enterprise Auth** | JWT + Redis token blacklisting, RBAC, MFA-ready |

---

## 📐 Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Angular 17 SPA (Port 4200)                   │
│              NgRx · Tailwind CSS · WebSocket · Chart.js              │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ HTTPS / WSS
┌───────────────────────────▼─────────────────────────────────────────┐
│              Spring Cloud Gateway (Port 8080)                        │
│         JWT Auth Filter · Rate Limiting · Circuit Breaker            │
└──┬──────────┬──────────┬──────────┬──────────┬──────────────────────┘
   │          │          │          │          │
┌──▼──┐  ┌───▼───┐  ┌───▼───┐  ┌──▼──┐  ┌───▼────┐  ┌────────────┐
│Auth │  │Supply │  │ Risk  │  │Event│  │Notif.  │  │ AI Service │
│:8081│  │Chain  │  │Engine │  │Proc.│  │:8085   │  │ :8090      │
│     │  │:8082  │  │:8083  │  │:8084│  │        │  │ FastAPI    │
└──┬──┘  └───┬───┘  └───┬───┘  └──┬──┘  └───┬────┘  │ PyTorch    │
   │         │          │         │          │       └────────────┘
   └─────────┴──────────┴────┬────┴──────────┘
                             │
        ┌────────────────────┼──────────────────────┐
        │                    │                       │
   ┌────▼────┐         ┌─────▼────┐           ┌─────▼─────┐
   │Postgres │         │  Kafka   │           │  Redis    │
   │+Timescale│        │  3.6     │           │  7.2      │
   └─────────┘         └──────────┘           └───────────┘
```

### Microservices

| Service | Port | Technology | Responsibility |
|---|---|---|---|
| **API Gateway** | 8080 | Spring Cloud Gateway | JWT auth, rate limiting, routing, circuit breaker |
| **Auth Service** | 8081 | Spring Boot + Security | Login/register, JWT/refresh tokens, RBAC |
| **Supply Chain Service** | 8082 | Spring Boot + JPA | Supplier/product/route CRUD, caching |
| **Risk Engine Service** | 8083 | Spring Boot + WebFlux | Risk orchestration, alerts, what-if scenarios |
| **Event Processor** | 8084 | Spring Boot + Kafka | Kafka→WebSocket bridge, real-time fan-out |
| **Notification Service** | 8085 | Spring Boot + Mail | Email, Slack, in-app multi-channel delivery |
| **AI Service** | 8090 | FastAPI + PyTorch | ML predictions, sentiment analysis, anomaly detection |
| **Frontend** | 4200 | Angular 17 + NgRx | SPA dashboard, real-time UI |

---

## 🚀 Quick Start

### Prerequisites
- Docker 24+ and Docker Compose v2
- 8 GB RAM minimum (16 GB recommended)

### One-command launch

```bash
git clone https://github.com/ravigithubcse/supplysense-ai.git
cd supplysense-ai/infrastructure/docker
docker compose up -d
```

### Access the stack

| Service | URL | Credentials |
|---|---|---|
| **Frontend** | http://localhost:4200 | admin@ss.ai / Admin1234! |
| **API Gateway** | http://localhost:8080 | — |
| **AI Service Docs** | http://localhost:8090/docs | — |
| **Kafka UI** | http://localhost:8180 | — |
| **Grafana** | http://localhost:3000 | admin / admin |
| **Kibana** | http://localhost:5601 | — |
| **Mailhog** | http://localhost:8025 | — |
| **MLflow** | http://localhost:5000 | — |

### Demo Accounts

```
Admin:   admin@ss.ai    / Admin1234!
Manager: manager@ss.ai  / Manager1234!
Analyst: analyst@ss.ai  / Analyst1234!
```

---

## 🛠️ Technology Stack

### Backend
| Layer | Technology |
|---|---|
| Language | Java 21 (Virtual Threads ready) |
| Framework | Spring Boot 3.2, Spring Security 6.2 |
| API Gateway | Spring Cloud Gateway 2023.0 |
| ORM | Spring Data JPA + Hibernate 6 |
| Migration | Flyway |
| Messaging | Apache Kafka 3.6 |
| Cache | Redis 7.2 |
| Auth | JWT (jjwt 0.12) + BCrypt |
| Docs | SpringDoc OpenAPI 3 |
| Metrics | Micrometer + Prometheus |

### AI / ML
| Layer | Technology |
|---|---|
| Language | Python 3.11 |
| Framework | FastAPI 0.109 + Uvicorn |
| Deep Learning | PyTorch 2.1 |
| NLP | HuggingFace Transformers (RoBERTa) |
| Forecasting | Prophet + LSTM ensemble |
| Anomaly Detection | Isolation Forest (scikit-learn) |
| Time Series | XGBoost |

### Frontend
| Layer | Technology |
|---|---|
| Framework | Angular 17 (Standalone Components) |
| State Management | NgRx 17 (Store + Effects + DevTools) |
| Styling | Tailwind CSS 3.4 |
| Real-time | STOMP over SockJS |
| Charts | Chart.js 4 |
| Maps | Mapbox GL 3 |
| HTTP | Angular HttpClient + Interceptors |

### Infrastructure
| Layer | Technology |
|---|---|
| Containers | Docker + Docker Compose |
| Orchestration | Kubernetes 1.29 |
| IaC | Terraform 1.7 (AWS EKS) |
| CI/CD | GitHub Actions |
| Monitoring | Prometheus + Grafana |
| Logging | ELK Stack (Elasticsearch + Kibana) |
| Tracing | Jaeger |
| Database | PostgreSQL 16 + TimescaleDB |

---

## 📡 API Documentation

All services expose Swagger UI:

```
http://localhost:8080/swagger-ui.html  # API Gateway
http://localhost:8081/swagger-ui.html  # Auth
http://localhost:8082/swagger-ui.html  # Supply Chain
http://localhost:8083/swagger-ui.html  # Risk Engine
http://localhost:8090/docs             # AI Service
```

### Key Endpoints

```http
POST /api/v1/auth/login                    # Authenticate
GET  /api/v1/suppliers?page=0&size=20      # List suppliers
GET  /api/v1/risk/scores                   # Latest risk scores
GET  /api/v1/risk/dashboard                # Dashboard KPIs
POST /api/v1/risk/scores/calculate         # On-demand risk calc
GET  /api/v1/risk/alerts?status=ACTIVE     # Active alerts
PATCH /api/v1/risk/alerts/{id}/resolve     # Resolve alert
POST /api/v1/risk/scenarios/what-if        # What-if simulation
POST /api/v1/predict/risk                  # AI risk prediction
GET  /api/v1/sentiment?supplierId=x        # News sentiment
```

---

## 🧪 Running Tests

```bash
# Backend (per service)
cd backend/auth-service && ./mvnw test

# AI service
cd ai-service && pytest tests/ -v --cov=app

# Frontend
cd frontend && npm test

# All backend services in parallel
for svc in api-gateway auth-service supply-chain-service risk-engine-service; do
  (cd backend/$svc && ./mvnw test -q) &
done
wait
```

---

## ☸️ Kubernetes Deployment

```bash
# Deploy to AWS EKS
cd infrastructure/kubernetes
kubectl apply -f base/namespace.yaml
kubectl apply -f base/deployments.yaml

# Check rollout
kubectl rollout status deployment/api-gateway -n supplysense
kubectl rollout status deployment/frontend -n supplysense
```

---

## 📁 Project Structure

```
supplysense-ai/
├── backend/
│   ├── api-gateway/          # Spring Cloud Gateway
│   ├── auth-service/         # JWT Authentication
│   ├── supply-chain-service/ # Supplier CRUD
│   ├── risk-engine-service/  # Risk orchestration + alerts
│   ├── event-processor-service/ # Kafka → WebSocket
│   └── notification-service/ # Multi-channel notifications
├── ai-service/               # FastAPI + ML models
│   ├── app/
│   │   ├── routers/          # predict, sentiment, anomaly
│   │   ├── services/         # model_registry (LSTM, RoBERTa, IF)
│   │   └── schemas/          # Pydantic request/response models
│   └── requirements.txt
├── frontend/                 # Angular 17 SPA
│   └── src/app/
│       ├── core/             # Services, guards, interceptors, models
│       └── features/         # dashboard, suppliers, risk-map, alerts, analytics
├── infrastructure/
│   ├── docker/               # docker-compose.yml + monitoring
│   └── kubernetes/           # K8s manifests
└── .github/workflows/        # GitHub Actions CI/CD
```

---

## 👨‍💻 Author

<div align="center">

### **Ravi Kumar**
*Full-Stack Software Engineer | AI-Integrated Product Development*

[![GitHub](https://img.shields.io/badge/GitHub-ravigithubcse-181717?style=for-the-badge&logo=github)](https://github.com/ravigithubcse)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=for-the-badge&logo=linkedin)](https://linkedin.com/in/ravi-kumar)
[![Email](https://img.shields.io/badge/Email-Available_for_Roles-EA4335?style=for-the-badge&logo=gmail)](mailto:ravi@example.com)

</div>

| | |
|---|---|
| 🎓 **Education** | B.E. Computer Science & Engineering, 2024 — CGPA **8.96** |
| 📍 **Location** | Bengaluru, India — **Immediately Available** |
| 💼 **Experience** | 1.5 years professional experience (Product Engineer) |
| 🛠️ **Core Stack** | Java · Spring Boot · Angular · Kafka · Redis · Docker · AWS |
| 🏆 **HackerRank** | Active competitive programmer |
| 📜 **Certifications** | Udemy, JSpiders — Full Stack & Cloud |

### Why I built SupplySense AI

This project demonstrates my ability to architect and deliver a **complete enterprise system** from scratch — spanning microservices design, real-time event streaming, ML model integration, a production Angular SPA, and cloud-native deployment. Every component reflects real engineering decisions I would make on the job.

> 🤝 **Open to roles in:** Product Engineering · Backend Engineering · Full-Stack Development · Java/Spring Boot · Angular · Bengaluru (on-site or hybrid)

### Other Portfolio Projects

| Project | Stack | Description |
|---|---|---|
| [adaptiveflow-ai](https://github.com/ravigithubcse/adaptiveflow-ai) | Spring Boot + Angular + AI | Adaptive workflow automation platform |
| [civicshield-ai](https://github.com/ravigithubcse/civicshield-ai) | Java + ML | Civic issue detection and routing |
| [SkillSync-AI](https://github.com/ravigithubcse/SkillSync-AI) | Spring Boot + NLP | AI-powered skill gap analysis tool |

---

<div align="center">

**⭐ Star this repo if it helped you · 🍴 Fork it · 📬 Reach out for collaboration**

*Built with ❤️ and lots of ☕ by Ravi Kumar*

</div>
