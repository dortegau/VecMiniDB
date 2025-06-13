# VecMiniDB Project Rules

## Code Style Rules

### Guard Clauses
- **ALWAYS use guard clauses** instead of nested if-else statements
- Place all validation and early returns at the beginning of methods
- Use descriptive error messages in guard clauses

**Example:**
```java
// ✅ GOOD - Guard clauses
public void processVector(Vector vector) {
    if (vector == null) throw new IllegalArgumentException("Vector cannot be null");
    if (vector.values().length == 0) throw new IllegalArgumentException("Vector cannot be empty");
    if (vector.id().isEmpty()) throw new IllegalArgumentException("Vector ID cannot be empty");
    
    // Main logic here
    doProcessing(vector);
}

// ❌ BAD - Nested if-else
public void processVector(Vector vector) {
    if (vector != null) {
        if (vector.values().length > 0) {
            if (!vector.id().isEmpty()) {
                doProcessing(vector);
            } else {
                throw new IllegalArgumentException("Vector ID cannot be empty");
            }
        } else {
            throw new IllegalArgumentException("Vector cannot be empty");
        }
    } else {
        throw new IllegalArgumentException("Vector cannot be null");
    }
}
```

### General Java Rules
- Use record classes when possible for immutable data
- Prefer defensive copying for arrays in public APIs
- Use @Serial annotation for serialVersionUID
- Apply proper null checks and validation
- Use meaningful variable and method names

### Testing Rules
- Write comprehensive tests for all public methods
- Test edge cases (null, empty, boundary values)
- Use descriptive test method names
- Group related assertions in single test methods

### Documentation Rules
- Add JavaDoc for public APIs
- Include examples in complex method documentation
- Document any non-obvious behavior or constraints

## Project Specific Rules

### Hexagonal Architecture
- **Domain Layer**: Pure business logic, no external dependencies
  - Entities: Core business objects (Vector)
  - Value Objects: Immutable data containers (VectorId, VectorData)
  - Domain Services: Business rules (SimilarityCalculator implementations)
- **Application Layer**: Use cases and ports
  - Ports In: Define what the application can do (use cases)
  - Ports Out: Define what the application needs (repositories)
  - Use Cases: Orchestrate domain logic and external interactions
- **Infrastructure Layer**: External concerns
  - Adapters In: Controllers, facades (VectorDatabaseFacade)
  - Adapters Out: Repositories, external services (FileVectorRepository)
  - Configuration: Dependency injection (VectorDatabaseFactory)

### Architecture Rules
- **Dependency Direction**: Always point inward (Infrastructure → Application → Domain)
- **No Circular Dependencies**: Outer layers depend on inner layers, never reverse
- **Interface Segregation**: Small, focused interfaces in application ports
- **Dependency Injection**: Use factory pattern for wiring components
- **Guard Clauses**: Always validate inputs at layer boundaries

### Vector Database
- Always use SimilarityCalculator interface for new similarity algorithms
- Ensure thread safety in database operations
- Maintain backward compatibility in API changes
- Use defensive copying for vector data
- Create new similarity algorithms in domain.services package

### Testing Strategy
- **Unit Tests**: Test each layer in isolation using mocks
- **Integration Tests**: Test adapter implementations with real infrastructure
- **Domain Tests**: Test business logic without external dependencies
- **Application Tests**: Test use cases with mocked repositories

### Package Structure
```
com.dortegau.vecminidb/
├── domain/
│   ├── entities/        # Core business objects
│   ├── valueobjects/    # Immutable data containers
│   └── services/        # Domain services and interfaces
├── application/
│   ├── ports/
│   │   ├── in/         # Use case interfaces
│   │   └── out/        # Repository interfaces
│   └── usecases/       # Use case implementations
└── infrastructure/
    ├── adapters/
    │   ├── in/         # Controllers, facades
    │   └── out/        # Repository implementations
    └── config/         # Configuration and factories
```

### Build Commands
- Tests: `./mvnw test`
- Compile: `./mvnw compile`
- Clean: `./mvnw clean`
- Run Examples: `java -cp "target/classes" com.dortegau.vecminidb.examples.SimilarityComparison`

## Code Quality Standards
- Maintain immutability where possible
- Use builder pattern for complex object creation
- Prefer composition over inheritance
- Follow SOLID principles
- Apply hexagonal architecture principles consistently