# Contributing to AgroHub

First off, thank you for considering contributing to AgroHub! It's people like you that make AgroHub such a great tool for farmers worldwide.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Community](#community)

## Code of Conduct

This project and everyone participating in it is governed by our commitment to creating a welcoming and inclusive environment. By participating, you are expected to uphold this code.

### Our Standards

- **Be respectful**: Treat everyone with respect and kindness
- **Be collaborative**: Work together towards common goals
- **Be inclusive**: Welcome diverse perspectives and experiences
- **Be constructive**: Provide helpful feedback and suggestions
- **Be patient**: Remember that everyone is learning

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples** (screenshots, code snippets)
- **Describe the behavior you observed** and what you expected
- **Include device/environment details** (Android version, device model)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description** of the suggested enhancement
- **Explain why this enhancement would be useful** to farmers
- **Include mockups or examples** if applicable

### Your First Code Contribution

Unsure where to begin? Look for issues labeled:

- `good-first-issue` - Simple issues perfect for beginners
- `help-wanted` - Issues where we need community help
- `documentation` - Improvements to documentation
- `bug` - Confirmed bugs that need fixing

### Areas We Need Help

1. **Localization**: Translating the app to regional languages (Hindi, Tamil, Telugu, Bengali, etc.)
2. **UI/UX**: Improving accessibility and user experience for rural users
3. **Testing**: Writing unit tests and integration tests
4. **Documentation**: Improving guides and API documentation
5. **Features**: Implementing new features from the roadmap
6. **Bug Fixes**: Fixing reported issues
7. **Performance**: Optimizing app performance for low-end devices

## Getting Started

### Prerequisites

- **Android Studio**: Latest stable version (Hedgehog or newer)
- **JDK**: Version 17 or higher
- **Git**: For version control
- **Kotlin**: Knowledge of Kotlin programming
- **Jetpack Compose**: Familiarity with modern Android UI

### Development Setup

1. **Fork the repository**
   ```bash
   # Click the "Fork" button on GitHub
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/AgroHub.git
   cd AgroHub
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/AgroHub.git
   ```

4. **Create local.properties**
   ```properties
   sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
   backend.base.url=http://10.0.2.2:8080/api/
   GEMINI_API_KEY=your_gemini_api_key_here
   GOOGLE_MAPS_API_KEY=your_maps_api_key_here
   ```

5. **Install dependencies**
   ```bash
   # Open project in Android Studio
   # Gradle will automatically sync dependencies
   ```

6. **Run the app**
   ```bash
   # Use Android Studio's Run button or
   ./gradlew installDebug
   ```

### Backend Setup (Optional)

If you're working on backend features:

```bash
cd Backend
docker-compose up -d
```

See `Backend/README.md` for detailed backend setup instructions.

## Pull Request Process

### Before Submitting

1. **Create a new branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```

2. **Make your changes**
   - Write clean, readable code
   - Follow our coding standards
   - Add comments for complex logic
   - Update documentation if needed

3. **Test your changes**
   - Run existing tests: `./gradlew test`
   - Add new tests for your changes
   - Test on multiple devices/emulators
   - Verify no regressions

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add crop rotation tracking feature"
   ```

   Use conventional commit messages:
   - `feat:` New feature
   - `fix:` Bug fix
   - `docs:` Documentation changes
   - `style:` Code style changes (formatting)
   - `refactor:` Code refactoring
   - `test:` Adding or updating tests
   - `chore:` Maintenance tasks

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

### Submitting the Pull Request

1. **Go to the original repository** on GitHub
2. **Click "New Pull Request"**
3. **Select your fork and branch**
4. **Fill out the PR template**:
   - Clear title describing the change
   - Detailed description of what and why
   - Link to related issues
   - Screenshots/videos if UI changes
   - Testing steps

5. **Wait for review**
   - Address reviewer feedback
   - Make requested changes
   - Keep the conversation constructive

### PR Review Process

- **Automated checks** must pass (build, tests, linting)
- **At least one approval** from maintainers required
- **All conversations resolved** before merging
- **Squash and merge** for clean history

## Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// Good
class FarmViewModel(
    private val repository: FarmRepository
) : ViewModel() {
    
    fun loadFarms() {
        viewModelScope.launch {
            // Implementation
        }
    }
}

// Use meaningful names
val cropYieldPerHectare = calculateYield()

// Add KDoc for public APIs
/**
 * Calculates the optimal irrigation schedule.
 *
 * @param cropType Type of crop being grown
 * @param soilMoisture Current soil moisture level
 * @return Recommended irrigation schedule
 */
fun calculateIrrigationSchedule(
    cropType: CropType,
    soilMoisture: Float
): IrrigationSchedule
```

### Jetpack Compose Guidelines

```kotlin
// Composable naming: PascalCase
@Composable
fun FarmCard(
    farm: Farm,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Modifier parameter last with default
    Card(modifier = modifier) {
        // Implementation
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun FarmCardPreview() {
    AgroHubTheme {
        FarmCard(
            farm = Farm.sample(),
            onClick = {}
        )
    }
}
```

### Architecture Patterns

- **MVVM**: Use ViewModel for business logic
- **Repository Pattern**: Separate data sources
- **Clean Architecture**: Domain, Data, Presentation layers
- **Dependency Injection**: Use constructor injection
- **Coroutines**: For asynchronous operations
- **StateFlow**: For reactive state management

### File Organization

```
app/src/main/java/com/example/agrohub/
├── data/              # Data layer
│   ├── remote/        # API services
│   ├── repository/    # Repository implementations
│   └── mapper/        # DTO to domain mappers
├── domain/            # Domain layer
│   ├── model/         # Domain models
│   ├── repository/    # Repository interfaces
│   └── util/          # Domain utilities
├── presentation/      # ViewModels
├── ui/                # UI layer
│   ├── screens/       # Screen composables
│   ├── components/    # Reusable components
│   ├── theme/         # Theme and styling
│   └── navigation/    # Navigation setup
└── services/          # External services (Gemini, etc.)
```

### Testing

Write tests for:
- **ViewModels**: Business logic and state management
- **Repositories**: Data operations
- **Composables**: UI behavior (where applicable)

```kotlin
class FarmViewModelTest {
    @Test
    fun `loadFarms updates state to Success`() = runTest {
        // Given
        val farms = listOf(Farm.sample())
        coEvery { repository.getFarms() } returns Result.Success(farms)
        
        // When
        viewModel.loadFarms()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.farmsState.value
        assertTrue(state is UiState.Success)
        assertEquals(farms, (state as UiState.Success).data)
    }
}
```

## Documentation

### Code Documentation

- Add KDoc comments for public APIs
- Explain complex algorithms
- Document assumptions and constraints
- Include usage examples

### User Documentation

- Update README.md for new features
- Add setup instructions for new dependencies
- Create guides for complex features
- Include screenshots and videos

## Community

### Communication Channels

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Pull Requests**: Code reviews and discussions

### Getting Help

- Check existing documentation first
- Search closed issues for similar problems
- Ask in GitHub Discussions
- Be specific and provide context

### Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- Credited in the app's About section

## License

By contributing to AgroHub, you agree that your contributions will be licensed under the MIT License.

## Questions?

Don't hesitate to ask! We're here to help:
- Open a GitHub Discussion
- Comment on relevant issues
- Reach out to maintainers

---

**Thank you for helping make agriculture more accessible and sustainable for farmers worldwide!** 🌾

Every contribution, no matter how small, makes a difference in the lives of farmers who depend on AgroHub.
