# AnimationUtils Usage Examples

This document provides examples of how to use the `AnimationUtils` object for consistent animations throughout the AgroHub application.

## Overview

`AnimationUtils` provides centralized animation definitions following the design system requirements:
- Animation durations: 200-400ms
- Smooth easing curves
- Consistent animation patterns

## Basic Usage

### 1. Fade-In Animation for Cards

Use `fadeInEnter()` for simple fade-in animations:

```kotlin
@Composable
fun MyCard() {
    AnimatedVisibility(
        visible = true,
        enter = AnimationUtils.fadeInEnter()
    ) {
        Card {
            // Card content
        }
    }
}
```

### 2. Scale Animation for Cards

Use `scaleInEnter()` for scale animations:

```kotlin
@Composable
fun MyCard() {
    AnimatedVisibility(
        visible = true,
        enter = AnimationUtils.scaleInEnter()
    ) {
        Card {
            // Card content
        }
    }
}
```

### 3. Combined Card Animation (Recommended)

Use `cardEnterAnimation()` for the standard card appearance (fade + scale):

```kotlin
@Composable
fun MyCard() {
    AnimatedVisibility(
        visible = true,
        enter = AnimationUtils.cardEnterAnimation(),
        exit = AnimationUtils.cardExitAnimation()
    ) {
        Card {
            // Card content
        }
    }
}
```

### 4. Slide-Up Animation for Messages

Use `slideUpEnter()` for message animations:

```kotlin
@Composable
fun ChatMessage(message: ChatMessage) {
    AnimatedVisibility(
        visible = true,
        enter = AnimationUtils.slideUpEnter()
    ) {
        ChatBubble(message)
    }
}
```

### 5. Staggered List Animations

Use `staggeredListItemEnter()` for list items with delays:

```kotlin
@Composable
fun MyList(items: List<Item>) {
    LazyColumn {
        itemsIndexed(items) { index, item ->
            AnimatedVisibility(
                visible = true,
                enter = AnimationUtils.staggeredListItemEnter(index)
            ) {
                ItemCard(item)
            }
        }
    }
}
```

## Modifier Extensions

### Animated Fade-In

Apply fade-in animation directly to a composable:

```kotlin
@Composable
fun MyComponent() {
    Box(
        modifier = Modifier
            .animatedFadeIn(
                durationMillis = AnimationUtils.DURATION_STANDARD,
                delayMillis = 0
            )
    ) {
        // Content
    }
}
```

### Animated Scale

Apply scale animation directly to a composable:

```kotlin
@Composable
fun MyComponent() {
    Box(
        modifier = Modifier
            .animatedScale(
                durationMillis = AnimationUtils.DURATION_STANDARD,
                delayMillis = 0,
                initialScale = 0.95f
            )
    ) {
        // Content
    }
}
```

### Combined Card Appearance

Apply both fade and scale animations:

```kotlin
@Composable
fun MyCard() {
    Card(
        modifier = Modifier
            .animatedCardAppearance(
                durationMillis = AnimationUtils.DURATION_STANDARD,
                delayMillis = 0
            )
    ) {
        // Card content
    }
}
```

### Staggered List Item

Apply staggered animation to list items:

```kotlin
@Composable
fun MyList(items: List<Item>) {
    LazyColumn {
        itemsIndexed(items) { index, item ->
            ItemCard(
                item = item,
                modifier = Modifier.animatedStaggeredListItem(index)
            )
        }
    }
}
```

## Animation Constants

### Durations

```kotlin
AnimationUtils.DURATION_FAST      // 200ms - Quick feedback
AnimationUtils.DURATION_STANDARD  // 300ms - Most interactions
AnimationUtils.DURATION_SLOW      // 400ms - Emphasis
AnimationUtils.STAGGER_DELAY      // 50ms - Delay between list items
```

### Easing Curves

```kotlin
AnimationUtils.EASING_STANDARD    // FastOutSlowInEasing - Most animations
AnimationUtils.EASING_EMPHASIZED  // LinearOutSlowInEasing - Important transitions
```

## Complete Examples

### Example 1: Animated Stat Card

```kotlin
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.animatedCardAppearance(
            durationMillis = AnimationUtils.DURATION_STANDARD
        ),
        shape = AgroHubShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = gradient)
                .padding(AgroHubSpacing.md)
        ) {
            // Card content
        }
    }
}
```

### Example 2: Animated Community Feed

```kotlin
@Composable
fun CommunityFeed(posts: List<Post>) {
    LazyColumn {
        itemsIndexed(posts) { index, post ->
            AnimatedVisibility(
                visible = true,
                enter = AnimationUtils.staggeredListItemEnter(index)
            ) {
                PostCard(post = post)
            }
        }
    }
}
```

### Example 3: Animated Chat Messages

```kotlin
@Composable
fun ChatMessageList(messages: List<ChatMessage>) {
    LazyColumn {
        items(messages, key = { it.id }) { message ->
            AnimatedVisibility(
                visible = true,
                enter = AnimationUtils.slideUpEnter()
            ) {
                ChatBubble(message = message)
            }
        }
    }
}
```

### Example 4: Screen Fade-In

```kotlin
@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animatedFadeIn(
                durationMillis = AnimationUtils.DURATION_SLOW
            )
    ) {
        // Screen content
    }
}
```

## Custom Animation Specs

If you need custom animation specs, you can use the constants:

```kotlin
val customAnimation = tween<Float>(
    durationMillis = AnimationUtils.DURATION_STANDARD,
    easing = AnimationUtils.EASING_STANDARD
)
```

## Shared Element Transitions

For shared element transitions, use:

```kotlin
val sharedElementSpec = AnimationUtils.sharedElementAnimationSpec<Float>()
```

## Best Practices

1. **Use standard durations**: Stick to `DURATION_FAST`, `DURATION_STANDARD`, or `DURATION_SLOW`
2. **Use standard easing**: Use `EASING_STANDARD` for most animations
3. **Stagger list items**: Use `staggeredListItemEnter()` for list animations
4. **Combine animations**: Use `cardEnterAnimation()` for cards (fade + scale)
5. **Keep it simple**: Don't over-animate - use animations purposefully
6. **Test performance**: Ensure animations run smoothly on target devices

## Requirements Mapping

- **Requirement 11.1**: Fade-in and scale animations for cards
- **Requirement 11.2**: Staggered list animations
- **Requirement 11.4**: Shared element transition helpers
- **Requirement 11.6**: Animation timing consistency (200-400ms)
