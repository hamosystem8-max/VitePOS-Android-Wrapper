# VitePOS Android Wrapper v0.2.1 — GitHub Build

This package is arranged as a complete GitHub repository.

## Automatic APK build

GitHub Actions workflow:
`.github/workflows/build-apk.yml`

It builds:
`VitePOS-Android-Wrapper-v0.2.1-debug.apk`

### Manual trigger
GitHub -> Actions -> Build VitePOS Android APK -> Run workflow

### Automatic trigger
Any push to `main` that changes Android source/build files starts a build automatically.

### Download
Open the successful Actions run and download the artifact:
`VitePOS-Android-Wrapper-v0.2.1`

The artifact ZIP contains:
- VitePOS-Android-Wrapper-v0.2.1-debug.apk
- SHA-256 checksum
