# Trade Finance Service

A REST API that models a trade finance workflow between importers, banks, and exporters.

## Process Flow

```
Importer  ──creates──►  Bank  ──approves──►  Exporter
                          │                      │
                     rejects (→ Importer     declines (→ Bank
                      can correct &            for re-evaluation)
                      resubmit)
```

| Status | Description |
|---|---|
| `SUBMITTED` | Created by importer, awaiting bank review |
| `BANK_APPROVED` | Bank approved, forwarded to exporter |
| `BANK_REJECTED` | Bank rejected, returned to importer for correction |
| `EXPORTER_ACCEPTED` | Exporter accepted with pricing & delivery details |
| `EXPORTER_DECLINED` | Exporter declined, returned to bank for re-evaluation |

---

## Tech Stack

- **Java 21**
- **Spring Boot 4.0.3**
- **Spring Data JPA + Hibernate**
- **MySQL** (default profile) / **H2 in-memory** (demo profile)
- **Lombok**
- **Bean Validation (jakarta.validation)**

---

## Running the Application

### Option A — H2 In-Memory (no setup required, recommended for reviewers)

No database installation needed. Data is pre-loaded automatically on startup.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

The app starts on **http://localhost:8080**.

You can inspect the database via the H2 console at **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:trade_finance`
- Username: `sa`
- Password: *(leave empty)*

---

### Option B — MySQL (default profile)

**Prerequisites:** MySQL 8+ running locally.

1. Create the database:
```sql
CREATE DATABASE trade_finance;
```

2. Update credentials in `src/main/resources/application.yml` if needed (default: `root` / `halogen1`).

3. Run:
```bash
./mvnw spring-boot:run
```

---

## Sample Data

Both profiles load `src/main/resources/data.sql` on startup, which pre-populates:

**Parties:**
| ID | Name | Type |
|---|---|---|
| 1 | Acme Imports SRL | IMPORTER |
| 2 | Global Traders SRL | IMPORTER |
| 3 | Banca Transilvania | BANK |
| 4 | ING Bank Romania | BANK |
| 5 | EuroExport GmbH | EXPORTER |
| 6 | Asia Pacific Goods Ltd | EXPORTER |

**Trade Requests** covering all possible statuses: `SUBMITTED`, `BANK_APPROVED`, `BANK_REJECTED`, `EXPORTER_ACCEPTED`, `EXPORTER_DECLINED`.

---

## Authentication

This service uses a simplified header-based identity model (no JWT/OAuth2). Each request must include:

| Header | Example | Description |
|---|---|---|
| `X-Party-Id` | `3` | The ID of the acting party |
| `X-Party-Type` | `BANK` | One of: `IMPORTER`, `BANK`, `EXPORTER` |

---

## API Reference

### Parties

#### List all parties
```
GET /parties
GET /parties?type=IMPORTER
```

#### Get party by ID
```
GET /parties/{id}
```

---

### Trade Requests

#### Create a trade request *(Importer)*
```
POST /trade-requests
Content-Type: application/json

{
  "importerId": 1,
  "bankId": 3,
  "exporterId": 5,
  "goodsDescription": "Industrial steel pipes, grade A",
  "quantity": 500,
  "currency": "EUR"
}
```

#### Approve a trade request *(Bank)*
```
POST /trade-requests/{id}/approve
X-Party-Id: 3
X-Party-Type: BANK
```
Allowed from: `SUBMITTED` or `EXPORTER_DECLINED`

#### Reject a trade request *(Bank)*
```
POST /trade-requests/{id}/reject
X-Party-Id: 3
X-Party-Type: BANK
Content-Type: application/json

{
  "reason": "Insufficient documentation provided"
}
```
Allowed from: `SUBMITTED` or `EXPORTER_DECLINED`

#### Accept a trade request *(Exporter)*
```
POST /trade-requests/{id}/exporter-accept
X-Party-Id: 5
X-Party-Type: EXPORTER
Content-Type: application/json

{
  "price": 75000,
  "deliveryDetails": "Delivery via DHL within 30 days, FOB Rotterdam"
}
```
Allowed from: `BANK_APPROVED`

#### Decline a trade request *(Exporter)*
```
POST /trade-requests/{id}/exporter-decline
X-Party-Id: 5
X-Party-Type: EXPORTER
Content-Type: application/json

{
  "reason": "Cannot fulfill order due to capacity constraints"
}
```
Allowed from: `BANK_APPROVED`

#### Update and resubmit *(Importer)*
Allowed only when status is `BANK_REJECTED`.
```
PUT /trade-requests/{id}
X-Party-Id: 1
X-Party-Type: IMPORTER
Content-Type: application/json

{
  "goodsDescription": "Updated description",
  "quantity": 300,
  "currency": "EUR",
  "bankId": 3,
  "exporterId": 6
}
```

---

### Transaction History

#### Get history for a trade request
```
GET /trade-requests/{id}/history
```

Returns all state transitions in chronological order, including actor, action, and timestamps.

---

### Views per actor

```
GET /trade-requests/importer    (X-Party-Id + X-Party-Type: IMPORTER)
GET /trade-requests/bank        (X-Party-Id + X-Party-Type: BANK)
GET /trade-requests/exporter    (X-Party-Id + X-Party-Type: EXPORTER)
```

- Importer sees all their requests
- Bank sees requests pending review (`SUBMITTED` or `EXPORTER_DECLINED`)
- Exporter sees requests awaiting their decision (`BANK_APPROVED`)

---

## Bonus: VAT Calculation Job

A scheduled job runs daily at **02:00 AM** and calculates **45% VAT** on all `EXPORTER_ACCEPTED` transactions that have a `finalPrice` set. The result is stored in the `vat_amount` field.

---

## Error Responses

All errors follow a consistent structure:

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "TRADE_REQUEST_NOT_FOUND",
  "message": "TradeRequest not found: 99",
  "path": "/trade-requests/99"
}
```

---

## Development time

Approximately **4–5 hours**.
