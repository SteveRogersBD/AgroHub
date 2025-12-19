# Populate Demo Data for Hackathon
# Creates fake users, posts, follows, likes, and comments

Write-Host "=== Populating Demo Data for Hackathon ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api"

# Demo users data
$demoUsers = @(
    @{
        email = "rajesh.farmer@agrohub.com"
        username = "rajesh_farmer"
        password = "Demo123456!"
        name = "Rajesh Kumar"
    },
    @{
        email = "priya.agro@agrohub.com"
        username = "priya_agro"
        password = "Demo123456!"
        name = "Priya Sharma"
    },
    @{
        email = "amit.crops@agrohub.com"
        username = "amit_crops"
        password = "Demo123456!"
        name = "Amit Patel"
    },
    @{
        email = "sunita.organic@agrohub.com"
        username = "sunita_organic"
        password = "Demo123456!"
        name = "Sunita Reddy"
    },
    @{
        email = "vikram.harvest@agrohub.com"
        username = "vikram_harvest"
        password = "Demo123456!"
        name = "Vikram Singh"
    }
)

# Demo posts content
$demoPosts = @(
    "Just harvested my wheat crop! Yield is looking great this season. Any tips for storage?",
    "Trying organic farming for the first time. Excited to see the results!",
    "Weather forecast shows rain next week. Perfect timing for planting!",
    "My tomatoes are growing beautifully! Thanks to everyone who gave advice last month.",
    "Looking for recommendations on pest control. Seeing some issues with my cotton crop.",
    "Attended a great workshop on sustainable farming today. Learned so much!",
    "First time using drip irrigation. The water savings are incredible!",
    "Market prices for rice are looking good. Time to sell!",
    "Anyone else dealing with this heat wave? My crops need extra care.",
    "Proud to say I am now certified in organic farming!",
    "Just bought new equipment. Farming is getting more efficient every year!",
    "Sharing my experience with crop rotation. It really works!",
    "Beautiful sunrise over my fields this morning. Love being a farmer!",
    "Question: What is the best fertilizer for vegetables? Need suggestions.",
    "Harvest festival preparations are underway! Excited to celebrate with the community!"
)

# Demo comments
$demoComments = @(
    "Great work! Keep it up!",
    "Thanks for sharing this!",
    "I had the same experience. Very helpful!",
    "Congratulations! Well deserved!",
    "This is really useful information.",
    "I would love to learn more about this.",
    "Amazing results! How did you do it?",
    "Thanks for the tips!",
    "Very inspiring!",
    "I am trying this too. Wish me luck!",
    "Excellent advice!",
    "This helped me a lot. Thank you!",
    "Keep sharing your journey!",
    "Impressive work!",
    "I agree completely!"
)

$userTokens = @{}
$userIds = @{}
$postIds = @()

# Step 1: Register and login all demo users
Write-Host "Step 1: Creating demo users..." -ForegroundColor Yellow
Write-Host ""

foreach ($user in $demoUsers) {
    Write-Host "  Creating user: $($user.username)..." -ForegroundColor Gray
    
    # Register
    $registerData = @{
        email = $user.email
        username = $user.username
        password = $user.password
    } | ConvertTo-Json
    
    $registerResponse = curl.exe -s -X POST "$baseUrl/auth/register" `
        -H "Content-Type: application/json" `
        -d $registerData 2>$null
    
    # Login
    $loginData = @{
        email = $user.email
        password = $user.password
    } | ConvertTo-Json
    
    $loginResponse = curl.exe -s -X POST "$baseUrl/auth/login" `
        -H "Content-Type: application/json" `
        -d $loginData
    
    try {
        $loginJson = $loginResponse | ConvertFrom-Json
        $userTokens[$user.username] = $loginJson.accessToken
        $userIds[$user.username] = $loginJson.userId
        Write-Host "    ✓ User created and logged in" -ForegroundColor Green
    } catch {
        Write-Host "    ⚠ User may already exist (continuing...)" -ForegroundColor Yellow
    }
    
    Start-Sleep -Milliseconds 200
}

Write-Host ""
Write-Host "✓ Created $($userTokens.Count) users" -ForegroundColor Green
Write-Host ""

# Step 2: Create posts from different users
Write-Host "Step 2: Creating demo posts..." -ForegroundColor Yellow
Write-Host ""

