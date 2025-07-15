# Automated Release System for macOS Improvements

This document explains how the automated release system works for the macOS improvements branch.

## How It Works

The enhanced GitHub Actions workflow automatically creates releases when code is pushed to the `macOS-improvements` branch.

### Automatic Version Bumping

The system uses **semantic versioning** based on commit messages:

- **Major version bump** (e.g., 1.0.0 → 2.0.0): Triggered by commit messages containing "breaking" or "major"
- **Minor version bump** (e.g., 1.0.0 → 1.1.0): Triggered by commit messages containing "feat", "feature", or "minor"
- **Patch version bump** (e.g., 1.0.0 → 1.0.1): Default for all other commits

### Release Creation Process

1. **Version Detection**: The workflow analyzes commit messages since the last tag
2. **Version Calculation**: Determines the appropriate version bump type
3. **POM Update**: Updates the Maven `pom.xml` with the new version
4. **Build**: Compiles and packages the application
5. **Release Notes**: Generates comprehensive release notes including:
   - List of changes from commit messages
   - Installation instructions
   - Technical details (build date, commit SHA, version bump type)
6. **Tag Creation**: Creates and pushes a new Git tag
7. **GitHub Release**: Creates a GitHub release with the built packages

### Release Content

Each release includes:
- **macOS Package (.pkg)**: Ready-to-install package
- **macOS Disk Image (.dmg)**: Drag-and-drop installer
- **Detailed release notes**: With installation and technical information
- **Prerelease flag**: Marked as prerelease for testing

## Usage Examples

### Triggering Different Version Bumps

```bash
# Patch version bump (1.0.0 → 1.0.1)
git commit -m "Fix macOS PKG creation issue"

# Minor version bump (1.0.0 → 1.1.0)
git commit -m "Add new feature for better error handling"

# Major version bump (1.0.0 → 2.0.0)
git commit -m "Breaking change: redesign packaging system"
```

### Viewing Releases

Releases are automatically published to:
`https://github.com/originalmagneto/autogram-macOS/releases`

## Benefits

✅ **Automatic**: No manual release creation needed
✅ **Consistent**: Standardized release notes and versioning
✅ **Traceable**: Clear connection between commits and releases
✅ **User-friendly**: Detailed installation instructions
✅ **Developer-friendly**: Technical details for debugging

## Pull Request Testing

For pull requests, the workflow still uploads artifacts (not releases) for testing purposes. Only pushes to the main branch create actual releases.

## Troubleshooting

If a release fails to create:
1. Check the [Actions tab](https://github.com/originalmagneto/autogram-macOS/actions) for error details
2. Ensure the build completes successfully
3. Verify that the `GITHUB_TOKEN` has sufficient permissions
4. Check that the version calculation logic works correctly

Common issues:
- **No releases created**: Check that your commit messages follow the expected patterns
- **Wrong version bump**: Ensure your commit messages contain the right keywords
- **Build failures**: Check the Actions tab for detailed error logs
- **Version format errors**: The system now filters for semantic version tags (vX.Y.Z) and validates components to prevent zero-length errors

## Recent Improvements

### Version Handling Fix
The automated release system has been improved to handle edge cases:

- **Semantic Version Filtering**: Only considers tags in `vX.Y.Z` format (e.g., `v1.0.0`, `v2.1.3`)
- **Non-semantic Tag Handling**: Ignores tags like `test`, `dev`, or other non-version tags
- **Version Validation**: Validates extracted version components to prevent packaging errors
- **Fallback Mechanism**: Defaults to `v1.0.0` when no valid semantic version tags exist

This resolves the "Version [test.1.0] contains a zero length component" error that was occurring during Maven packaging.

---

*This automated system ensures that every successful build on the macOS-improvements branch results in a properly versioned, documented, and distributed release.*