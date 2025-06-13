# VecMiniDB

A lightweight vector database implementation in Java using hexagonal architecture principles. VecMiniDB provides a simple, file-based vector storage solution with similarity search capabilities, perfect for local development and small to medium-scale applications.

## Features

- **Vector Storage & Retrieval**: Store and query high-dimensional vectors with unique identifiers
- **Similarity Search**: Multiple similarity algorithms (Cosine, Euclidean, Manhattan distance)
- **File-based Persistence**: Simple file-based storage with serialization
- **Clean Architecture**: Hexagonal/Ports & Adapters pattern for maintainability
- **Text Embeddings**: Built-in mini text embedder for demonstration purposes
- **Fluent Query API**: Intuitive query builder for complex searches
- **Zero External Dependencies**: Pure Java implementation (except test dependencies)

## Architecture

VecMiniDB follows hexagonal architecture principles with clear separation of concerns:

```
├── domain/                 # Core business logic
│   ├── entities/          # Vector entity
│   ├── valueobjects/      # VectorId, VectorData
│   └── services/          # Similarity calculators
├── application/           # Use cases and ports
│   ├── ports/in/         # Input ports (interfaces)
│   ├── ports/out/        # Output ports (interfaces)
│   └── usecases/         # Business logic implementations
└── infrastructure/       # External concerns
    ├── adapters/in/      # API facades, query builders
    ├── adapters/out/     # File repository
    └── config/           # Dependency injection
```

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

### Fluent Query API

```java
import com.dortegau.vecminidb.infrastructure.adapters.in.VectorQueryBuilder;
import com.dortegau.vecminidb.application.usecases.VectorSimilarityService;

VectorQueryBuilder queryBuilder = new VectorQueryBuilder(similarityService);

List<VectorSimilarityUseCase.SimilarityResult> results = queryBuilder
    .select()
    .similarTo(queryVector)
    .limit(10)
    .minSimilarity(0.7)
    .execute();
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
| 100 vectors  | 0.04 ms          | 2,243 vectors/ms |
| 500 vectors  | 0.16 ms          | 3,180 vectors/ms |
| 1,000 vectors| 0.28 ms          | 3,588 vectors/ms |
| 2,500 vectors| 0.63 ms          | 3,944 vectors/ms |
| 5,000 vectors| 1.55 ms          | 3,233 vectors/ms |

#### Algorithm Comparison (2,000 vectors)
| Algorithm | Average Search Time |
|-----------|-------------------|
| Cosine Similarity | 0.46 ms |
| Euclidean Distance | 0.51 ms |
| Manhattan Distance | 0.48 ms |

#### Other Metrics
- **Query Rate**: 1.5M queries/second (sequential)
- **Insertion Rate**: 639 vectors/second
- **Memory Efficiency**: 671 bytes per vector
- **Memory Overhead**: 3.2MB for 5,000 vectors

### Performance Characteristics

- ✅ **Sub-millisecond search** for datasets up to 1,000 vectors
- ✅ **Linear scaling** with dataset size
- ✅ **Memory efficient** storage (< 1KB per vector)
- ✅ **Consistent performance** across similarity algorithms
- ✅ **High query throughput** for read-heavy workloads

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

Tests include:
- Unit tests for all domain services
- Integration tests for complete workflows
- Repository tests for persistence
- Text similarity demonstration tests

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