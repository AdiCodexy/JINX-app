# JINX — Android App

## Setup (one-time)

1. Install [Android Studio](https://developer.android.com/studio) on your computer
2. Open this folder in Android Studio
3. It will download everything needed automatically
4. Or just push to GitHub — Actions will build the APK for you (see below)

## Build via GitHub Actions (easiest — no setup needed)

1. Create a new GitHub repo (e.g. `bestypt-app`)
2. Push this entire folder to it
3. Go to **Actions** tab on GitHub → the build starts automatically
4. Wait ~3-4 minutes → click the build → download **BestYpt-Debug-APK**
5. Transfer the APK to your phone and install it

> **Enable "Install from unknown sources"** on your phone before installing.

## Change the URL

Edit `app/src/main/java/com/jinx/bestypt/MainActivity.java`
Find line: `private static final String APP_URL = "https://aditya7karale.github.io/bestypt/";`
Change it to your actual GitHub Pages URL.

## What it does

- Opens your app in a full-screen WebView (no browser chrome)
- Shows a purple loading bar at the top
- If the page fails to load: shows **"Error — JINX messed up"** with a Retry button
- Back button navigates back inside the app
- External links open in the phone's browser
