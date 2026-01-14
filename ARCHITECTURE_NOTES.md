# Architecture Notes - Legacy Order Entry Application

## Purpose

This document describes the **intentionally terrible** architectural patterns used in this application. This codebase is designed to simulate realistic legacy enterprise code with significant technical debt, making migration to C# WPF a meaningful challenge.

## Anti-Patterns and Bad Practices Used (Intentional)

### 1. NO Separation of Concerns

**Where**: Everywhere, but especially `OrderEditorDialog.java`, `MainApp.java`

**What's wrong**:
- Business logic (pricing calculations, validation) is embedded directly in UI event handlers
- File I/O code is mixed with UI code  
- No service layer, no data access layer
- Everything happens in one place

**Why it's bad**:
- Can't test business logic without UI
- Can't reuse calculations elsewhere (so we copy-paste them!)
- Changes to business rules require UI changes
- Makes migration extremely difficult

**How to fix during migration**: Extract all business logic to separate service classes with interfaces.

### 2. Global Static State

**Where**: `MainApp.java`

**What's wrong**: All data is stored in public static lists - global mutable state is a nightmare!

**How to fix during migration**: Create proper repository classes with dependency injection.

### 3. Duplicated Business Logic

**Where**: `OrderEditorDialog.java` and `ScenarioRunner.java`

**What's wrong**: The EXACT same pricing calculation code is copy-pasted in multiple places.

**How to fix during migration**: Create a single `PricingService` class used by both UI and scenario runner.

### 4. No Tests

**Where**: Nowhere - there are no tests!

**How to fix during migration**: Write comprehensive tests FIRST before migrating any code.

### 5. File I/O in UI Layer

**Where**: `MainApp.java` static methods - UI code directly writes to files!

### 6. Poor Error Handling

**Where**: Throughout - exceptions are caught and ignored.

### 7. Manual ID Generation

**Where**: Linear search through all items - not thread-safe, O(n) for each insert!

### 8. Hard-Coded Business Rules

**Where**: Tax rate and discount tiers hard-coded everywhere.

### 9. UI Code Directly Manipulates Domain Objects

**Where**: Domain objects are anemic (just getters/setters), no business logic in domain layer.

### 10. Linear Search for Everything

**Where**: Every lookup is O(n) - no HashMap or Dictionary usage.

## Key Lessons from This Codebase

**DO NOT** do any of the following in production code:
- ❌ Put business logic in UI code
- ❌ Use global static mutable state
- ❌ Copy-paste code instead of extracting methods
- ❌ Skip writing tests
- ❌ Swallow exceptions silently

**DO** these things during migration:
- ✅ Extract and test business logic FIRST
- ✅ Create proper abstractions (interfaces)
- ✅ Use dependency injection
- ✅ Implement MVVM for WPF
- ✅ Write comprehensive tests

## Conclusion

This codebase is a **deliberate disaster** to simulate realistic legacy enterprise applications. Every anti-pattern is intentional!

Good luck with the migration! You'll need it with this codebase! 😅
