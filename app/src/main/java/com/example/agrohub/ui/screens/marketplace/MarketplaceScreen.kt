package com.example.agrohub.ui.screens.marketplace

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.data.MockDataProvider
import com.example.agrohub.models.Product
import com.example.agrohub.ui.components.buttons.AgroHubFAB
import com.example.agrohub.ui.components.buttons.PrimaryButton
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.navigation.Routes
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing

/**
 * Marketplace Screen - E-commerce interface for agricultural products
 * 
 * Features:
 * - Category filtering with horizontal scrolling chips
 * - Product grid with LazyVerticalGrid
 * - Product cards with images, prices, and CTAs
 * - Floating action button for selling
 * - Grid layout animations
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.5, 7.6
 */
@Composable
fun MarketplaceScreen(navController: NavController) {
    val categories = remember { MockDataProvider.getProductCategories() }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val allProducts = remember { MockDataProvider.generateMarketplaceProducts() }
    val filteredProducts = remember(selectedCategory) {
        if (selectedCategory == "All") {
            allProducts
        } else {
            allProducts.filter { it.category == selectedCategory }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroHubColors.BackgroundLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            MarketplaceHeader()
            
            // Category Chips
            CategoryChips(
                categories = listOf("All") + categories,
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Product Grid
            ProductGrid(
                products = filteredProducts,
                onProductClick = { product ->
                    navController.navigate(Routes.productDetail(product.id))
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Sell FAB
        AgroHubFAB(
            icon = AgroHubIcons.Add,
            onClick = { /* Handle create listing */ },
            contentDescription = "Sell Product",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AgroHubSpacing.md)
        )
    }
}

/**
 * Marketplace Header
 * Requirements: 7.1
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceHeader() {
    TopAppBar(
        title = {
            Text(
                text = "Marketplace",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AgroHubColors.White
        ),
        actions = {
            IconButton(onClick = { /* Handle search */ }) {
                Icon(
                    imageVector = AgroHubIcons.Search,
                    contentDescription = "Search Products",
                    tint = AgroHubColors.DeepGreen
                )
            }
            IconButton(onClick = { /* Handle filter */ }) {
                Icon(
                    imageVector = AgroHubIcons.Filter,
                    contentDescription = "Filter Products",
                    tint = AgroHubColors.DeepGreen
                )
            }
        }
    )
}

/**
 * Category Chips - Horizontal scrolling category filter
 * Requirements: 7.1
 */
@Composable
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .background(AgroHubColors.White)
            .padding(vertical = AgroHubSpacing.sm),
        contentPadding = PaddingValues(horizontal = AgroHubSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelect(category) }
            )
        }
    }
}

/**
 * Individual Category Chip
 * Requirements: 7.1
 */
@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        AgroHubColors.DeepGreen
    } else {
        AgroHubColors.LightGreen
    }
    
    val textColor = if (isSelected) {
        AgroHubColors.White
    } else {
        AgroHubColors.DeepGreen
    }
    
    Surface(
        onClick = onClick,
        shape = AgroHubShapes.large,
        color = backgroundColor,
        modifier = Modifier.animateContentSize()
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.padding(
                horizontal = AgroHubSpacing.md,
                vertical = AgroHubSpacing.sm
            )
        )
    }
}

/**
 * Product Grid - LazyVerticalGrid with product cards
 * Requirements: 7.2, 7.5
 */
@Composable
fun ProductGrid(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AgroHubSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
    ) {
        itemsIndexed(products) { index, product ->
            // Staggered animation for grid items
            val animationDelay = (index * 50).coerceAtMost(300)
            
            AnimatedProductCard(
                product = product,
                onClick = { onProductClick(product) },
                animationDelay = animationDelay
            )
        }
    }
}

/**
 * Animated Product Card wrapper with staggered animation
 * Requirements: 7.5
 */
@Composable
fun AnimatedProductCard(
    product: Product,
    onClick: () -> Unit,
    animationDelay: Int
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        ProductCard(
            product = product,
            onClick = onClick
        )
    }
}

/**
 * Product Card - E-commerce card with image, title, price, location, CTA
 * Requirements: 7.2, 7.3
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Product Image
            Image(
                painter = painterResource(id = product.imageUrl),
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(AgroHubShapes.medium),
                contentScale = ContentScale.Crop
            )
            
            // Product Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(AgroHubSpacing.sm)
            ) {
                // Title
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AgroHubColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
                
                // Price
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AgroHubColors.DeepGreen
                )
                
                Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
                
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = AgroHubIcons.Location,
                        contentDescription = null,
                        tint = AgroHubColors.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
                    Text(
                        text = product.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = AgroHubColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // CTA Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AgroHubSpacing.sm),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgroHubColors.DeepGreen
                ),
                shape = AgroHubShapes.small
            ) {
                Text(
                    text = "View Details",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
