# Loading and Empty State Components - Usage Examples

This document provides examples of how to use the loading indicators and empty state components throughout the AgroHub application.

## Loading Indicators

### 1. Circular Progress Indicator

Use for general loading states, centered on screen or in a section:

```kotlin
@Composable
fun MyScreen() {
    var isLoading by remember { mutableStateOf(true) }
    
    if (isLoading) {
        AgroHubCircularProgress()
    } else {
        // Your content
    }
}
```

### 2. Linear Progress Indicator

Use for progress bars at the top of screens or sections:

```kotlin
@Composable
fun MyScreen() {
    var isLoading by remember { mutableStateOf(true) }
    
    Column {
        if (isLoading) {
            AgroHubLinearProgress()
        }
        // Your content
    }
}

// With progress value (0.0 to 1.0)
@Composable
fun UploadScreen() {
    var uploadProgress by remember { mutableStateOf(0.5f) }
    
    Column {
        AgroHubLinearProgress(progress = uploadProgress)
        // Your content
    }
}
```

### 3. Full Screen Loading

Use when the entire screen is loading:

```kotlin
@Composable
fun MyScreen() {
    var isLoading by remember { mutableStateOf(true) }
    
    if (isLoading) {
        FullScreenLoading()
    } else {
        // Your content
    }
}
```

### 4. Shimmer Effects

Use shimmer placeholders while content is loading:

#### Shimmer Box (for rectangular content)
```kotlin
@Composable
fun LoadingCard() {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            ShimmerBox(height = 24.dp, cornerRadius = 4.dp)
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(height = 16.dp, cornerRadius = 4.dp)
        }
    }
}
```

#### Shimmer Circle (for avatars)
```kotlin
@Composable
fun LoadingProfile() {
    Row {
        ShimmerCircle(size = 48.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            ShimmerBox(height = 20.dp, cornerRadius = 4.dp)
            ShimmerBox(height = 16.dp, cornerRadius = 4.dp)
        }
    }
}
```

#### Shimmer Card (pre-built card placeholder)
```kotlin
@Composable
fun LoadingFeed() {
    LazyColumn {
        items(5) {
            ShimmerCard()
        }
    }
}
```

#### Shimmer List Item (pre-built list item placeholder)
```kotlin
@Composable
fun LoadingList() {
    Column {
        repeat(5) {
            ShimmerListItem()
        }
    }
}
```

#### Loading List (multiple shimmer items)
```kotlin
@Composable
fun MyListScreen() {
    var isLoading by remember { mutableStateOf(true) }
    
    if (isLoading) {
        LoadingList(itemCount = 5)
    } else {
        // Your actual list
    }
}
```

## Empty States

### 1. Generic Empty State

Use for custom empty states:

```kotlin
@Composable
fun MyScreen() {
    val items = remember { emptyList<Item>() }
    
    if (items.isEmpty()) {
        EmptyState(
            icon = AgroHubIcons.Field,
            title = "No Items",
            message = "You don't have any items yet. Add your first item to get started.",
            actionText = "Add Item",
            onActionClick = { /* Handle add */ }
        )
    } else {
        // Show items
    }
}
```

### 2. Empty Farms State

Use on the home screen when user has no farms:

```kotlin
@Composable
fun HomeScreen(navController: NavController) {
    val farms = remember { MockDataProvider.generateFarms() }
    
    if (farms.isEmpty()) {
        EmptyFarmsState(
            onAddFarm = { navController.navigate(Routes.ADD_FARM) }
        )
    } else {
        // Show farms
    }
}
```

### 3. Empty Crops State

Use when a farm has no crops:

```kotlin
@Composable
fun FarmDetailScreen() {
    val crops = remember { emptyList<Crop>() }
    
    if (crops.isEmpty()) {
        EmptyCropsState(
            onAddCrop = { /* Handle add crop */ }
        )
    } else {
        // Show crops
    }
}
```

### 4. Empty Community Posts State

Use on the community screen when there are no posts:

```kotlin
@Composable
fun CommunityScreen(navController: NavController) {
    val posts = remember { MockDataProvider.generateCommunityPosts() }
    
    if (posts.isEmpty()) {
        EmptyPostsState(
            onCreatePost = { /* Handle create post */ }
        )
    } else {
        // Show posts
    }
}
```

### 5. Empty Marketplace Products State

Use on the marketplace screen when no products are available:

```kotlin
@Composable
fun MarketplaceScreen() {
    val products = remember { MockDataProvider.generateProducts() }
    
    if (products.isEmpty()) {
        EmptyProductsState(
            onAddProduct = { /* Handle add product */ }
        )
    } else {
        // Show products
    }
}
```

### 6. Empty Chat State

Use on the chat screen when there are no messages:

```kotlin
@Composable
fun ChatScreen() {
    val messages = remember { emptyList<ChatMessage>() }
    
    if (messages.isEmpty()) {
        EmptyChatState()
    } else {
        // Show messages
    }
}
```

### 7. Empty Weather Alerts State

Use on the weather screen when there are no alerts:

