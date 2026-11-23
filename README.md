<img src="assets/icon.png" alt="UntisX app icon" width="144" />

# UntisX
An alternative mobile client for the Untis timetable system.

**Current version:** 6.0.0-beta01 (beta release)

UntisX was originally forked from [BetterUntis](https://github.com/SapuSeven/BetterUntis) and is maintained independently by [rxnzolaf](https://github.com/rxnzolaf).

<!-- <a href="https://github.com/rxnzolaf/UntisX/actions/workflows/build.yml"><img src="https://github.com/rxnzolaf/UntisX/actions/workflows/build.yml/badge.svg" alt="Build"></a> (Can be re-enabled when automatic workflow runs are restored) -->

## Screenshots

Screenshots coming soon.

## Top features
- Modern and intuitive user interface
- Selectable app themes: "Standard" and "Gaming" (blue-purple gaming design), switchable under Settings > Design
- Simple and straight-forward login process
- Fast timetable loading with smart caching
- Multiple separate accounts/profiles
- RoomFinder to quickly find free rooms
- Info Center for viewing events, contact hours and own absences
- Class management features for teachers (Absence checking, lesson topic editing, ...)
- Break notifications and auto-mute during lessons
- Privacy-focused design with manual bug reporting via GitHub issues (no automatic error tracking or crash reporting)

## Available languages

[![Translation status](https://hosted.weblate.org/widget/betteruntis/translations/multi-auto.svg)](https://hosted.weblate.org/engage/betteruntis/)

## Development history

### Origin: BetterUntis
UntisX started as a fork of [BetterUntis](https://github.com/SapuSeven/BetterUntis), the alternative Untis client by [SapuSeven](https://github.com/SapuSeven).
Before the fork, the upstream project went through the following major versions:

#### v3.x
The original version of BetterUntis had many design and performance flaws.
Since development became increasingly more difficult, the project was scrapped and rewritten from scratch.

Although some parts of the original code were reused, the codebase was entirely switched to Kotlin.
Kotlin has many features and libraries that immensely help to communicate with the Untis API and process the timetable data.

Another major change was the use of a custom WeekView (based on [Till Hellmund](https://github.com/thellmund)'s fork of [Android-Week-View](https://github.com/alamkanak/Android-Week-View)) for the timetable display, which improved performance by a lot.

#### v4.x
This major version updated the WeekView component again to be based entirely on Jetpack Compose.
Since the rest of the app was already built on Compose, this made it easier to develop and improve the WeekView component.

#### v5.x
This version introduced a complete architecture overhaul to follow the latest Android Jetpack guidelines.
Additionally, several components (like the Untis API Client and the Compose Preferences) were separated into their own modules
to potentially reuse them in other projects.

### UntisX

#### v6.0.0-beta01
UntisX picks up at BetterUntis 5.x and continues development independently under maintainer rxnzolaf.
Key changes in 6.0.0-beta01 include:
- Renamed project to UntisX with new application ID (`com.rxnzolaf.untis`).
- Introduced selectable themes with a new "Gaming" (blue-purple gaming design) theme alongside the "Standard" theme.
- Removed automatic error tracking/crash reporting in favor of transparent, manual GitHub bug reports.
- Retained the modular architecture (`app` module, `api`, `material-color-utils`) and Kotlin/Jetpack Compose codebase.

## Project Git Structure

### Branch: master
**master** is the only permanent branch of this repository and always contains the latest state of the project.
Release tags (for example `6.0.0-beta01`) are created from **master**, which is also the base for all pull requests.

### Other branches
Bigger features and experiments that require multiple commits should branch off **master** and merge back into it.
These branches have a limited lifetime and should be deleted after the merge if they are no longer needed.

### Builds and releases
The application ID is `com.rxnzolaf.untis` (or `com.rxnzolaf.untis.debug` for debug builds).

The repository uses GitHub Actions for CI/CD:

- `build.yml` builds the debug and release APKs for both the **Gms** and **Foss** product flavors (`gmsDebug`, `gmsRelease`, `fossDebug`, `fossRelease`). Automatic triggers are currently disabled, so workflow runs are triggered manually (`workflow_dispatch`).
- `deploy.yml` creates release builds (APKs and AAB bundles), publishes GitHub releases, deploys to Google Play Store tracks, and prepares F-Droid releases when manually triggered.

## Reporting bugs

If you encounter a bug or issue in UntisX, please submit a report manually via [GitHub Issues](https://github.com/rxnzolaf/UntisX/issues/new).

When opening an issue, please include:
- A description of what happened and steps to reproduce the issue.
- Your UntisX version (e.g., `6.0.0-beta01`) and Android version.
- Device model.

**Important:** Do not include personal data, credentials, or passwords in your bug report.

## Contributing

Your help is greatly appreciated, but first please read the following sections to prevent common mistakes:

### Translating into another language

All texts live in `app/src/main/res/values/strings.xml`, with one `values-<language>` folder per translation.
UntisX inherits its translations from the [BetterUntis translation project](https://hosted.weblate.org/projects/betteruntis/translations/) on [Weblate](https://hosted.weblate.org/accounts/register/).

If you haven't already, sign up for an account there, select your desired language (or click the “Start new translation“ button at the bottom) and look at "Strings status".
All categories marked in red are areas where translations are missing or potentially incorrect. Click on them to start translating.
You can also open a pull request with your changes directly against **master**.

### Implementing new features / Fixing bugs

Anything that requires you to perform code changes should be done on a branch based on **master**, and submitted as a pull request against **master**.
Keep pull requests focused on a single feature or fix, and describe what it changes and why.

## Credits & License

UntisX is based on **BetterUntis** by [SapuSeven](https://github.com/SapuSeven) and is maintained by [rxnzolaf](https://github.com/rxnzolaf). It builds on the work of the original project and its contributors.
All credit for the original app, its design and its API client goes to them.

- Original project: [SapuSeven/BetterUntis](https://github.com/SapuSeven/BetterUntis)
- WeekView based on [Till Hellmund](https://github.com/thellmund)'s fork of [Android-Week-View](https://github.com/alamkanak/Android-Week-View)

UntisX keeps the license of the original project: the **GNU General Public License v3.0** (GPL-3.0).
See [LICENSE](LICENSE) for the full license text. As a modified version of the original work, UntisX stays under the GPL-3.0 and the copyright of the original authors remains with them.

Copyright (C) RxNzOlaf
