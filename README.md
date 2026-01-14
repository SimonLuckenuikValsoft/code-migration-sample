# Order Entry Application - Migration Assignment

## Overview

This is a legacy Java desktop application for order entry. **Your task is to migrate this codebase to C# WPF** using AI assistance (LLMs like ChatGPT, Claude, or GitHub Copilot).

### What This Application Does

**Order Entry System** for managing customer orders:
- **Customer Management**: Add, edit, delete, search customers
- **Product Catalog**: View available products with prices  
- **Order Processing**: Create orders, add line items, calculate pricing
- **Pricing Engine**: Tiered discounts (5%/10%/15%), tax calculation (14.975%)
- **Data Persistence**: SQLite database for all data storage

## Quick Start (< 2 minutes)

**No Java knowledge required!** Just run one script:

### Linux/Mac:
```bash
./run.sh
```

### Windows:
```cmd
run.bat
```

The script will:
1. Install Java 11 if not present (Linux/Mac only - Windows requires manual install)
2. Download all dependencies automatically (including SQLite JDBC driver)
3. Initialize the SQLite database with sample data
4. Launch the desktop application

**That's it!** You should see the Order Entry application window.

## Exploring the Application

Before migrating, familiarize yourself with the functionality:

1. **Customers Tab**: Browse customer list, add/edit customers
2. **Orders Tab**: View existing orders, create new orders
3. **Order Editor**: 
   - Select a customer
   - Add product line items
   - See live pricing calculations (subtotal, discount, tax, total)
   - Save the order

## Understanding the Codebase

### Project Structure

```
src/main/java/aim/legacy/
├── db/              # Database access (SQLite)
├── domain/          # Data models (Customer, Product, Order, OrderLine)
├── ui/              # Swing UI screens
└── scenarios/       # Test scenario runner

orderentry.db        # SQLite database (created on first run)
scenarios/           # JSON test scenarios for validation
```

### Code Characteristics (Important for Migration)

This codebase uses **procedural, database-centric patterns** similar to Progress ABL:

1. **Direct SQL in UI Code**: No ORM, no repositories - SQL queries are embedded directly in event handlers
2. **String-Based SQL**: Queries built using string concatenation (SQL injection risk!)
3. **Procedural Flow**: FOR EACH-style loops, manual ResultSet iteration
4. **Temp Tables Pattern**: In-memory collections (`ArrayList<TempLine>`) simulate temporary tables
5. **Mixed Concerns**: Business logic, validation, and database access all in UI code
6. **No Comments**: Code has no explanatory comments (intentional)

**Why These Patterns?**
These patterns are common in legacy Progress ABL applications. The migration challenge is to:
- Extract business logic from UI code
- Replace direct SQL with Entity Framework or similar
- Implement proper separation of concerns (MVVM pattern)
- Add proper error handling and security

## Migration Task

### Your Goal

Migrate this Java Swing application to **C# WPF** while:

1. **Maintaining Functional Parity**: All features must work identically
2. **Improving Architecture**: Extract business logic, use MVVM, proper data access
3. **Using Modern Patterns**: Entity Framework, dependency injection, proper validation
4. **Preserving Business Rules**: Exact same pricing calculations and validation

### Validation

Use the scenario runner to verify pricing parity:

```bash
./mvnw -q exec:java@scenario-runner -Dexec.args="scenarios/pricing_scenarios.json"
```

This outputs canonical JSON with pricing calculations. Your C# version should produce identical output for the same inputs.

Example output:
```json
{
  "scenarioName": "ten_percent_discount_at_threshold",
  "result": {
    "discount": "100.00",
    "subtotal": "1000.00",
    "tax": "134.78",
    "total": "1034.78"
  },
  "validationErrors": null
}
```

### Business Rules to Preserve

**Pricing Calculation:**
1. Subtotal = Sum of (quantity × unit price) for all lines
2. Discount tiers based on subtotal:
   - ≥ $500: 5%
   - ≥ $1000: 10%
   - ≥ $2000: 15%
3. Tax = 14.975% applied to (subtotal - discount)
4. Total = subtotal - discount + tax
5. All monetary values rounded to 2 decimals using HALF_UP

**Validation Rules:**
- Customer is required
- At least 1 line item required
- Quantity must be positive
- Unit price must be ≥ 0
- Discount cannot exceed 15%

## Using LLMs for Migration

### Recommended Approach

1. **Understand First**: Run the Java app, explore the code
2. **Extract Business Logic**: Ask LLM to identify and extract pricing/validation logic
3. **Design C# Architecture**: Get LLM to design proper MVVM structure
4. **Migrate Incrementally**: One screen at a time
5. **Validate Continuously**: Run scenario tests after each component

### Effective LLM Prompts

**Example prompts to try:**

```
"Analyze this Java OrderEditorDialog class and extract the pricing calculation 
logic into a separate C# service class. The business rules are: [paste rules]"
```

```
"This Java code uses direct SQL queries. Convert it to C# using Entity Framework 
Core with SQLite, maintaining the same database schema."
```

```
"Create a C# WPF MVVM implementation of this Java Swing CustomersScreen, 
separating UI, view model, and data access properly."
```

### What Makes This Challenging for LLMs

- **Mixed concerns**: Business logic embedded in UI makes extraction difficult
- **String-based SQL**: LLMs must recognize SQL injection risks and modernize
- **Procedural patterns**: Translating FOR EACH-style loops to LINQ
- **Implicit dependencies**: No dependency injection makes dependencies hard to track
- **No separation**: Must infer proper architectural boundaries

## Database Schema

The SQLite database has 4 tables:

```sql
customer (cust_id, cust_name, email, phone, address)
product (prod_id, prod_name, unit_price)
orders (order_id, cust_id, cust_name, order_date, subtotal, discount, tax, total)
order_line (line_id, order_id, prod_id, prod_name, quantity, unit_price)
```

The database file `orderentry.db` is created automatically on first run with sample data:
- 5 customers
- 10 products  
- 2 sample orders

## Additional Commands

**Compile only:**
```bash
./mvnw compile
```

**Run scenario tests:**
```bash
./mvnw -q exec:java@scenario-runner -Dexec.args="scenarios/pricing_scenarios.json"
./mvnw -q exec:java@scenario-runner -Dexec.args="scenarios/validation_scenarios.json"
```

## Success Criteria

Your C# migration is successful when:

1. ✅ All UI screens function identically to Java version
2. ✅ Scenario runner outputs match exactly (pricing parity)
3. ✅ Code uses proper MVVM architecture
4. ✅ Data access uses Entity Framework (not raw SQL)
5. ✅ Business logic is separated from UI
6. ✅ Validation is centralized and reusable
7. ✅ No SQL injection vulnerabilities

## Tips

- **Start simple**: Migrate the Customer screen first (simpler than Orders)
- **Test frequently**: Use the scenario runner to catch calculation errors early
- **Ask specific questions**: LLMs work better with focused, specific prompts
- **Review LLM output**: Don't blindly accept generated code - understand it
- **Iterate**: Refine your prompts based on LLM responses

Good luck with your migration!
