# Denari-Manager-Service

RESTful payment processing API with Modern Treasury ACH integration and automated rent-splitting

🔗 **Related:** [Frontend](https://github.com/IsaacRamirez108/Benifit-Manager-Application)

## 🚀 Tech Stack

- Framework: Spring Boot
- Language: Java 17
- Database: PostgreSQL
- Payment Processing: Modern Treasury API
- Authentication: JWT with Twilio OTP
- Security: Spring Security, AES-GCM encryption

## 🏗️ Architecture

denari-backend/
├── src/main/java/com/denari/
│   ├── controllers/      # REST endpoints
│   ├── services/         # Business logic
│   ├── repositories/     # Data access
│   ├── models/           # JPA entities
│   ├── dto/              # Data transfer objects
│   ├── config/           # Spring configuration
│   └── webhooks/         # Modern Treasury webhooks
└── src/main/resources/
└── application.yml   # Configuration

## ✨ Key Features

- ✅ Automated payment splitting (50% + 50% + $15 service fee)
- ✅ Async webhook processing with thread pool executor
- ✅ JWT authentication with OTP verification
- ✅ AES-encrypted PII storage
- ✅ Idempotent payment processing
- ✅ Retry logic for failed payments (NSF scenarios)

## 🔐 Security Features

- JWT-based authentication with Bearer tokens
- Twilio OTP verification (6-digit codes)
- AES-GCM encryption for SSN storage
- HMAC SHA-256 webhook signature validation
- Rate limiting on sensitive endpoints
- CORS configuration for mobile app

## 🔧 Local Setup

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Maven 3.8+

### Installation

1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/denari-backend.git
cd denari-backend
```

2. Configure database
```bash
# Create PostgreSQL database
createdb denari_db
```

3. Set environment variables
```bash
# Create application-local.properties
cat > src/main/resources/application-local.properties << 'ENV'
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/denari_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# Modern Treasury
modern.treasury.api.key=your_api_key
modern.treasury.organization.id=your_org_id

# Twilio
twilio.account.sid=your_account_sid
twilio.auth.token=your_auth_token
twilio.verify.service.sid=your_service_sid

# JWT
jwt.secret=your_secret_key
ENV
```

4. Run the application
```bash
mvn spring-boot:run
```

API will be available at `http://localhost:8080`

## 📚 API Documentation

### Authentication
```http
POST /api/auth/send-otp
POST /api/auth/verify-otp
GET  /api/auth/me
```

### Onboarding
```http
POST /api/onboarding/personal-info
POST /api/onboarding/address
POST /api/onboarding/rental-data
POST /api/onboarding/property-manager
POST /api/onboarding/identity-verification
POST /api/onboarding/payment-schedule
POST /api/onboarding/connect-bank-account
GET  /api/onboarding/summary
```

### Payments
```http
GET  /api/payments/history
GET  /api/payments/upcoming
PUT  /api/payments/schedule
```

### Webhooks
```http
POST /api/webhooks/modern-treasury
```
