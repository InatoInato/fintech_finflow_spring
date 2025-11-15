# FinFlow – Modern Wallet & Transaction API

FinFlow is a clean and production-ready **Spring Boot 3** backend project that provides user authentication, wallet management, and transaction handling (deposit, withdraw, transfer). It is fully documented with Swagger/OpenAPI and runs via Docker.

This README explains how to run the project locally, how the API works, and how to test everything.

---

## 🚀 Features

* **User Authentication** (Register/Login with JWT)
* **Wallet Management** (multiple wallets per user)
* **Transactions**:

  * Deposit
  * Withdraw
  * Wallet-to-wallet transfer
* **Validation** with Jakarta Validation
* **Global Error Handling** (400/401/403/404/500)
* **Swagger UI** auto-generated OpenAPI documentation
* **Dockerized** for easy launch
* Clean project structure with services, DTOs, repositories

---

## 🛠 Technologies

* Java 21
* Spring Boot 3
* Spring Security (JWT)
* PostgreSQL
* Docker + Docker Compose
* Swagger / Springdoc OpenAPI
* Lombok

---

## 📦 How to Run the Project

!!! Requires Docker Desktop (with Linux containers, WSL 2 recommended) !!!

### **1. Clone the repository**

```
git clone https://github.com/your/repo.git
cd finflow
```

### **2. Start the backend using Docker Compose**

```
docker compose up --build
```

This will start:

* `backend` (Spring Boot)
* `postgres` database

Backend runs on:

```
http://localhost:8080
```

---

## 📖 API Documentation (Swagger)

After running Docker, open:

```
http://localhost:8080/swagger-ui/index.html
```

This shows all endpoints with ability to test requests.

---

## 🔐 Authentication Flow

### **Register**

```
POST /api/v1/auth/register
```

Body:

```json
{
  "email": "user@example.com",
  "password": "strongpassword"
}
```

### **Login**

```
POST /api/v1/auth/login
```

Response returns JWT:

```json
{
  "token": "your.jwt.token"
}
```

Copy and use this token for all authorized requests:

```
Authorization: Bearer <token>
```

---

## 👛 Wallet Endpoints

After registration wallet will create automaticly :)

### **Get all wallets of current user**

Use jwt token!!!

```
GET /api/v1/wallet 
```

---

## 💸 Transaction Endpoints

### **Transfer / Deposit / Withdraw**

```
POST /api/v1/transaction
```

All three operations use the same endpoint:

#### **Transfer**

```json
{
  "fromWalletId": 1,
  "toWalletId": 2,
  "amount": 100
}
```

#### **Deposit**

```json
{
  "toWalletId": 2,
  "amount": 50
}
```

#### **Withdraw**

```json
{
  "fromWalletId": 1,
  "amount": 20
}
```

### Error cases automatically handled:

* 400 – insufficient balance
* 403 – wallet does not belong to user
* 404 – wallet not found
* 401 – no token

---

## 🗂 Project Structure

```
finflow/
│
├─ auth/
│  ├─ controller/
│  ├─ service/
│  ├─ repository/
│  └─ dto/
│
├─ wallet/
│  ├─ controller/
│  ├─ service/
│  ├─ repository/
│  └─ entity/
│
├─ transaction/
│  ├─ controller/
│  ├─ service/
│  ├─ repository/
│  └─ entity/
│
├─ exception/
├─ config/
└─ security/
```

---

## 🐳 Docker Compose

The project uses PostgreSQL with default dev credentials:

```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_DB=finflow
```

You can modify them in `docker-compose.yml`.

---

## 🧪 Testing with Swagger or Postman

* Register → Login → Copy token
* Create wallet(s)
* Send transaction requests
* View errors / success responses

Everything is interactive in Swagger.

---

## 📬 Contact

If you want to contribute, open a pull request or create an issue.

---

FinFlow is designed as a clean, production-style portfolio backend project — ideal for interviews, showcasing code quality, and real-world reasoning.
