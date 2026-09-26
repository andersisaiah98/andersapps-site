# J's Cook Book

Julia's personal recipe and meal book for Android: recipes filed into categories, photos, ingredients, steps, a cook log of every time she's made something, and "give me something to make" shortcuts. Tactile, springy and photo-forward.

Native Kotlin + Jetpack Compose, offline-first (Room), with Supabase sync between two phones.

## Status

| Phase | What | State |
|---|---|---|
| 0 | Foundation: scaffold, theme and tokens (light + dark), bundled fonts, bottom navigation shell, fallback art, hidden design-system screen | Done |
| 1 | Local core: Room, recipe/category CRUD, detail, grid, shared-element tile → detail, photo viewer | Done |
| 2 | Sync: Supabase schema + RLS, auth, household, sync engine, photo uploads, Realtime | **Done, awaiting review** |
| 3 | Cook log + Journal, quick actions + randomizer, cooking mode, search + filters | Not started |
| 4 | Drag to organize, pinch grid density, swipe actions, haptics pass, Baseline Profiles, jank check | Not started |
| 5 | Extras (ask first): import from URL, share target | Not started |

## Requirements

- Android Studio (current stable) with its bundled JDK 21
- Android SDK Platform **37** (Android 17) and a current Build Tools. Android Studio offers to install them on first sync.
- Phones with USB debugging on. Tested targets: Pixel 11 Pro and Pixel 7 Pro (the performance floor), both Android 17.

The build uses the Gradle wrapper (Gradle 9.5.0), AGP 9.3.3 with built-in Kotlin, Kotlin 2.4.20 and the Compose BOM 2026.09.00. Versions live in `gradle/libs.versions.toml`.

## Setup

1. Open the `jscookbook/` folder (not the repo root) in Android Studio and let it sync.
2. Android Studio writes `local.properties` with `sdk.dir` for you. It is gitignored.
3. For sync, set up Supabase (below) and add its keys to `local.properties`. Without them the app builds and runs local-only.

### `local.properties` keys

Fill these in on your machine only. Never commit them, and never paste them into chat.

| Key | Needed from | What it is |
|---|---|---|
| `sdk.dir` | Phase 0 | Written by Android Studio |
| `SUPABASE_URL` | Phase 2 | Your Supabase project URL (Project Settings › API) |
| `SUPABASE_ANON_KEY` | Phase 2 | The project's **anon / public** key. Never the service-role key. |
| `GOOGLE_WEB_CLIENT_ID` | Phase 2, optional | The **Web application** OAuth client ID from Google Cloud (see Google sign-in below). Leave it out and only email sign-in is offered. |

They reach the code as `BuildConfig` fields (`app/build.gradle.kts`). The same names also work as environment variables, which is how CI gets them (optional repository secrets).

### Release signing

Create the release keystore once, on your machine, outside the repo (or anywhere; `*.jks` is gitignored). `keytool` ships with Android Studio's JDK (`<Android Studio>/jbr/bin/keytool`). It asks for the passwords interactively, so they never land in your shell history:

```sh
keytool -genkeypair -v -keystore ~/keys/jscookbook-release.jks -alias jscookbook \
  -keyalg RSA -keysize 4096 -validity 36500 -dname "CN=J's Cook Book"
```

Then create `jscookbook/keystore.properties` (gitignored) with these four properties:

| Property | Value |
|---|---|
| `storeFile` | Path to the `.jks`, e.g. `/Users/you/keys/jscookbook-release.jks` |
| `storePassword` | The keystore password you chose |
| `keyAlias` | `jscookbook` |
| `keyPassword` | The key password you chose |

With that file present, `assembleRelease` signs with your key; without it, release builds fall back to the debug key. Back up the `.jks` and its passwords somewhere safe: an app can only be updated with the key it was first installed with.

Google sign-in needs the key's SHA-1:

```sh
keytool -list -v -keystore ~/keys/jscookbook-release.jks -alias jscookbook | grep SHA1
```

**Debug key.** Debug builds use `app/debug.keystore`, which *is* committed. It's a throwaway debug key with the standard public password (`android`), shared so that APKs built on CI and on your machine are signed alike and install over each other. Its SHA-1 is `4B:42:75:9C:16:87:60:C5:E2:11:9A:7D:EC:51:69:0B:85:EC:AE:05`. It is not the release key and must never become it. (Phase 0–1 CI APKs were signed with a random key, so uninstall that build once before installing a Phase 2 one.)

