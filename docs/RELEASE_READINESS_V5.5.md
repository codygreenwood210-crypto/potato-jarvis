# POTATO V5.5 release readiness

## Automated build target
- compileSdk 36
- targetSdk 36
- minSdk 26
- JDK 17
- Android Gradle Plugin 8.12.2
- Gradle 8.13

## Outputs
CI produces a debug APK for development/device diagnostics and a release Android App Bundle (AAB). The release AAB is not considered publishable until it is signed with the developer upload key and accepted by Google Play.

## Manual/external gates
Google Play Console app creation, Play App Signing enrollment, upload-key custody, Accessibility Permission Declaration Form/review, Data safety/privacy declarations, Play Protect classification, internal-track installation, and physical-device regression testing are external/manual gates.
