# Architecture Notes - Legacy Order Entry Application

## Purpose

This document describes the intentionally legacy architectural patterns used in this application, explains why they're considered "legacy," and provides guidance for migration to modern patterns (particularly for a C# WPF migration).

## Legacy Patterns Used (Intentional)

### 1. "God Service" Class

**Where**: `OrderService.java`

**Pattern**: Single service class handles all business operations (customers, products, orders). Uses singleton pattern for global access.

**Why it's legacy**:
- Violates Single Responsibility Principle
- Becomes a bottleneck as application grows
- Difficult to test in isolation
- Tight coupling to all repositories

**Modern alternative**: Separate services per domain (CustomerService, ProductService, OrderService) with dependency injection.

**Migration note**: In C# WPF, use separate service classes with interface-based dependency injection (ICustomerService, IOrderService, etc.).

### 2. Static Utility Methods

**Where**: `PricingCalculator.java`, `OrderValidator.java`

**Pattern**: Business logic implemented as static methods.

**Why it's legacy**:
- Cannot be mocked or substituted in tests
- Hard to extend or override behavior
- No polymorphism or interface implementation
- Global state can lead to threading issues

**Modern alternative**: Instance-based services with interfaces.

**Migration note**: Convert to instance methods on service classes with interfaces (IPricingCalculator, IOrderValidator).

### 3. Anemic Domain Model

**Where**: All domain POJOs in `domain/` package

**Pattern**: Domain objects are pure data containers with getters/setters, no behavior.

**Why it's legacy**:
- Business logic scattered across service classes
- Objects don't protect their own invariants
- Reduces encapsulation

**Modern alternative**: Rich domain models with behavior and validation.

**Migration note**: C# properties can help, but consider adding validation and business methods to domain objects.

### 4. Manual ID Generation

**Where**: All repository classes

**Pattern**: Repositories manually loop through collections to find max ID and increment.

**Why it's legacy**:
- Not thread-safe
- Inefficient for large datasets
- Prone to ID collisions

**Modern alternative**: Use database auto-increment or GUID generation.

**Migration note**: Use Entity Framework identity columns or Guid.NewGuid() in C#.

### 5. Direct Repository Access from Service

**Where**: `OrderService` directly creates and holds repository instances

**Pattern**: Service class directly instantiates and owns repositories.

**Why it's legacy**:
- Tight coupling
- Cannot swap implementations
- Difficult to test

**Modern alternative**: Dependency injection with repository interfaces.

**Migration note**: Use C# dependency injection container with ICustomerRepository, etc.

### 6. UI Tightly Coupled to Services

**Where**: All UI classes in `ui/` package

**Pattern**: UI components directly call service methods and update themselves.

**Why it's legacy**:
- Violates separation of concerns
- Business logic can leak into UI
- Hard to unit test UI logic
- Cannot easily change UI framework

**Modern alternative**: MVVM pattern with ViewModels mediating between UI and services.

**Migration note**: WPF works best with MVVM - create ViewModels for each screen, use data binding, INotifyPropertyChanged, and Commands.

### 7. Manual UI Construction

**Where**: All Swing dialogs and screens

**Pattern**: UI is constructed programmatically in code.

**Why it's legacy**:
- Verbose and error-prone
- Hard to visualize layout
- Difficult to maintain
- No designer support

**Modern alternative**: Declarative UI (XAML in WPF).

**Migration note**: Use XAML for UI definition in WPF with data binding to ViewModels.

### 8. Duplicated Logic

**Where**: Product lookup in `OrderService.addLineToOrder()` duplicates logic in `OrderLineDialog`

**Pattern**: Same logic implemented in multiple places.

**Why it's legacy**:
- Maintenance burden
- Inconsistency risk
- Violates DRY principle

**Modern alternative**: Extract to shared methods or services.

**Migration note**: Consolidate logic in ViewModels or shared services.

### 9. Exception-based Validation

**Where**: `OrderValidator` returns error lists, `ValidationException` thrown in service

**Pattern**: Mix of return values and exceptions for validation.

**Why it's legacy**:
- Inconsistent error handling approach
- Exceptions for flow control

**Modern alternative**: Result pattern or validation frameworks.

**Migration note**: Use FluentValidation in C# or implement Result<T> pattern.

### 10. In-place Object Mutation

**Where**: `PricingCalculator.calculateOrderPricing()` modifies order directly

**Pattern**: Methods mutate objects passed as parameters.

**Why it's legacy**:
- Side effects not obvious from method signature
- Hard to track state changes
- Makes testing difficult

**Modern alternative**: Immutable objects or explicit return values.

**Migration note**: Consider immutable records in C# or explicit result objects.

## Coupling Points to Address in Migration

### Data Access Layer
Current: JSON file I/O with manual serialization
Migration: Entity Framework with SQL Server or SQLite

### Business Logic Layer
Current: Static methods and singleton service
Migration: Instance-based services with interfaces, dependency injection

### Presentation Layer
Current: Swing with manual UI construction
Migration: WPF with MVVM pattern, XAML, data binding

### Pricing and Validation
Current: Static utility classes
Migration: Service classes with interfaces, possibly specification pattern for validation

## What Would Be Extracted for Modern Architecture

### 1. Interfaces
Define contracts:
- ICustomerRepository
- IProductRepository  
- IOrderRepository
- IPricingCalculator
- IOrderValidator

### 2. ViewModels (for WPF)
Create ViewModels for each screen:
- CustomerListViewModel
- OrderListViewModel
- OrderEditorViewModel
- Uses INotifyPropertyChanged
- Implements ICommand for actions

### 3. Dependency Injection Container
Register all services and repositories for injection.

### 4. Unit of Work Pattern
For transactional operations across multiple repositories.

### 5. Domain Events
For decoupling business logic (e.g., OrderCreated event).

### 6. Value Objects
For money calculations (Money class instead of BigDecimal/decimal).

## Migration Strategy Recommendations

### Phase 1: Backend First
1. Keep existing domain models initially
2. Replace repositories with Entity Framework
3. Create service interfaces
4. Implement dependency injection
5. Port business logic to instance methods
6. Verify with scenario runner (parity testing)

### Phase 2: ViewModels
1. Create ViewModels for each screen
2. Implement INotifyPropertyChanged
3. Create Commands for actions
4. Test ViewModels in isolation

### Phase 3: WPF UI
1. Create XAML layouts matching Swing screens
2. Bind to ViewModels
3. Implement data templates for lists
4. Add validation UI feedback

### Phase 4: Refinement
1. Add rich domain behavior
2. Implement specification pattern for validation
3. Add domain events
4. Improve error handling
5. Add logging and instrumentation

## Testing Strategy for Migration

### 1. Use Scenario Runner
The scenario runner provides canonical output for pricing and validation scenarios. Run the same scenarios in C# to verify identical behavior.

### 2. Unit Test Coverage
Ensure 100% coverage of business logic:
- Pricing calculations
- Validation rules
- Repository operations

### 3. Integration Tests
Test service layer with in-memory database.

### 4. UI Tests
Use WPF UI testing frameworks for automated UI testing.

## Conclusion

This application demonstrates common legacy patterns that would be refactored in a modern implementation. The tight coupling, static methods, and lack of abstraction make it a good candidate for practicing migration techniques while maintaining business logic integrity through comprehensive testing and scenario-based parity validation.
