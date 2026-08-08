# Oura Widget

An Android home-screen widget that shows today's **steps** and **active calories** from
[Oura](https://ouraring.com), built with Jetpack Glance.

## Features

- Clean, card-style widget (day/night aware) instead of a bare RemoteViews layout
- Refreshes in the background every 15 minutes (Android's floor for periodic work)
- Tap the refresh icon on the widget for an immediate update
- Last-known values are cached and shown instantly on relaunch/reboot — no blank widget
  while the network call is in flight
- Personal access token is stored in `EncryptedSharedPreferences`, on-device only

## Setup

1. Get a personal access token from
   [cloud.ouraring.com/personal-access-tokens](https://cloud.ouraring.com/personal-access-tokens).
2. Install the APK (see below), open the app, paste the token, tap **Save & refresh**.
3. Long-press the home screen → Widgets → **Oura Widget** → drag it onto the home screen.

## Getting the APK

Every push builds a debug APK in GitHub Actions:

- Go to the **Actions** tab → the latest `Build APK` run → download the
  `oura-widget-debug` artifact.
- Tagging a commit `vX.Y.Z` also attaches the APK to a GitHub **Release**, which is a
  more permanent download link than a workflow artifact.

## Building locally

Requires an Android SDK (compileSdk 34) on your machine.

```
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`.

## Project layout

- `data/` — Oura API client (Retrofit + kotlinx.serialization), encrypted token store,
  local cache of the last snapshot
- `work/` — WorkManager scheduling for periodic + on-demand refresh
- `ui/ConfigActivity.kt` — token entry screen, doubles as the widget's configuration
  activity and the app's launcher screen
- `OuraWidget.kt` — the Glance widget UI
- `OuraWidgetReceiver.kt` — the `AppWidgetProvider` entry point
