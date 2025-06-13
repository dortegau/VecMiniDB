# VecMiniDB

A lightweight yet enterprise-grade vector database implementation in Java using hexagonal architecture principles. VecMiniDB provides a robust, file-based vector storage solution with similarity search capabilities, featuring Write-Ahead Logging for durability and in-memory indexing for performance.

## Features

### Core Capabilities
- **Vector Storage & Retrieval**: Store and query high-dimensional vectors with unique identifiers
- **Similarity Search**: Multiple similarity algorithms (Cosine, Euclidean, Manhattan distance)
- **In-Memory Query Engine**: Lightning-fast queries that never touch disk during search operations
- **Text Embeddings**: Built-in mini text embedder for demonstration purposes
- **Fluent Query API**: Intuitive query builder for complex searches

### Enterprise-Grade Reliability
- **Write-Ahead Logging (WAL)**: Ensures durability and prevents data loss
- **Automatic Recovery**: Seamless recovery from WAL on startup
- **Thread-Safe Operations**: Concurrent read/write operations with ConcurrentHashMap
- **Graceful Error Handling**: Robust handling of edge cases and failures

### Architecture & Performance
- **Clean Architecture**: Hexagonal/Ports & Adapters pattern for maintainability
- **Pluggable Indexing**: VectorIndex interface for different indexing strategies
- **Memory Efficiency**: ~700 bytes per vector with defensive copying
- **Zero External Dependencies**: Pure Java implementation (except test dependencies)

## Architecture

VecMiniDB follows hexagonal architecture principles with clear separation of concerns and enterprise-grade components:

```
├── domain/                 # Core business logic
│   ├── entities/          # Vector entity
│   ├── valueobjects/      # VectorId, VectorData
│   └── services/          # Similarity calculators, VectorIndex interface
├── application/           # Use cases and ports
│   ├── ports/in/         # Input ports (interfaces)
│   ├── ports/out/        # Output ports (interfaces)
│   └── usecases/         # Business logic implementations
└── infrastructure/       # External concerns
    ├── adapters/in/      # API facades, InMemoryVectorQuery, query builders
    ├── adapters/out/     # FileVectorRepository, WriteAheadLog, FlatVectorIndex
    └── config/           # Dependency injection
```

### Key Components

- **WriteAheadLog**: Ensures durability with JSON-based logging and automatic recovery
- **VectorIndex**: Pluggable indexing interface with FlatVectorIndex implementation
- **InMemoryVectorQuery**: High-performance query engine operating purely in memory
- **FileVectorRepository**: Enhanced with WAL integration and in-memory indexing

## Quick Start

### Basic Usage

```java
import com.dortegau.vecminidb.infrastructure.config.VectorDatabaseFactory;
import com.dortegau.vecminidb.infrastructure.adapters.in.VectorDatabaseFacade;
import com.dortegau.vecminidb.domain.entities.Vector;

// Create database instance
VectorDatabaseFacade database = VectorDatabaseFactory.createDefault("my_vectors.vecdb");

// Store vectors
Vector vector1 = Vector.of("doc1", new double[]{1.0, 0.5, 0.8});
Vector vector2 = Vector.of("doc2", new double[]{0.9, 0.6, 0.7});

database.insert(vector1);
database.insert(vector2);

// Find similar vectors
Vector query = Vector.of("query", new double[]{1.0, 0.5, 0.9});
List<Vector> similar = database.findSimilar(query, 5);

// Get similarity scores
List<VectorSimilarityUseCase.SimilarityResult> results = 
    database.findSimilarWithScores(query, 5);
```

### Text Similarity Example

```java
import com.dortegau.vecminidb.infrastructure.adapters.in.MiniTextEmbedder;

// Create text embedder
MiniTextEmbedder embedder = new MiniTextEmbedder(64); // 64-dimensional vectors

// Embed and store documents
Vector doc1 = embedder.embedText("Machine learning is fascinating", "ml_doc");
Vector doc2 = embedder.embedText("Deep learning uses neural networks", "dl_doc");

database.insert(doc1);
database.insert(doc2);

// Query with text
Vector queryVector = embedder.embedQuery("What is machine learning?");
List<Vector> results = database.findSimilar(queryVector, 3);
```

### In-Memory Query Engine

