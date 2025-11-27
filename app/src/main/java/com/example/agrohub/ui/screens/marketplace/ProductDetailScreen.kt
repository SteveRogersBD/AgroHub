package com.example.agrohub.ui.screens.marketplace

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrohub.data.MockDataProvider
import com.example.agrohub.models.Product
import com.example.agrohub.ui.components.buttons.PrimaryButton
import com.example.agrohub.ui.components.buttons.SecondaryButton
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing

/**
 * Product Detail Screen - Detailed view of a marketplace product
 * 
 * Features:
 * - Large product images with rounded corners
 * - Detailed product description
 * - Seller profile with avatar and name
 * - Contact buttons (call, message)
 * - Back navigation
 * 
 * Requirements: 7.4
 * 
 * @param navController Navigation controller for back navigation
 * @param productId ID of the product to display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String
) {
    // Get product data
    val product = remember(productId) {
        MockDataProvider.getProductById(productId)
    }
    
    // Fade-in animation
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "fadeIn"
    )
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    if (product == null) {
        // Product not found - show error state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AgroHubColors.BackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = AgroHubIcons.CriticalStatus.icon,
                    contentDescription = "Product not found",
                    tint = AgroHubColors.TextSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
                Text(
                    text = "Product not found",
                    style = MaterialTheme.typography.titleLarge,
                    color = AgroHubColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(AgroHubSpacing.md))
                PrimaryButton(
                    text = "Go Back",
                    onClick = { navController.popBackStack() }
                )
            }
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = AgroHubIcons.Back,
                            contentDescription = "Back",
                            tint = AgroHubColors.DeepGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroHubColors.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AgroHubColors.BackgroundLight)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .graphicsLayer { this.alpha = alpha }
        ) {
            // Large Product Image
            ProductImageSection(product = product)
            
            // Product Details Card
            ProductDetailsCard(product = product)
            
            // Seller Profile Card
            SellerProfileCard(product = product)
            
            // Contact Buttons
            ContactButtonsSection(product = product)
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(AgroHubSpacing.lg))
        }
    }
}

/**
 * Product Image Section - Large product image with rounded corners
 * Requirements: 7.4
 */
@Composable
fun ProductImageSection(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(AgroHubSpacing.md),
        shape = AgroHubShapes.large,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Image(
            painter = painterResource(id = product.imageUrl),
            contentDescription = product.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Product Details Card - Title, price, location, category, description
 * Requirements: 7.4
 */
@Composable
fun ProductDetailsCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AgroHubSpacing.md)
            .padding(bottom = AgroHubSpacing.md),
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md)
        ) {
            // Title
            Text(
                text = product.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Price
            Text(
                text = product.price,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.DeepGreen
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = AgroHubIcons.Location,
                    contentDescription = null,
                    tint = AgroHubColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
                Text(
                    text = product.location,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AgroHubColors.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Category
            Surface(
                shape = AgroHubShapes.small,
                color = AgroHubColors.LightGreen
            ) {
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AgroHubColors.DeepGreen,
                    modifier = Modifier.padding(
                        horizontal = AgroHubSpacing.sm,
                        vertical = AgroHubSpacing.xs
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Divider
            HorizontalDivider(color = AgroHubColors.BackgroundLight)
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Description Header
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.sm))
            
            // Description
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyLarge,
                color = AgroHubColors.TextSecondary,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight.times(1.5f)
            )
        }
    }
}

/**
 * Seller Profile Card - Avatar, name, and seller information
 * Requirements: 7.4
 */
@Composable
fun SellerProfileCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AgroHubSpacing.md)
            .padding(bottom = AgroHubSpacing.md),
        shape = AgroHubShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = AgroHubColors.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AgroHubSpacing.md)
        ) {
            Text(
                text = "Seller Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgroHubColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seller Avatar
                Image(
                    painter = painterResource(id = product.sellerAvatar),
                    contentDescription = product.sellerName,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(AgroHubSpacing.md))
                
                // Seller Name
                Column {
                    Text(
                        text = product.sellerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AgroHubColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(AgroHubSpacing.xs))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = AgroHubIcons.Phone,
                            contentDescription = null,
                            tint = AgroHubColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(AgroHubSpacing.xs))
                        Text(
                            text = product.sellerPhone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgroHubColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Contact Buttons Section - Call and Message buttons
 * Requirements: 7.4
 */
@Composable
fun ContactButtonsSection(product: Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AgroHubSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm)
    ) {
        // Call Button
        PrimaryButton(
            text = "Call Seller",
            onClick = { /* Handle call action */ },
            icon = AgroHubIcons.Phone,
            modifier = Modifier.weight(1f)
        )
        
        // Message Button
        SecondaryButton(
            text = "Message",
            onClick = { /* Handle message action */ },
            modifier = Modifier.weight(1f)
        )
    }
}
