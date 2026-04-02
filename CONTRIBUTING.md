# Contributing

Thank you for contributing to Zorvyn Finance Backend.

## Development Setup

1. Install JDK 8.
2. Ensure MySQL is running (or use test profile where applicable).
3. Run:

```powershell
.\mvnw.cmd test
```

## Branch and Commit Guidelines

- Use short-lived feature/fix branches.
- Keep commits focused and atomic.
- Use clear commit messages, for example:
  - `feat: add dashboard monthly trend endpoint`
  - `fix: enforce analyst access for insights endpoint`
  - `docs: update README auth section`

## Pull Request Checklist

- Tests pass locally.
- API behavior and RBAC are validated.
- No secrets are committed.
- Documentation is updated for endpoint or config changes.