## Get the APK without building

Every push that touches `jscookbook/` is built by GitHub Actions (`.github/workflows/jscookbook.yml`), which also runs the unit tests.

1. Open the repo on GitHub › **Actions** › **J's Cook Book** › the latest green run.
2. Under **Artifacts**, download **jscookbook-debug-apk**. It's a zip; inside is `app-debug.apk`.
3. Install it: `adb install -r app-debug.apk`, or copy it to the phone and open it (allow "Install unknown apps" for your file manager when asked).

## Build and install

With both phones plugged in and authorized (`adb devices` lists both):

```sh
cd jscookbook

# Debug build, installed on every connected phone
./gradlew installDebug

# Or build once and install per phone
./gradlew assembleDebug
adb devices
adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk
```

A minified release build, signed with your release key when `keystore.properties` exists:

```sh
./gradlew assembleRelease
adb -s <serial> install -r app/build/outputs/apk/release/app-release.apk
```

## Tests

```sh
./gradlew testDebugUnitTest :core:model:test   # all unit tests (also run on CI)
./gradlew connectedDebugAndroidTest            # UI tests, run on each connected phone
```

## Project layout

```
jscookbook/
  app/                      Single activity, navigation shell, screens
    ui/navigation/          Type-safe routes, NavHost, the floating bottom bar with the raised ＋
    ui/home, cookbook, journal, settings, add
    ui/detail, editor, category, photos
    data/, di/              Settings (DataStore) and Hilt wiring
  core/model/               Plain Kotlin: recipe models, ingredient parser
  core/data/                Room database (the source of truth), repositories, on-device photo store
    sync/                   Phone side of sync: dirty rows, last-write-wins apply, cursors
    schemas/                Exported Room schemas (every change gets a migration)
  core/sync/                Supabase client, sign-in, household, sync engine, Realtime, WorkManager
  supabase/migrations/      Postgres schema, RLS, storage and Realtime setup
  core/designsystem/        Everything visual, shared by every screen
    theme/                  BM color tokens, light/dark schemes, type, shapes, elevation, springs
    component/              Buttons, cards, tiles, quick-action cards, segmented control, haptics
    recipeimage/            RecipeImageProvider, ImageSource, fallback-art illustrations
    catalog/                The hidden design-system screen
    res/font/               Fraunces + DM Sans (variable, bundled)
  licenses/                 SIL Open Font License texts for the bundled fonts
```

Phase 4 adds `:baselineprofile` and `:macrobenchmark`.

## Design system

- **Palette:** Benjamin Moore colors from Amerie Creative's "TerraCotta" palette, as tokens named after the paints (`BM.TerraCottaTile`, `BM.CloudWhite`, …). Colors that aren't paints live in `Derived` and each one says what it came from. Material You dynamic color is never used.
- **Type:** Fraunces for display and headlines (optical size and "wonky" axes tuned per size), DM Sans for UI and body. Both are bundled in `res/font`, so there's no font swap on first launch and everything works offline.
- **Motion:** named springs (`Snappy`, `Bouncy`, `Gentle`, `Hero`, `Playful`) and no linear tweens. When the system "Remove animations" setting is on, every spec snaps and decorative motion is skipped (`LocalReducedMotion`).
- **Fallback art:** recipes without a photo get an illustration chosen from the title, category and type, a palette tint from an FNV-1a hash of the title, and the initial in Fraunces. It is deterministic, so both phones always agree. It sits behind `RecipeImageProvider` with `ImageSource = PHOTO | FALLBACK | AI`, ready for AI images later.
- **Hidden design-system screen:** Settings › tap **Version** 7 times. It shows every color with its name, code and hex, live contrast ratios, the type scale, shapes, elevation, buttons, cards, the full fallback-art set, the icons, live spring demos and every haptic, with a light/dark toggle.

## Supabase

Everything the server needs is in `supabase/migrations/20260926000000_init.sql`: the tables (mirroring Room), Row Level Security on every table, the household functions, the private `photos` bucket and its policies, and the Realtime publication. It's idempotent, so running it twice is harmless.

