# POTATO V5.5 changes

- Keeps every V5.4 feature, including optional Accessibility context.
- Targets Android 16 / API 36 for current Google Play submission requirements.
- Upgrades Android Gradle Plugin to 8.12.2 and Gradle to 8.13.
- Removes the temporary external lint-runtime override after toolchain modernization.
- Adds a separate first-use Accessibility disclosure with affirmative Accept or Decline before normal app use.
- Explicitly declares the service is not an accessibility tool.
- Keeps Accessibility narrowly scoped to foreground package-name events with window-content retrieval disabled.
- Adds Play Console declaration and release-readiness documentation.
- Adds release App Bundle generation. Production signing remains a controlled credential step.
