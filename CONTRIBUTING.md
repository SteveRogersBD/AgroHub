# Contributing to AgroHub

First off, thank you for considering contributing to AgroHub! It's people like you that make AgroHub such a great tool for farmers worldwide.

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title**
* **Describe the exact steps to reproduce the problem**
* **Provide specific examples to demonstrate the steps**
* **Describe the behavior you observed and what behavior you expected**
* **Include screenshots if possible**
* **Include your Android version and device model**

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title**
* **Provide a detailed description of the suggested enhancement**
* **Explain why this enhancement would be useful**
* **List some examples of how it would be used**

### Pull Requests

* Fill in the required template
* Follow the Kotlin coding style
* Include appropriate test coverage
* Update documentation as needed
* End all files with a newline

## Development Setup

1. Fork and clone the repository
2. Set up your API keys in `local.properties`
3. Open the project in Android Studio
4. Run the tests to ensure everything works
5. Create a new branch for your feature

## Coding Standards

### Kotlin Style Guide

* Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
* Use meaningful variable and function names
* Write comments for complex logic
* Keep functions small and focused

### Compose Best Practices

* Use `remember` for state that survives recomposition
* Hoist state when needed
* Use `LaunchedEffect` for side effects
* Follow Material Design 3 guidelines

### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line

Example:
```
Add crop disease detection feature

- Implement camera capture
- Add ML model integration
- Create result display screen

Fixes #123
```

## Testing

* Write unit tests for business logic
* Write UI tests for user interactions
* Ensure all tests pass before submitting PR
* Aim for high test coverage

Run tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Documentation

* Update README.md if you change functionality
* Add KDoc comments to public APIs
* Update relevant guides in the docs folder

## Questions?

Feel free to open an issue with your question or reach out to the maintainers.

Thank you for contributing! 🌾
