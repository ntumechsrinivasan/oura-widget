# Oura Widget

A minimal Android app for the Pixel 10 Pro XL that shows today's steps and
total calories from Oura:

- **Home screen widget** — tap it to force-refresh.
- **Background check every ~3 hours** (via WorkManager) that updates the
  widget and pushes a notification with the latest numbers.

## What's already set up

Copy `gradle.properties.example` to `gradle.properties` and paste your Oura
personal access token into the `OURA_ACCESS_TOKEN=` line. It's wired into the
build via `BuildConfig.OURA_ACCESS_TOKEN` — nothing to paste into code. Get a
token at `cloud.ouraring.com/personal-access-tokens`.

**`gradle.properties` is gitignored on purpose** — never commit it, since it
will hold your real token once you fill it in.

## How to build and install

1. Install [Android Studio](https://developer.android.com/studio) if you
   don't have it (free, works on Mac/Windows/Linux).
2. Open Android Studio → **Open** → select this `OuraWidget` folder.
3. Let it sync (first sync downloads Gradle + dependencies, takes a few
   minutes).
4. Connect your Pixel 10 Pro XL via USB, with **USB debugging** enabled
   (Settings → About phone → tap "Build number" 7 times → Developer options
   → USB debugging).
5. Select your phone in the device dropdown at the top, then click the green
   **Run ▶** button.
6. On first launch, allow the notification permission prompt.
7. Long-press your home screen → **Widgets** → find **Oura Widget** → drag
   it onto your home screen.

## Notes / limitations

- Android doesn't allow true lock-screen "at a glance" widgets the way
  iOS does — this widget lives on the home screen. Tapping it refreshes
  instantly; the background job also refreshes it automatically every few
  hours.
- The Oura API only reflects data your ring has actually synced to your
  phone/cloud — if the ring hasn't synced recently, numbers may lag behind
  reality until it does.
- `updatePeriodMillis` in the widget config is a backup refresh signal;
  the real schedule is driven by the WorkManager job in `MainActivity`
  (every 3 hours), which is more reliable on modern Android.