```kotlin
@Composable
fun WeatherScreen() {
    val alerts = remember { emptyList<WeatherAlert>() }
    
    if (alerts.isEmpty()) {
        EmptyWeatherAlertsState()
    } else {
        // Show alerts
    }
}
```

### 8. Empty Notes State

Use on the profile screen when user has no saved notes:

```kotlin
@Composable
fun ProfileScreen() {
    val notes = remember { emptyList<Note>() }
    
    if (notes.isEmpty()) {
        EmptyNotesState(
            onCreateNote = { /* Handle create note */ }
        )
    } else {
        // Show notes
    }
}
```

### 9. Empty Activity State

Use when there's no activity history:

```kotlin
@Composable
fun ActivityHistorySection() {
    val activities = remember { emptyList<Activity>() }
    
    if (activities.isEmpty()) {
        EmptyActivityState()
    } else {
        // Show activities
    }
}
```

### 10. Empty Search Results State

Use when search returns no results:

```kotlin
@Composable
fun SearchScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val results = remember { emptyList<SearchResult>() }
    
    if (results.isEmpty() && searchQuery.isNotEmpty()) {
        EmptySearchResultsState(searchQuery = searchQuery)
    } else {
        // Show results
    }
}
```

### 11. Empty Tasks State

Use when there are no pending tasks:

```kotlin
@Composable
fun TasksScreen() {
    val tasks = remember { emptyList<Task>() }
    
    if (tasks.isEmpty()) {
        EmptyTasksState()
    } else {
        // Show tasks
    }
}
```

### 12. No Connection State

Use when the app is offline:

```kotlin
@Composable
fun MyScreen() {
    var isOnline by remember { mutableStateOf(false) }
    
    if (!isOnline) {
        NoConnectionState(
            onRetry = { /* Handle retry */ }
        )
    } else {
        // Show content
    }
}
```

### 13. Generic Error State

Use when an error occurs:

```kotlin
@Composable
fun MyScreen() {
    var hasError by remember { mutableStateOf(false) }
    
    if (hasError) {
        ErrorState(
            title = "Failed to Load Data",
            message = "We couldn't load your data. Please try again.",
            onRetry = { /* Handle retry */ }
        )
    } else {
        // Show content
    }
}
```

## Combined Loading and Empty States

### Complete Screen Example

Here's a complete example showing how to handle loading, empty, error, and content states:

```kotlin
@Composable
fun CompleteScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    val items = remember { mutableStateListOf<Item>() }
    
    LaunchedEffect(Unit) {
        try {
            // Simulate loading
            delay(2000)
            // Load items
            items.addAll(loadItems())
            isLoading = false
        } catch (e: Exception) {
            hasError = true
            isLoading = false
        }
    }
    
    when {
        isLoading -> {
            // Show loading state
            LoadingList(itemCount = 5)
        }
        hasError -> {
            // Show error state
            ErrorState(
                onRetry = {
                    isLoading = true
                    hasError = false
                    // Retry loading
                }
            )
        }
        items.isEmpty() -> {
            // Show empty state
            EmptyState(
                icon = AgroHubIcons.Field,
                title = "No Items",
                message = "You don't have any items yet.",
                actionText = "Add Item",
                onActionClick = { /* Handle add */ }
            )
        }
        else -> {
            // Show content
            LazyColumn {
                items(items) { item ->
                    ItemCard(item = item)
                }
            }
        }
    }
}
```

### Partial Loading (Content with Loading Indicator)

Show content with a loading indicator at the top:

```kotlin
@Composable
fun RefreshableScreen() {
    var isRefreshing by remember { mutableStateOf(false) }
    val items = remember { mutableStateListOf<Item>() }
    
    Column {
        if (isRefreshing) {
            AgroHubLinearProgress()
        }
        
        LazyColumn {
            items(items) { item ->
                ItemCard(item = item)
            }
        }
    }
}
```

### Pagination Loading

Show loading indicator at the bottom while loading more items:

```kotlin
@Composable
fun PaginatedList() {
    val items = remember { mutableStateListOf<Item>() }
    var isLoadingMore by remember { mutableStateOf(false) }
    
    LazyColumn {
        items(items) { item ->
            ItemCard(item = item)
        }
        
        if (isLoadingMore) {
            item {
                AgroHubCircularProgress()
            }
        }
    }
}
```

## Best Practices

1. **Always provide context**: Use specific empty states that match the screen context
2. **Include actions**: When possible, provide an action button to help users get started
3. **Use shimmer for content**: Use shimmer placeholders that match your actual content layout
4. **Handle all states**: Always handle loading, empty, error, and content states
5. **Consistent timing**: Use consistent animation durations (200-400ms)
6. **Accessibility**: All icons have content descriptions for screen readers
7. **Color consistency**: All components use the AgroHub color palette
8. **Spacing consistency**: All components use the AgroHub spacing scale

## Requirements Validation

These components satisfy the following requirements:

- **Requirement 11.5**: Loading state indicators with shimmer effects and progress indicators
- **Requirement 12.4**: Empty state presentation with contextual illustrations and messages
