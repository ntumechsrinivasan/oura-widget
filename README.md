# Oura Widget

A minimal Android app for the Pixel 10 Pro XL that shows today's steps and
total calories from Oura:

- **Home screen widget** — tap it to force-refresh.
- **Background check every ~3 hours** (via WorkManager) that updates the
  widget and pushes a notification with the latest numbers.

## How to build and install (phone-only, no computer needed)

The `.github/workflows/build-apk.yml` workflow builds the APK on GitHub's
servers, so you never need Android Studio. Do all of this from the GitHub
app or a mobile browser:

1. **Add your Oura token as a repo secret** (one-time): in this repo go to
   **Settings → Secrets and variables → Actions → New repository secret**.
   Name it `OURA_ACCESS_TOKEN` and paste your token from
   `cloud.ouraring.com/personal-access-tokens` as the value. This keeps the
   token out of the codebase — it's injected at build time only.
2. **Trigger a build**: any push to this repo starts one automatically. To
   run it on demand instead, go to the **Actions** tab → **Build APK** →
   **Run workflow**.
3. **Wait for the green check** (a few minutes), then open the
   **Releases** page of this repo (or `/releases/tag/latest-apk`). It
   always holds the newest build as `app-debug.apk`.
4. **Download `app-debug.apk`** directly in your phone's browser, then tap
   the downloaded file to install. Android will ask to allow installs from
   that app (Chrome/Files) the first time — allow it, then tap **Install**.
5. Open the app once to grant the notification permission, then long-press
   your home screen → **Widgets** → **Oura Widget** → drag it onto your
   home screen.

If you ever regenerate your Oura token, just update the
`OURA_ACCESS_TOKEN` repo secret and re-run the workflow — no code changes
needed.

### Building from a computer instead

If you'd rather use Android Studio: copy `gradle.properties.example` to
`gradle.properties`, paste your token into the `OURA_ACCESS_TOKEN=` line,
open the `OuraWidget` folder in Android Studio, and run it on a connected
device. `gradle.properties` is gitignored on purpose — never commit it.

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
- The APK built by this workflow is signed with Android's default debug
  key (fine for installing on your own device); it's not suitable for
  Play Store distribution.
