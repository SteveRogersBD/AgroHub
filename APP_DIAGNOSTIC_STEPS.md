# Android App Diagnostic Steps

## Step 1: Check if You Can Log In

1. **Open the app**
2. **Try to log in** with any credentials
3. **Tell me what happens**:
   - ✅ Login successful - you see the home screen
   - ❌ Login fails - what error message?

## Step 2: Check Logcat for Errors

In Android Studio:

1. **Open Logcat** (bottom panel)
2. **Filter by "AgroHub" or "Error"**
3. **Navigate to Community tab**
4. **Copy and share the error messages** you see

Look for errors like:
- `Unable to resolve host`
- `Connection refused`
- `401 Unauthorized`
- `500 Internal Server Error`
- `Failed to connect`

## Step 3: Verify Network Configuration

### For Emulator:
Your `local.properties` should have:
```properties
BACKEND_BASE_URL=http://10.0.2.2:8080/api/
```
✅ This is correct in your file

### For Physical Device:
You need your computer's IP address:

1. **Find your IP**:
   ```powershell
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., 192.168.1.100)

2. **Update local.properties**:
   ```properties
   BACKEND_BASE_URL=http://YOUR_IP:8080/api/
   ```

3. **Rebuild the app** after changing

## Step 4: Test Backend Directly

From your computer, test if the backend is accessible:

```powershell
# Test API Gateway
curl.exe http://localhost:8080/actuator/health

# Test Feed endpoint (should return 401)
curl.exe http://localhost:8080/api/feed
```

Expected: `{"status":"UP"}` for first command, `401 Unauthorized` for second

## Step 5: Check Token

The app might not have a valid authentication token. Try:

1. **Log out** of the app completely
2. **Close the app**
3. **Reopen and log in again**
4. **Try accessing Community tab**

## Common Issues and Solutions

### Issue: "Unable to resolve host" or "Connection refused"

**Cause**: App can't reach the backend

**Solutions**:
1. Make sure Docker services are running:
   ```powershell
   cd Backend
   docker-compose ps
   ```
2. Check if using emulator or physical device
3. Verify `BACKEND_BASE_URL` in `local.properties`
4. Rebuild app after changing configuration

### Issue: "401 Unauthorized"

**Cause**: No valid authentication token

**Solutions**:
1. Log out and log back in
2. Check if token expired (1 hour lifetime)
3. Make sure you're logged in before accessing Community

### Issue: "500 Internal Server Error"

**Cause**: Backend service error

**Solutions**:
1. Check backend logs:
   ```powershell
   docker-compose logs feed-service
   docker-compose logs post-service
   ```
2. Restart services:
   ```powershell
   docker-compose restart
   ```

### Issue: Empty feed or "No posts yet"

**Cause**: This is normal!

**Explanation**:
- Feed shows posts from users you follow
- If you haven't followed anyone, feed will be empty
- If followed users haven't posted, feed will be empty

**Solution**: This is expected behavior, not an error

## What Information I Need

To help you fix the issue, please provide:

1. **Exact error message** from the app
2. **When it happens** (login, viewing feed, creating post)
3. **Logcat output** showing the error
4. **Are you using emulator or physical device?**
5. **Can you log in successfully?**

## Quick Test Checklist

Run through this checklist:

- [ ] Docker services are running (`docker-compose ps`)
- [ ] API Gateway is UP (`curl.exe http://localhost:8080/actuator/health`)
- [ ] Using correct URL for your device type (emulator vs physical)
- [ ] Rebuilt app after any configuration changes
- [ ] Can log into the app successfully
- [ ] Tried logging out and back in

## Debug Mode

To see detailed network logs in the app:

1. The app already has `LoggingInterceptor` enabled in debug builds
2. Check Logcat for HTTP request/response logs
3. Look for lines with "OkHttp" or "Retrofit"

## Still Having Issues?

Share with me:
1. Screenshot of the error
2. Logcat output (filtered by "Error" or "Exception")
3. Output of `docker-compose ps`
4. Whether you're using emulator or physical device

I'll help you debug the specific issue!
