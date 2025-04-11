# Git Collaboration Guidelines

This document outlines the best practices and conventions for using Git in a collaborative development environment.

## 1. Branching Strategy

- **`main` (or `master`)**:
    - Production-ready, stable code only.
    - Never commit directly to this branch.

- **`dev`**:
    - Main development branch.
    - Merge all feature branches into this branch.

- **`feature/<name>`**:
    - For new features.
    - Branch off from `dev`, and merge back via Pull Request (PR).

- **`bugfix/<name>`**:
    - For bug fixes, especially hotfixes.

- **`release/<version>`**:
    - For release preparation, including final testing and version bumping.

## 2. Commit Message Convention

Use clear, consistent commit messages to improve readability and history traceability.

### Format

```text
<type><optional scope>: <short summary>

[optional detailed explanation]
```

### Common Types

| Type     | Description                              |
|----------|------------------------------------------|
| feat     | A new feature                            |
| fix      | A bug fix                                |
| docs     | Documentation changes                    |
| style    | Formatting, missing semi-colons…         |
| refactor | Code change that is not a fix or feature |
| test     | Adding or fixing tests                   |
| chore    | Build process, tooling, etc.             |

### Examples

```text
feat: implement user login flow fix: resolve crash when username is empty docs: update setup instructions
```

## 3. Commit Requirements

- **Atomic Commits**: Each commit should represent a single logical change.
- **No junk commits** like `update`, `test`, `fix bug`.
- Avoid committing temporary or editor-specific files.
- Use descriptive commit messages following the above format.

## 4. Pull Request (PR) Guidelines

- All changes must go through a PR before being merged into shared branches.
- PRs should be:
- Based on the latest version of the `dev` branch
- Properly reviewed and approved
- Free of merge conflicts

## 5. Updating Your Feature Branch

When `main` is updated, rebase your feature branch:

```bash
git fetch origin
git rebase origin/main
```

## 6. Version Tags

- Use semantic versioning: v1.0.0, v1.1.0-beta, etc.
- Create a tag with:

```shell
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
```

- GPG signing is optional:

```shell
git tag -s v1.0.0 -m "Release v1.0.0"
```

## 7. .gitignore Rules

Ensure unnecessary files are excluded from Git.

## 8. Security Practices

- Do not commit sensitive information such as:
    - API keys
    - Passwords
    - Private certificates