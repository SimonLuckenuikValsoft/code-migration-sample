# Order Entry System

A desktop application for managing customer orders built with Java Swing and SQLite.

## About

This is a simple order management system that allows you to:
- Manage customer information
- Browse product catalog
- Create and manage customer orders
- Calculate order totals with automatic discounts and tax

## Requirements

- Java 11 or higher
- Maven (included via Maven Wrapper)

## Installation & Running

The easiest way to get started is to use the provided run script:

### Linux/Mac
```bash
./run.sh
```

### Windows
```cmd
run.bat
```

These scripts will automatically download all dependencies and start the application.

### Manual Run

If you prefer to run manually:

```bash
./mvnw compile
./mvnw exec:java
```

## Features

### Customer Management
- Add, edit, and view customer records
- Store contact information (name, email, phone, address)
- Search customers by name

### Product Catalog
- View available products
- Products include name and price information

### Order Processing
- Create new orders for customers
- Add multiple product line items to each order
- Automatic calculation of:
  - Subtotal (sum of all line items)
  - Discount (based on order value - see below)
  - Tax (14.975% applied after discount)
  - Grand total

### Discount Tiers

The system automatically applies discounts based on order subtotal:
- Orders over $600: 5% discount
- Orders over $1200: 10% discount  
- Orders over $2500: 15% discount

## Database

The application uses SQLite for data storage. The database file (`orderentry.db`) is automatically created on first run and includes sample data:
- 5 sample customers
- 10 sample products
- 2 sample orders

## Project Structure

```
src/main/java/aim/legacy/
├── db/          - Database connection and initialization
├── domain/      - Data model classes (Customer, Product, Order, OrderLine)
└── ui/          - Swing user interface screens
```

## Building from Source

```bash
./mvnw clean compile
```

## License

This is sample code for educational purposes.
