# Legacy Order Entry Application

A deliberately messy legacy-style Java desktop application demonstrating poor coding practices common in older enterprise systems. This application is designed as a technical assignment starter for migrating to .NET WPF, intentionally incorporating technical debt, tight coupling, and anti-patterns.

## Overview

This is a desktop "Order Entry" application for a small business with **intentionally poor design**:
- Customer management
- Product catalog  
- Order creation and editing with line items
- Pricing calculations with tiered discounts
- Tax calculation
- Order validation
- JSON-based persistence

**IMPORTANT**: This codebase intentionally violates best practices to simulate realistic legacy code!

## Prerequisites

- **JDK 11 or higher** (recommended)
- Code is compatible with **Java 8** language level
- Maven Wrapper is included - no Maven installation required

## Quick Start

### 1. Run the Desktop Application

**Note**: There are **NO unit tests** in this codebase - another example of technical debt!

```bash
./mvnw -q exec:java
```

This launches the Swing UI application. You can:
- Browse and search customers
- Create and edit orders
- Add/edit/remove order lines
- See pricing calculations in real-time
- Save and reload data from JSON files

### 2. Run the Scenario Runner

The scenario runner executes pricing and validation scenarios from JSON files and outputs canonical results (useful for parity testing after migration):

```bash
./mvnw -q exec:java@scenario-runner -Dexec.args="scenarios/pricing_scenarios.json"
```

Or for validation scenarios:

```bash
./mvnw -q exec:java@scenario-runner -Dexec.args="scenarios/validation_scenarios.json"
```

## Project Structure

```
src/main/java/aim/legacy/
├── domain/          # POJOs: Customer, Product, Order, OrderLine
├── ui/              # Swing screens with BUSINESS LOGIC embedded (bad!)
└── scenarios/       # Scenario runner (also messy)

data/                # JSON data files (customers, products, orders)
scenarios/           # Scenario JSON files for testing
```

**Note**: There is NO service layer, NO data access layer, NO tests - everything is in the UI!

## Business Rules

### Pricing Calculation
- **Subtotal**: Sum of all line totals (quantity × unit price)
- **Discount Tiers** (based on subtotal):
  - ≥ $500: 5% discount
  - ≥ $1,000: 10% discount
  - ≥ $2,000: 15% discount
- **Tax**: 14.975% (applied after discount)
- **Rounding**: All monetary values rounded to 2 decimals using HALF_UP

### Validation Rules
- Customer is required
- Order must have at least 1 line item
- Quantity must be a positive integer
- Unit price must be ≥ 0
- Discount cannot exceed 15%

## Data Persistence

The application uses JSON files with **NO data access layer**:
- File I/O code is directly in the UI classes (MainApp)
- Global static lists for data (terrible practice!)
- No abstraction, no repository pattern
- `customers.json`, `products.json`, `orders.json` under `data/` directory

Sample data is pre-loaded so the application runs immediately.

## Testing

**There are NO tests!** This is intentional technical debt to make migration more challenging.

## Scenario Runner for Parity Testing

The scenario runner has **duplicated business logic** - the same calculations are copy-pasted in multiple places! This is intentional technical debt.

Example scenario file structure:
```json
[
  {
    "scenarioName": "five_percent_discount",
    "customerId": 1,
    "lines": [
      {
        "productId": 1,
        "quantity": 1,
        "unitPrice": 500.00
      }
    ]
  }
]
```

## Technical Debt in This Codebase

This application is **intentionally poorly designed**. See [ARCHITECTURE_NOTES.md](ARCHITECTURE_NOTES.md) for details on:
- All the anti-patterns and bad practices used
- Why this code is terrible
- What needs to be refactored during migration
- How to extract proper architecture

## Building

To compile:

```bash
./mvnw compile
```

To create a JAR:

```bash
./mvnw package
```

## Why is this code so bad?

This is **intentional**! The codebase demonstrates common problems in legacy enterprise applications:
- ❌ No separation of concerns
- ❌ Business logic in UI code
- ❌ No data access layer
- ❌ No tests
- ❌ Global static state
- ❌ Copy-pasted code
- ❌ Hard-coded values
- ❌ Poor error handling

This makes the migration to .NET WPF more realistic and challenging!

## License

This is a sample application for educational and technical assessment purposes.