### Dashboard steps

1. **Create a project** at supabase.com (any region near you). Wait for it to finish provisioning.
2. **Run the schema.** SQL Editor › New query › paste the whole migration file › Run. It should finish with "Success. No rows returned".
3. **Check it landed:**
   - Table Editor shows `cookbooks`, `cookbook_members`, `cookbook_invites`, `categories`, `recipes`, `recipe_categories`, `ingredients`, `steps`, `photos`, `cook_logs`, `cook_log_photos`, each marked **RLS enabled**.
   - Storage shows a **private** bucket named `photos`.
   - Database › Publications › `supabase_realtime` lists the ten synced tables.
4. **Email sign-in.** Authentication › Sign In / Providers › Email: enabled (it is by default).
   - Authentication › URL Configuration › Redirect URLs › Add URL: `app.jscookbook://login`
   - Authentication › Email Templates › Magic Link: add a line with the code, e.g. `<p>Or enter this code in the app: {{ .Token }}</p>`. The app accepts either the link or the code.
   - Supabase's built-in email sender is rate-limited and meant for trying things out. If links stop arriving, set up your own SMTP under Authentication › Emails › SMTP Settings.
5. **Google sign-in (optional).** In Google Cloud Console (APIs & Services):
   1. Configure the OAuth consent screen (External; add both of your Google accounts as test users while it's in testing).
   2. Credentials › Create credentials › OAuth client ID › **Web application**. Copy its client ID and client secret.
   3. Credentials › Create credentials › OAuth client ID › **Android**: package `app.jscookbook`, SHA-1 of your **release** key. Make a second Android client with the **debug** SHA-1 above if you'll use debug/CI builds.
   4. In Supabase: Authentication › Sign In / Providers › Google › enable, paste the **Web** client ID and secret, save.
   5. Put the **Web** client ID in `local.properties` as `GOOGLE_WEB_CLIENT_ID`. (The Android clients have no ID to copy anywhere; Google matches them by package + SHA-1.)
6. **Keys.** Project Settings › API (or API Keys): copy the Project URL into `SUPABASE_URL` and the **anon public** key into `SUPABASE_ANON_KEY` in `local.properties`. Never use the `service_role` key anywhere in the app.
7. **CI builds with sync (optional).** GitHub › repo › Settings › Secrets and variables › Actions › New repository secret, one each for `SUPABASE_URL`, `SUPABASE_ANON_KEY` and `GOOGLE_WEB_CLIENT_ID`. The anon key is designed to ship inside apps; RLS is what protects the data.

### How sync works

- **Room is the source of truth.** Screens only ever read Room. Every local write marks the row `dirty`.
- **Push.** A WorkManager job upserts dirty rows (parents first) and marks them clean if they weren't edited again meanwhile. Photos upload to `photos/<cookbook>/<photo>.jpg` plus a thumbnail before their row is pushed, so the other phone never sees a photo without its file. Photos are at most 2048 px, so each goes up in one idempotent request; a failed upload is retried on the next pass.
- **Pull.** For each table, rows whose `server_updated_at` is newer than this phone's cursor (with a one-minute overlap), 500 at a time.
- **Conflicts: last write wins** by `updated_at`. The server trigger ignores an update older than what it has; the phone keeps an unpushed local edit unless the incoming one is newer. So both phones converge on the most recent edit. Deletes are soft, so they sync like any edit.
- **When.** On app start, about 1.5 s after any local change, within a second of a Realtime notice from the other phone, hourly in the background, and as soon as the network returns after an offline edit.
- **Household.** Phone 1: Settings › Share this cook book (publishes it with the same id) › Invite the other phone (a single-use code valid for 7 days). Phone 2: Settings › Join with a code. Recipes already on phone 2 move into the shared cook book.
- **Photos from the other phone** download into app storage after each pull, so they're available offline. The bucket is private; downloads are authenticated and checked by the same membership rule.

## Fonts

Fraunces (© The Fraunces Project Authors) and DM Sans (© The DM Sans Project Authors) are used under the SIL Open Font License 1.1. The license texts are in `licenses/` and are also bundled in the app (Settings › Font licenses).
