# Workflow

A Kotlin library for building advanced workflow patterns with a fluent DSL and coroutines support.

## Overview

Workflow provides a powerful DSL for creating complex workflow patterns with built-in error handling, context management, and parallel execution capabilities. It's designed to make workflow creation intuitive and type-safe.

## Requirements

- Java 17 or higher
- Gradle (will be downloaded automatically via wrapper)
- Git (for version management)

## Features

- **DSL Workflows**: Fluent API with custom operators (`➡️`, `⚑`, `🔚`, `♦️`, `♻️`)
- **Context Management**: Shared state across workflow steps using `WorkflowContext`
- **Error Handling**: Built-in failure recovery with `♻️` operator
- **Type Safety**: Generic workflow with type-safe operations
- **Coroutines Support**: Async workflow execution with `runTest`
- **Method Chaining**: Fluent API for building complex workflows
- **Parallel Execution**: Run steps concurrently (planned feature)

## Building the Project

To build the project, run:

```bash
./gradlew build
```

## Running Tests

To run the tests, use:

```bash
./gradlew test
```

## Usage

### Basic Workflow

```kotlin
val result = Workflow.start("initial") {
    initial `➡️` `⚑`("step1", "step2") `➡️` { context, input ->
        "processed: $input"
    }
}
// Result: "processed: step2"
```

### Multi-Step Workflow

```kotlin
val result = Workflow.start("start") {
    initial `➡️` `⚑`("step1") `➡️` { context, input ->
        "step1: $input"
    } `➡️` `⚑`("step2") `➡️` { context, input ->
        "step2: $input"
    }
}
// Result: "step2: step2"
```

### Context Management

```kotlin
val result = Workflow.start("initial") {
    initial `➡️` `⚑`("value1", "value2", "value3") `➡️` { context, input ->
        context.store("customValue")
        "stored: $input"
    }
}
// Result: "stored: value3"
```

### Error Recovery

```kotlin
val result = Workflow.start("initial") {
    initial `➡️` `⚑`("test") `➡️` { context, input ->
        throw RuntimeException("test error")
    } `♻️` { context, error ->
        Result.success("recovered: ${error.exceptionOrNull()?.message}")
    }
}
// Result: "recovered: null"
```

### Decision Making

```kotlin
val result = Workflow.start("initial") {
    initial `➡️` `⚑`("test") `♦️` { context, input ->
        Result.success("decided: ${input.get()}")
    }
}
// Result: "decided: test"
```

### End Conditions

```kotlin
val result = Workflow.start("initial") {
    initial `➡️` `⚑`("test", "value") `➡️` { context, input ->
        "processed: $input"
    } `🔚` String::class
}
// Result: "processed: value"
```

## DSL Operators

- **`➡️`**: Execute a step or transformation
- **`⚑`**: Define a step with values (takes varargs)
- **`🔚`**: End workflow with specific type
- **`♦️`**: Decision point with custom logic
- **`♻️`**: Recovery with Result type

## Core Components

### Workflow<T, V>
The main workflow class that manages the execution flow.

### WorkflowContext
Manages shared state across workflow steps:
- `store(value)`: Store a value in context
- `get(klass)`: Retrieve a value by type
- `has(klass)`: Check if value exists
- `clone()`: Create a copy of context

### Step<T>
Represents a single workflow step:
- `from(context)`: Execute the step with given context

## GitHub Workflows

This project includes GitHub Actions workflows for automated CI/CD:

### Build and Test (`build.yml`)
- **Trigger**: Push to `main` and pull requests
- **Purpose**: Continuous Integration
- **Actions**: Builds the project and runs tests

### Deploy (`deploy.yml`)
- **Trigger**: Only runs on tag pushes (`v*`)
- **Purpose**: Dedicated deployment workflow for releases
- **Actions**:
  - Builds and tests the project
  - Publishes to Maven Central
  - Creates GitHub Release with JReleaser

### Required Secrets
Set up these secrets in your GitHub repository settings:

- `MAVEN_CENTRAL_USERNAME`: Your Sonatype OSSRH username
- `MAVEN_CENTRAL_PASSWORD`: Your Sonatype OSSRH password
- `SIGNING_KEY`: Your PGP private key
- `SIGNING_PASSWORD`: Your PGP key password

## Publishing to Maven Central

This project is configured to publish to Maven Central using JReleaser.

### Prerequisites

1. **Sonatype OSSRH Account**: Sign up at https://oss.sonatype.org/
2. **PGP Key**: Create a PGP key for signing releases
3. **GitHub Repository**: Ensure your repository is public
4. **Git Tags**: Use semantic versioning tags (e.g., `v1.0.0`)

### Publishing

To create a release:
```bash
./gradlew jreleaser:fullRelease
```

To dry-run a release:
```bash
./gradlew jreleaser:dryRun
```

## Project Structure

```
src/
├── main/kotlin/io/github/ludorival/workflow/
│   ├── Workflow.kt         # Main workflow implementation with DSL
│   ├── WorkflowContext.kt  # Context management
│   └── Step.kt            # Step and related types
└── test/kotlin/io/github/ludorival/workflow/
    └── WorkflowTest.kt    # Comprehensive test suite
```

## Dependencies

- **Kotlinx Coroutines**: For async workflow execution
- **JUnit 5**: For testing
- **Kotlin Standard Library**: Core Kotlin functionality

## Gradle Tasks

Common Gradle tasks:

- `./gradlew build` - Build the project
- `./gradlew test` - Run tests
- `./gradlew clean` - Clean build artifacts
- `./gradlew jar` - Create a JAR file
- `./gradlew tasks` - List all available tasks
- `./gradlew jreleaser:fullRelease` - Create a full release with JReleaser
- `./gradlew jreleaser:dryRun` - Dry run a release

## License

This project is licensed under the MIT License. 