$postIndex = 0
foreach ($user in $demoUsers) {
    $token = $userTokens[$user.username]
    if (-not $token) { continue }
    
    # Each user creates 3 posts
    for ($i = 0; $i -lt 3; $i++) {
        if ($postIndex -ge $demoPosts.Count) { break }
        
        $postContent = $demoPosts[$postIndex]
        Write-Host "  $($user.username): Creating post..." -ForegroundColor Gray
        
        $postData = @{
            content = $postContent
            mediaUrl = $null
        } | ConvertTo-Json
        
        $createPostResponse = curl.exe -s -X POST "$baseUrl/posts" `
            -H "Authorization: Bearer $token" `
            -H "Content-Type: application/json" `
            -d $postData
        
        try {
            $postJson = $createPostResponse | ConvertFrom-Json
            $postIds += $postJson.id
            Write-Host "    ✓ Post created (ID: $($postJson.id))" -ForegroundColor Green
        } catch {
            Write-Host "    ⚠ Failed to create post" -ForegroundColor Yellow
        }
        
        $postIndex++
        Start-Sleep -Milliseconds 300
    }
}

Write-Host ""
Write-Host "✓ Created $($postIds.Count) posts" -ForegroundColor Green
Write-Host ""

# Step 3: Create follow relationships
Write-Host "Step 3: Creating follow relationships..." -ForegroundColor Yellow
Write-Host ""

$followCount = 0
foreach ($follower in $demoUsers) {
    $followerToken = $userTokens[$follower.username]
    if (-not $followerToken) { continue }
    
    # Each user follows 2-3 other users
    $otherUsers = $demoUsers | Where-Object { $_.username -ne $follower.username } | Get-Random -Count 3
    
    foreach ($followee in $otherUsers) {
        $followeeId = $userIds[$followee.username]
        if (-not $followeeId) { continue }
        
        Write-Host "  $($follower.username) following $($followee.username)..." -ForegroundColor Gray
        
        $followResponse = curl.exe -s -X POST "$baseUrl/follow/$followeeId" `
            -H "Authorization: Bearer $followerToken" 2>$null
        
        $followCount++
        Write-Host "    ✓ Follow created" -ForegroundColor Green
        Start-Sleep -Milliseconds 200
    }
}

Write-Host ""
Write-Host "✓ Created $followCount follow relationships" -ForegroundColor Green
Write-Host ""

# Step 4: Add likes to posts
Write-Host "Step 4: Adding likes to posts..." -ForegroundColor Yellow
Write-Host ""

$likeCount = 0
foreach ($post in $postIds) {
    # Random 2-4 users like each post
    $likingUsers = $demoUsers | Get-Random -Count (Get-Random -Minimum 2 -Maximum 5)
    
    foreach ($user in $likingUsers) {
        $token = $userTokens[$user.username]
        if (-not $token) { continue }
        
        $likeResponse = curl.exe -s -X POST "$baseUrl/likes/$post" `
            -H "Authorization: Bearer $token" 2>$null
        
        $likeCount++
        Start-Sleep -Milliseconds 100
    }
    
    Write-Host "  ✓ Added likes to post $post" -ForegroundColor Green
}

Write-Host ""
Write-Host "✓ Created $likeCount likes" -ForegroundColor Green
Write-Host ""

# Step 5: Add comments to posts
Write-Host "Step 5: Adding comments to posts..." -ForegroundColor Yellow
Write-Host ""

$commentCount = 0
foreach ($post in $postIds) {
    # Random 1-3 users comment on each post
    $commentingUsers = $demoUsers | Get-Random -Count (Get-Random -Minimum 1 -Maximum 4)
    
    foreach ($user in $commentingUsers) {
        $token = $userTokens[$user.username]
        if (-not $token) { continue }
        
        $commentContent = $demoComments | Get-Random
        
        $commentData = @{
            postId = $post
            content = $commentContent
        } | ConvertTo-Json
        
        $commentResponse = curl.exe -s -X POST "$baseUrl/comments" `
            -H "Authorization: Bearer $token" `
            -H "Content-Type: application/json" `
            -d $commentData 2>$null
        
        $commentCount++
        Start-Sleep -Milliseconds 150
    }
    
    Write-Host "  ✓ Added comments to post $post" -ForegroundColor Green
}

Write-Host ""
Write-Host "✓ Created $commentCount comments" -ForegroundColor Green
Write-Host ""

# Summary
Write-Host "=== Demo Data Population Complete! ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Summary:" -ForegroundColor White
Write-Host "  Users created: $($userTokens.Count)" -ForegroundColor Green
Write-Host "  Posts created: $($postIds.Count)" -ForegroundColor Green
Write-Host "  Follow relationships: $followCount" -ForegroundColor Green
Write-Host "  Likes added: $likeCount" -ForegroundColor Green
Write-Host "  Comments added: $commentCount" -ForegroundColor Green
Write-Host ""
Write-Host "Demo Users (all passwords: Demo123456!):" -ForegroundColor Yellow
foreach ($user in $demoUsers) {
    Write-Host "  Email: $($user.email)" -ForegroundColor Cyan
    Write-Host "  Username: $($user.username)" -ForegroundColor Gray
    Write-Host ""
}
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Login to the Android app with any of the demo users" -ForegroundColor White
Write-Host "  2. Navigate to the Community tab" -ForegroundColor White
Write-Host "  3. You should see posts from users you're following!" -ForegroundColor White
Write-Host "  4. Try liking and commenting on posts" -ForegroundColor White
Write-Host "  5. Create your own posts to add to the feed" -ForegroundColor White
Write-Host ""
Write-Host "Your app is now ready for the hackathon demo!" -ForegroundColor Green
Write-Host ""
