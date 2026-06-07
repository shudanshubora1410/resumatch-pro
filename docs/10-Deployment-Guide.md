# Deployment Guide — ResuMatch Pro

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Docker & Docker Compose (for containerized deployment)

---

## Local Development Setup

### 1. Clone & Navigate
```bash
cd resumatch-pro
```

### 2. Initialize Database
```bash
mysql -u root -p < backend/src/main/resources/init.sql
```
This creates the `resumatch_pro` database with all 16 tables and indexes.
Default admin: `admin@resumatch.pro` / `Admin@123456`

### 3. Configure Application
Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resumatch_pro
spring.datasource.username=root
spring.datasource.password=your_password
app.jwt.secret=your-256-bit-secret-key-change-this
```

### 4. Run Backend
```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```
Backend starts on `http://localhost:8080/api`

### 5. Serve Frontend
Open `frontend/index.html` directly, or:
```bash
cd frontend
python3 -m http.server 3000
```
Frontend: `http://localhost:3000`

---

## Docker Deployment

### 1. Configure Environment
```bash
cp .env.example .env
# Edit .env with your secrets
```

### 2. Start All Services
```bash
docker-compose up -d
```
Services: MySQL (3306), MinIO (9000/9001), Backend (8080), Frontend (80)

### 3. Verify
- Health: `http://localhost:8080/api/actuator/health`
- Swagger: `http://localhost:8080/api/swagger-ui.html`
- Frontend: `http://localhost`

---

## Configuration Reference

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Backend port |
| `app.jwt.secret` | (required) | HS256 signing key (256+ bits) |
| `app.jwt.access-token-expiration-ms` | 900000 | 15 minutes |
| `app.jwt.refresh-token-expiration-ms` | 604800000 | 7 days |
| `app.storage.type` | local | `local` or `minio` |
| `app.minio.enabled` | false | Enable S3 storage |
| `spring.mail.username` | (empty) | SMTP email for notifications |
| `app.recommendation.max-results` | 5 | AI job recommendations count |

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| MySQL connection refused | Ensure MySQL is running and credentials match |
| File upload fails | Check `spring.servlet.multipart.max-file-size=5MB` |
| JWT token invalid | Regenerate `app.jwt.secret` with 256+ bit key |
| MinIO not connecting | Set `app.minio.enabled=false` for local storage |
| Emails not sending | Configure `spring.mail.*` properties or leave blank to skip |
