# 🔗 URL Shortener (Spring Boot + Redis + PostgreSQL)

A production-style URL shortening service inspired by systems like Bitly.
Built to demonstrate backend system design concepts such as caching, rate limiting, and id generation.

---

## 🚀 Features

* Shorten long URLs → compact Base62 codes
* Redirect using short links
* PostgreSQL persistence (existing schema integration)
* Redis caching with TTL (cache-aside pattern)
* Rate limiting (Token Bucket algorithm via Bucket4j)
* URL validation and normalization
* Idempotent API (same URL → same short code)
* Click tracking (analytics)
* Swagger API documentation
* Unit + Controller + Integration tests

---

## 🏗️ Architecture

Client → API → Redis (cache) → PostgreSQL (source of truth)

* Cache-aside pattern for fast reads
* Database fallback on cache miss
* TTL ensures memory efficiency

---

## ⚙️ Tech Stack

* Java + Spring Boot
* PostgreSQL
* Redis
* Bucket4j (rate limiting)
* JPA / Hibernate
* Swagger (OpenAPI)
* JUnit + Mockito

---

## 📌 API Endpoints

### Create Short URL

POST /api/shorten

Request:
{
"url": "https://google.com"
}

Response:
{
"shortUrl": "http://localhost:8080/abc123"
}

---

### Redirect

GET /{code}

* 302 → Redirect to original URL
* 404 → Not found

---

## 🔥 Key Concepts Demonstrated

* Cache-aside pattern
* Id generation (Base62 encoding)
* Rate limiting (token bucket)
* Idempotent APIs
* Clean architecture (Controller → Service → Repository)
* Environment-based configuration (no secrets in repo)

---

## ▶️ Running Locally

1. Set environment variables:

DB_URL=...
DB_USER=...
DB_PASSWORD=...
REDIS_URL=...

2. Run app:

mvn spring-boot:run

3. Open Swagger:

http://localhost:8080/swagger-ui/index.html

---

## 📈 Future Improvements

* Distributed ID generation (Snowflake)
* Redis-based distributed rate limiting
* Docker + CI/CD
* Custom aliases for URLs
* Monitoring & logging

---
