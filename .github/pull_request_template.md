# Backend CI Rework PR

This PR updates CI to best practices:

- CI runs only for pull_request targeting main and develop.
- Push builds on feature branches are disabled.
- Path filters ignore docs and metadata.
- Concurrency cancels older runs for the same PR.
- Skip tokens are supported.
- Docker image validation occurs only for PRs to main and reuses the built JAR artifact (no rebuild).

## Skip Tokens
Supported tokens (PR title or latest commit message):
- [skip ci], [ci skip], [no ci]
- [skip actions], [actions skip]
- [skip dev-ci] (only affects PRs targeting develop)

## Force CI (override docs-only skip)
Use one of the following to force a CI run:
- Add [force ci] or [ci force] to the PR title or latest commit message
- Add the label ci:force to the PR
- Manually dispatch the workflow with force=true in the UI (Run workflow)

## Checklist
- [ ] CI runs for PRs against main & develop
- [ ] CI for PRs against develop can be skipped via [skip dev-ci]
- [ ] Deploy starts only from main (not part of this CI workflow)
- [ ] No duplicate builds, no unnecessary triggers
- [ ] Concurrency cancels older runs per PR/branch
- [ ] Path filters (paths-ignore) take effect for docs/meta changes

## Notes
- Security: minimal permissions, artifact reuse between jobs
- Scaling: concurrency enabled, no pushes to registry from CI
