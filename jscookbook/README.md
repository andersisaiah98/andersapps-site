# J's Cook Book

Julia's personal recipe and meal book for Android: recipes filed into categories, photos, ingredients, steps, a cook log of every time she's made something, and "give me something to make" shortcuts. Tactile, springy and photo-forward.

Native Kotlin + Jetpack Compose, offline-first (Room), with Supabase sync between two phones (Phase 2).

## Status

| Phase | What | State |
|---|---|---|
| 0 | Foundation: scaffold, theme and tokens (light + dark), bundled fonts, bottom navigation shell, fallback art, hidden design-system screen | **Done, awaiting review** |
| 1 | Local core: Room, recipe/category CRUD, detail, grid, shared-element tile → detail, photo viewer | Not started |
| 2 | Sync: Supabase schema + RLS, auth, household, sync engine, photo uploads, Realtime | Not started |
| 3 | Cook log + Journal, quick actions + randomizer, cooking mode, search + filters | Not started |
| 4 | Drag to organize, pinch grid density, swipe actions, haptics pass, Baseline Profiles, jank check | Not started |
| 5 | Extras (ask first): import from URL, share target | Not started |

## Requirements

- Android Studio (current stable) with its bundled JDK 21
- Android SDK Platform **37** (Android 17) and a current Build Tools. Android Studio offers to install them on first sync.
- Phones with USB debugging on. Tested targets: Pixel 11 Pro and Pixel 7 Pro (the performance floor), both Android 17.

The build uses the Gradle wrapper (Gradle 9.5.0), AGP 9.3.2 with built-in Kotlin, Kotlin 2.4.20 and the Compose BOM 2026.09.00. Versions live in `gradle/libs.versions.toml`.

## Setup

1. Open the `jscookbook/` folder (not the repo root) in Android Studio and let it sync.
2. Android Studio writes `local.properties` with `sdk.dir` for you. It is gitignored.
3. Nothing else is needed until Phase 2.

### `local.properties` keys

Fill these in on your machine only. Never commit them, and never paste them into chat.

| Key | Needed from | What it is |
|---|---|---|
| `sdk.dir` | Phase 0 | Written by Android Studio |
| `SUPABASE_URL` | Phase 2 | Your Supabase project URL (Project Settings › API) |
| `SUPABASE_ANON_KEY` | Phase 2 | The project's **anon / public** key. Never the service-role key. |

### Release signing (before Phase 2)

Phases 0 and 1 sign everything with the debug key. Before Phase 2 you'll create a release keystore on your machine; its SHA-1 is needed for Google sign-in. The exact `keytool` command and the gitignored `keystore.properties` format will be added here at the start of Phase 2.

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

A minified release build (still debug-signed in Phases 0–1) for checking real performance:

```sh
./gradlew assembleRelease
adb -s <serial> install -r app/build/outputs/apk/release/app-release.apk
```

## Tests

```sh
./gradlew :core:designsystem:testDebugUnitTest   # fallback-art determinism, palette contrast
./gradlew connectedDebugAndroidTest              # navigation UI test, runs on each connected phone
```

## Project layout

```
jscookbook/
  app/                      Single activity, navigation shell, screens
    ui/navigation/          Type-safe routes, NavHost, the floating bottom bar with the raised ＋
    ui/home, cookbook, journal, settings, add
    data/, di/              Settings (DataStore) and Hilt wiring
  core/designsystem/        Everything visual, shared by every screen
    theme/                  BM color tokens, light/dark schemes, type, shapes, elevation, springs
    component/              Buttons, cards, tiles, quick-action cards, segmented control, haptics
    recipeimage/            RecipeImageProvider, ImageSource, fallback-art illustrations
    catalog/                The hidden design-system screen
    res/font/               Fraunces + DM Sans (variable, bundled)
  licenses/                 SIL Open Font License texts for the bundled fonts
```

Later phases add `:core:data` (Room + sync), `:baselineprofile` and `:macrobenchmark`.

## Design system

- **Palette:** Benjamin Moore colors from Amerie Creative's "TerraCotta" palette, as tokens named after the paints (`BM.TerraCottaTile`, `BM.CloudWhite`, …). Colors that aren't paints live in `Derived` and each one says what it came from. Material You dynamic color is never used.
- **Type:** Fraunces for display and headlines (optical size and "wonky" axes tuned per size), DM Sans for UI and body. Both are bundled in `res/font`, so there's no font swap on first launch and everything works offline.
- **Motion:** named springs (`Snappy`, `Bouncy`, `Gentle`, `Hero`, `Playful`) and no linear tweens. When the system "Remove animations" setting is on, every spec snaps and decorative motion is skipped (`LocalReducedMotion`).
- **Fallback art:** recipes without a photo get an illustration chosen from the title, category and type, a palette tint from an FNV-1a hash of the title, and the initial in Fraunces. It is deterministic, so both phones always agree. It sits behind `RecipeImageProvider` with `ImageSource = PHOTO | FALLBACK | AI`, ready for AI images later.
- **Hidden design-system screen:** Settings › tap **Version** 7 times. It shows every color with its name, code and hex, live contrast ratios, the type scale, shapes, elevation, buttons, cards, the full fallback-art set, the icons, live spring demos and every haptic, with a light/dark toggle.

## Supabase

Set up in Phase 2. SQL migrations and RLS policies will live in `supabase/migrations/`, with step-by-step dashboard instructions here.

## Fonts

Fraunces (© The Fraunces Project Authors) and DM Sans (© The DM Sans Project Authors) are used under the SIL Open Font License 1.1. The license texts are in `licenses/` and are also bundled in the app (Settings › Font licenses).