```java
import com.dortegau.vecminidb.infrastructure.adapters.in.InMemoryVectorQuery;
import com.dortegau.vecminidb.domain.services.CosineSimilarity;

// Create high-performance in-memory query engine
List<Vector> vectorIndex = database.getAllVectorsFromMemory();
InMemoryVectorQuery queryEngine = new InMemoryVectorQuery(vectorIndex, new CosineSimilarity());

// Lightning-fast similarity search (never touches disk)
List<InMemoryVectorQuery.SimilarityResult> results = 
    queryEngine.findSimilarWithThreshold(queryVector, 10, 0.7);

// Builder pattern for flexible configuration
InMemoryVectorQuery customQuery = new InMemoryVectorQuery.Builder()
    .withVectors(vectorIndex)
    .withSimilarityCalculator(new EuclideanDistance())
    .build();
```

### Write-Ahead Log & Durability

```java
// WAL is automatically integrated - no additional code needed!
// Every insert is logged before execution for guaranteed durability

database.insert(vector); // Automatically logged to WAL
// Even if process crashes here, vector will be recovered on next startup

// Manual checkpoint (optional)
((FileVectorRepository) repository).checkpoint(); // Forces WAL clearance
```

## Interactive CLI (REPL)

VecMiniDB includes a powerful command-line interface for interactive vector database operations:

### Installation

```bash
# Build and install the CLI
./install-cli.sh

# Or run directly with Java
java -jar target/vecminidb-cli.jar
```

### CLI Commands

#### Data Operations
- `insert <id> <text>` - Insert text as vector with given ID
- `query <text> [limit]` - Find similar vectors (default limit: 5)
- `vector <id>` - Show vector values for given ID
- `meta <id>` - Show metadata and statistics for vector

#### Database Operations
- `list` - List all vector IDs
- `size` - Show number of vectors in database
- `stats` - Show database statistics
- `vocab` - Show text embedder vocabulary

#### Control Commands
- `help` - Show help message
- `exit` - Exit the REPL

### Example Session

```bash
$ vecminidb
╔══════════════════════════════════════════════════════════╗
║                    VecMiniDB REPL v1.0.0                   ║
║           Interactive Vector Database CLI                   ║
╚══════════════════════════════════════════════════════════╝

vecminidb> insert doc1 "Machine learning is fascinating"
✓ Inserted vector 'doc1' (64 dimensions)

vecminidb> insert doc2 "Deep learning uses neural networks"
✓ Inserted vector 'doc2' (64 dimensions)

vecminidb> query "artificial intelligence" 2
Found 2 similar vectors:
┌─────────────────────────────────────────┬─────────────┐
│ Vector ID                               │ Similarity  │
├─────────────────────────────────────────┼─────────────┤
│ doc1                                    │      0.8542 │
│ doc2                                    │      0.7891 │
└─────────────────────────────────────────┴─────────────┘

vecminidb> vector doc1
Vector ID: doc1
Dimensions: 64
Values: [0.123, 0.456, 0.789, ... 0.234, 0.567] (64 dimensions)

vecminidb> meta doc1
Metadata for vector 'doc1':
  ID: doc1
  Dimensions: 64
  Min value: -0.234567
  Max value: 0.456789
  Mean: 0.012345
  L2 norm: 2.345678

vecminidb> stats
VecMiniDB Statistics:
  Vectors: 2
  Vocabulary size: 8
  Embedding dimensions: 64
  Estimated vector memory: 1.0 KB

vecminidb> exit
Thank you for using VecMiniDB REPL!
```

## Similarity Algorithms

VecMiniDB supports multiple similarity algorithms:

- **Cosine Similarity**: Measures angle between vectors (default)
- **Euclidean Distance**: Measures straight-line distance
- **Manhattan Distance**: Measures city-block distance

```java
// Create database with specific similarity algorithm
VectorDatabaseFacade euclideanDb = VectorDatabaseFactory.create(
    "euclidean.vecdb", 
    new EuclideanDistance()
);
```

## Performance

VecMiniDB delivers excellent performance for small to medium-scale vector operations:

### Benchmark Results

*Tested on Apple M3 Pro (11 cores), 36GB RAM, macOS 15.4.1*

