# Open Billing System

A free, open-source billing system for small shops (grocery, medical, mobile and general stores).
Each shop runs its own copy: add products (or import them from a CSV file), make GST-aware bills
and download them as PDF.

![Billing screen](docs/screenshot.png)

## Features

- Product management and **CSV import** (duplicate SKUs are skipped, bad rows are reported)
- Billing with many items, GST per product, automatic stock reduction (all-or-nothing transaction)
- The price at the time of sale is stored on every bill, so old bills never change
- **PDF bill** with shop details and customer details
- **Admin and cashier roles**, passwords stored as BCrypt hashes
- Only the admin can change prices, and every change is logged (who, when, old price, new price)
- **Search with pagination** for products, bills and sold items
- Simple browser billing screen (search products, choose quantities, create the bill)
- Unit tests with JUnit 5 and Mockito

## Tech stack

Java 17, Spring Boot 3, Spring Data JPA, Spring Security, MySQL 8, OpenPDF, JUnit 5, Mockito.

## Quick start

**You need:** JDK 17 or newer and MySQL 8.

**1. Create the database**

```sql
CREATE DATABASE billing_db;
CREATE USER 'billing_user'@'localhost' IDENTIFIED BY 'choose-a-strong-password';
GRANT ALL PRIVILEGES ON billing_db.* TO 'billing_user'@'localhost';
```

**2. Configure** (environment variables)

| Variable | Meaning | Default |
|---|---|---|
| `DB_URL` | Database address | `jdbc:mysql://localhost:3306/billing_db` |
| `DB_USER` | Database user | `billing_user` |
| `DB_PASSWORD` | Database password | `changeme` (change it!) |
| `ADMIN_USERNAME` | First admin's name | `admin` |
| `ADMIN_PASSWORD` | First admin's password | empty: a random password is printed once at first start |

You can also put your own values in a file named `local.properties` in the project folder
(for example `spring.datasource.password=...`). It is ignored by Git.

**3. Run**

Windows:

```
set DB_PASSWORD=choose-a-strong-password
mvnw.cmd spring-boot:run
```

Linux or macOS:

```
export DB_PASSWORD=choose-a-strong-password
./mvnw spring-boot:run
```

Open `http://localhost:8080/` and log in with the admin account.

## First use

1. As admin, set your shop details (`PUT /api/shop`: shopName, address, phone, gstin, footerText).
2. Import your products from a CSV file (`POST /api/products/import`, form field `file`):

```
sku,name,price,stock
RICE01,Basmati Rice 1kg,95.50,40
SUGAR01,Sugar 1kg,44.00,60
```

3. Create cashier accounts (`POST /api/users`).
4. Make bills on the billing screen, then open the PDF.

## API overview

| Method and URL | Who | What |
|---|---|---|
| `POST /api/products` | admin | Add a product |
| `POST /api/products/import` | admin | Import products from CSV |
| `GET /api/products`, `GET /api/products/{id}` | admin, cashier | View products |
| `PUT /api/products/{id}/price` | admin | Change a price (logged) |
| `GET /api/products/{id}/price-history` | admin, cashier | Price change log |
| `POST /api/bills` | admin, cashier | Create a bill |
| `GET /api/bills`, `GET /api/bills/{id}` | admin, cashier | View bills |
| `GET /api/bills/{id}/pdf` | admin, cashier | Bill as PDF |
| `PUT /api/shop`, `GET /api/shop` | admin (PUT), both (GET) | Shop details |
| `POST /api/users` | admin | Create a cashier |
| `GET /api/search/products` | admin, cashier | Search and page products |
| `GET /api/search/bills` | admin, cashier | Search and page bills |
| `GET /api/search/transactions` | admin, cashier | Search and page sold items |

Login uses HTTP Basic authentication. Use HTTPS if the software is reachable over the internet.

## Run the tests

```
mvnw.cmd test
```

(The default test starts the app, so the database must be configured as above.)

## Roadmap

Docker Compose setup, email or authenticator-app (TOTP) verification for price changes, returns and refunds,
daily sales reports, batch and expiry tracking for medical shops.

## Note on GST

The software calculates GST per product, but it is not a certified tax invoicing tool.
Please check the legal invoice requirements with your accountant.

## Licence

MIT, see the `LICENSE` file.
