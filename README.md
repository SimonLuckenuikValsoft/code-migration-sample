# Legacy Order Entry Application

A legacy-style Java desktop application for managing customer orders. This application demonstrates a typical older enterprise system and serves as a technical assignment starter for practicing migration to modern platforms like .NET WPF.

## What Does This Application Do?

This is a **desktop Order Entry System** for a small business. It allows users to:

- **Manage Customers**: Add, edit, delete, and search for customer records
- **Browse Products**: View the product catalog with prices
- **Create Orders**: Build orders by selecting customers and adding product line items
- **Calculate Pricing**: Automatically calculate order totals with:
  - Tiered discounts based on order subtotal (5%, 10%, or 15%)
  - Tax calculation (14.975%)
  - Automatic rounding to 2 decimal places
- **Validate Orders**: Ensure orders meet business rules before saving
- **Persist Data**: Save and load all data from JSON files

The application includes sample data (5 customers, 10 products, 2 orders) so you can start using it immediately without any setup.

## Getting Started

### Prerequisites

You need **Java 11 or higher** installed on your system. The code is compatible with Java 8 language level, but Java 11+ is recommended for running it.

### Quick Setup

We provide setup scripts that will check for Java and help you install it if needed:

**On Linux/Mac:**
```bash
chmod +x setup.sh
./setup.sh
```

**On Windows:**
```cmd
setup.bat
```

These scripts will:
1. Check if Java is installed
2. Guide you through installation if needed (or install it automatically on Linux)
3. Verify your setup is ready

### Manual Java Installation

If you prefer to install Java manually:

1. Download OpenJDK 11+ from: https://adoptium.net/
2. Install it following the instructions for your operating system
3. Verify installation: `java -version`

## Running the Application

### 1. Launch the Desktop Application

**On Linux/Mac:**
```bash
./mvnw -q exec:java
```

**On Windows:**
```cmd
mvnw.cmd -q exec:java
```

This starts the Swing-based desktop application. You'll see a window with menu navigation where you can:

1. **Browse Customers**: View the customer list, search by name, add/edit/delete customers
2. **Manage Orders**: Create new orders, edit existing ones, view order history
3. **Edit Order Details**: 
   - Select a customer
   - Add product line items (product, quantity, price)
   - See live calculation of subtotal, discount, tax, and total
   - Save the order

### 2. Compile the Code

If you want to compile without running:

```bash
./mvnw compile
```

### 3. Run the Scenario Runner (for Testing)

The application includes a scenario runner that processes test cases from JSON files. This is useful for validating pricing calculations:

```bash
./mvnw -q exec:java@scenario-runner -Dexec.args="scenarios/pricing_scenarios.json"
```

Or test validation scenarios:

```bash
./mvnw -q exec:java@scenario-runner -Dexec.args="scenarios/validation_scenarios.json"
```

## How the Application Works

### Data Storage

All data is stored in JSON files under the `data/` directory:
- `customers.json` - Customer information (ID, name, email, phone, address)
- `products.json` - Product catalog (ID, name, unit price)
- `orders.json` - Order records (customer, line items, totals, dates)

The application loads this data on startup and saves changes automatically when you add/edit/delete records.

### Project Structure

```
src/main/java/aim/legacy/
├── domain/          # Data classes: Customer, Product, Order, OrderLine
├── ui/              # Swing screens: MainApp, CustomersScreen, OrdersScreen, OrderEditorDialog
└── scenarios/       # ScenarioRunner for testing

data/                # JSON data files
scenarios/           # Test scenario files
```

## Business Rules

The application implements these business rules for order pricing:

### Pricing Calculation

1. **Subtotal**: Sum of all line totals (quantity × unit price)
2. **Discount Tiers** (based on subtotal):
  - ≥ $500: 5% discount
  - ≥ $1,000: 10% discount
  - ≥ $2,000: 15% discount
3. **Tax**: 14.975% (applied after discount)
4. **Rounding**: All monetary values rounded to 2 decimals using HALF_UP

### Validation Rules

Before an order can be saved, it must pass these validations:
- Customer is required
- Order must have at least 1 line item
- Quantity must be a positive integer
- Unit price must be ≥ 0
- Discount cannot exceed 15%

## Sample Data

The application comes with pre-loaded sample data:

- **5 Customers**: John Doe, Jane Smith, Bob Johnson, Alice Williams, Charlie Brown
- **10 Products**: Including Laptop ($1299.99), Smartphone ($899.99), Tablet ($599.99), Monitor ($349.99), Keyboard ($149.99), Mouse ($29.99), and more
- **2 Sample Orders**: Demonstrating typical order structures

You can modify, delete, or add to this data through the application UI.

## Technical Notes

### Why Does This Code Look Old?

This is a **legacy application** designed to simulate older enterprise systems. It uses patterns and practices common in applications built before modern frameworks:

- Direct file I/O instead of database
- Swing UI (older Java desktop framework)
- Simple architecture without complex abstractions
- Global state management
- Manual ID generation

This makes it an ideal candidate for migration exercises to modern platforms like .NET WPF.

### Maven Wrapper

The project includes Maven Wrapper (`mvnw` / `mvnw.cmd`), so you don't need to install Maven separately. The wrapper automatically downloads the correct Maven version and runs it.

## Building

To compile the code:

```bash
./mvnw compile
```

To create a JAR file:

```bash
./mvnw package
```

The JAR will be created in the `target/` directory.

## Troubleshooting

**Problem**: "Java not found" error
- **Solution**: Run the setup script (`setup.sh` or `setup.bat`) or install Java manually from https://adoptium.net/

**Problem**: Application window doesn't appear
- **Solution**: Make sure you're running the command from the project root directory and that Java 11+ is installed

**Problem**: Data changes aren't persisted
- **Solution**: Check that the `data/` directory exists and is writable

## Next Steps

After getting familiar with the application, you can:

1. Explore the codebase to understand how it works
2. Use the scenario runner to understand pricing calculations
3. Review ARCHITECTURE_NOTES.md for insights on the code structure
4. Consider how you would migrate this to a modern platform

## About This Project

This application is designed for educational purposes and technical assessments, particularly for practicing code migration from Java to .NET platforms.