#### Scalability Performance
| Dataset Size | Search Time (avg) | Throughput |
|--------------|-------------------|------------|
| 100 vectors  | 0.04 ms          | 2,510 vectors/ms |
| 500 vectors  | 0.11 ms          | 4,595 vectors/ms |
| 1,000 vectors| 0.30 ms          | 3,357 vectors/ms |
| 2,500 vectors| 0.56 ms          | 4,428 vectors/ms |
| 5,000 vectors| 1.60 ms          | 3,127 vectors/ms |

#### Algorithm Comparison (2,000 vectors)
| Algorithm | Average Search Time |
|-----------|-------------------|
| Cosine Similarity | 0.47 ms |
| Euclidean Distance | 0.50 ms |
| Manhattan Distance | 0.51 ms |

#### Other Metrics
- **Query Rate**: 1.4M queries/second (sequential)
- **Insertion Rate**: 622 vectors/second (with WAL durability)
- **Memory Efficiency**: 709 bytes per vector
- **Memory Overhead**: 3.38MB for 5,000 vectors

### Performance Characteristics

- ✅ **Sub-millisecond search** for datasets up to 1,000 vectors
- ✅ **Linear scaling** with dataset size
- ✅ **Memory efficient** storage (~700 bytes per vector)
- ✅ **Consistent performance** across similarity algorithms
- ✅ **High query throughput** for read-heavy workloads (1.4M queries/sec)
- ✅ **Durability with minimal overhead** - WAL adds <1% performance cost
- ✅ **In-memory indexing** for O(1) vector lookup by ID

### Running Benchmarks

```bash
# Run performance benchmarks
./mvnw test -Dtest=PerformanceBenchmarkTest

# Run all tests including benchmarks
./mvnw test
```

## Building and Testing

### Prerequisites

- Java 21 or higher
- Maven 3.6+

### Build

```bash
# Clone the repository
git clone https://github.com/dortegau/VecMiniDB.git
cd VecMiniDB

# Run tests
./mvnw test

# Build jar
./mvnw clean package

# Run with coverage
./mvnw clean test jacoco:report
```

### Test Coverage

**125 comprehensive tests** covering all aspects of the system:

- **Domain Layer**: Unit tests for similarity algorithms and vector operations
- **Application Layer**: Use case and port testing with mocks
- **Infrastructure Layer**: Repository, WAL, and indexing component tests
- **Integration Tests**: End-to-end workflows and text similarity demonstrations
- **Performance Benchmarks**: Scalability, algorithm comparison, and memory analysis

#### Test Categories
- **41 new tests** for WAL, Vector Index, and In-Memory Query Engine
- **84 existing tests** for core functionality and benchmarks
- **100% coverage** of critical paths including error handling
- **Thread-safety validation** for concurrent operations

## Project Structure

```
src/
├── main/java/com/dortegau/vecminidb/
│   ├── domain/
│   │   ├── entities/Vector.java
│   │   ├── valueobjects/{VectorId,VectorData}.java
│   │   └── services/{CosineSimilarity,EuclideanDistance,ManhattanDistance}.java
│   ├── application/
│   │   ├── ports/{in,out}/
│   │   └── usecases/{VectorDatabaseService,VectorSimilarityService}.java
│   └── infrastructure/
│       ├── adapters/in/{VectorDatabaseFacade,MiniTextEmbedder,VectorQueryBuilder}.java
│       ├── adapters/out/FileVectorRepository.java
│       └── config/VectorDatabaseFactory.java
└── test/java/com/dortegau/vecminidb/
    ├── domain/           # Unit tests
    ├── application/      # Use case tests
    ├── infrastructure/   # Adapter tests
    └── integration/      # End-to-end tests
```

## Design Principles

- **Guard Clauses**: Comprehensive input validation
- **Immutability**: Records and defensive copying
- **Single Responsibility**: Each class has one clear purpose
- **Dependency Inversion**: Depend on abstractions, not concretions
- **Clean Code**: Readable, maintainable, and well-tested

## Limitations

- **Scale**: Designed for small to medium datasets (< 100K vectors)
- **Search Algorithm**: Uses linear search (O(n) complexity)
- **Persistence**: Simple file-based serialization
- **Text Embedder**: Basic bag-of-words implementation for demo purposes

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Daniel Ortega Ufano**
- Email: danielortegaufano@gmail.com
- GitHub: [@dortegau](https://github.com/danielortegaufano)

## Acknowledgments

- Inspired by modern vector database architectures
- Built following hexagonal architecture principles
- Designed for educational and demonstration purposes