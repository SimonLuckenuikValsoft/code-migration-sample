# Legacy Order Entry Application

A legacy-style Java desktop application demonstrating a typical enterprise order entry system. This application is designed as a technical assignment starter for migrating to .NET WPF, intentionally incorporating legacy patterns common in older Java enterprise applications.

## Overview

This is a desktop "Order Entry" application for a small business, featuring:
- Customer management
- Product catalog
- Order creation and editing with line items
- Pricing calculations with tiered discounts
- Tax calculation
- Order validation
- JSON-based persistence

## Prerequisites

- **JDK 11 or higher** (recommended)
- Code is compatible with **Java 8** language level
- Maven Wrapper is included - no Maven installation required

## Quick Start

### 1. Run Tests

```bash
./mvnw test
```

This runs the comprehensive test suite (20+ tests) covering pricing calculations, validation rules, and edge cases.

### 2. Run the Desktop Application

```bash
./mvnw -q exec:java
```

This launches the Swing UI application. You can:
- Browse and search customers
- Create and edit orders
- Add/edit/remove order lines
- See pricing calculations in real-time
- Save and reload data from JSON files

### 3. Run the Scenario Runner

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
├── services/        # Business logic: PricingCalculator, OrderValidator, OrderService
├── persistence/     # JSON repositories: CustomerRepository, ProductRepository, OrderRepository
├── ui/              # Swing screens and dialogs
└── scenarios/       # Scenario runner for parity testing

data/                # JSON data files (customers, products, orders)
scenarios/           # Scenario JSON files for testing
```

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

The application uses JSON files for persistence under the `data/` directory:
- `customers.json` - Customer records
- `products.json` - Product catalog
- `orders.json` - Order history

Sample data is pre-loaded so the application runs immediately.

## Testing

The test suite includes:
- Discount threshold tests (at and near boundaries)
- Tax calculation tests
- Rounding behavior tests (HALF_UP at half cents)
- Validation tests for all business rules
- Edge case tests

Run with: `./mvnw test`

## Scenario Runner for Parity Testing

The scenario runner is designed to support parity testing when migrating to another platform (e.g., C# WPF):

1. Create scenario JSON files with test cases
2. Run the scenario runner to get canonical output
3. Implement the same scenarios in the target platform
4. Compare outputs to ensure identical behavior

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

## Architecture Notes

This application intentionally uses legacy patterns typical of older Java enterprise applications. See [ARCHITECTURE_NOTES.md](ARCHITECTURE_NOTES.md) for details on:
- Legacy patterns used and why
- Tight coupling points
- What would be refactored in a modern application
- Migration considerations for C# WPF

## Building

To compile without running:

```bash
./mvnw compile
```

To create a JAR:

```bash
./mvnw package
```

## License

This is a sample application for educational and technical assessment purposes.