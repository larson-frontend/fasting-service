# CI/CD Guide (fasting-service)

This project uses modular GitHub Actions for CI (backend-ci.yml) and deploy (backend-deploy.yml) with PR‑only builds, skip/force tokens, path filtering, concurrency, and artifact reuse.

## TL;DR
- CI runs only for pull_request targeting develop and main.
- No push builds for feature branches.
- Docs/meta-only PRs are auto-skipped fast (unless forced).
- You can force CI with tokens, a label, or manual dispatch.
- Deploy runs only on push to main after merge and reuses CI artifacts (no rebuild) when available.
- Concurrency cancels older runs for the same PR automatically.

## Triggers
- CI: pull_request to develop or main.
- Deploy: push to main (after merge), plus manual workflow_dispatch.

## Skip tokens (to avoid CI)
Put tokens in the PR title or the latest commit message:
- Global skip: `[skip ci]`, `[ci skip]`, `[no ci]`, `[skip actions]`, `[actions skip]`
- Develop-only skip (does not affect PRs to main): `[skip dev-ci]`

## Force CI (override skips and docs-only auto-skip)
Any of these will force a run:
- Add token in PR title or latest commit message: `[force ci]` or `[ci force]`
- Add label to PR: `ci:force`
- Manual run: open the workflow “Backend CI” and use Run workflow with `force=true`

Notes:
- Force overrides both docs-only auto-skip and the skip tokens above.
- Force tokens must be in the PR title or the latest commit message, not in PR comments.

## Docs-only detection
Changes are considered docs/meta-only when:
- Only files match: `**/*.md`, `LICENSE`, `.github/**`, `CHANGELOG.md`
- AND no other code paths changed.

Default behavior: CI exits early without running Maven. Add a force option to override.

## Examples

### 1) Skip CI globally (any PR)
- PR title: `update docs [skip ci]`
- or commit message trailer:
  ```
  Update documentation
  
  [skip ci]
  ```

### 2) Build PR to develop by default, but skip explicitly
- PR title: `feat: new preferences API [skip dev-ci]`

### 3) Force CI for a docs-only PR
- PR title: `docs: api usage [force ci]`
- or add label: `ci:force`
- or manual run: use “Run workflow” in GitHub UI with `force=true`

### 4) Force CI via commit message
- Commit message:
  ```
  Docs pass 2
  
  [ci force]
  ```

### 5) Label via CLI (force build)
- With GitHub CLI:
  ```bash
  gh pr edit <PR_NUMBER> --add-label 'ci:force'
  ```

### 6) Manual dispatch with force
- From the Actions page: Backend CI → Run workflow → set `force=true` → Run
- With CLI (optional):
  ```bash
  gh workflow run backend-ci.yml -f force=true
  ```

## No duplicate builds
- Only pull_request events build; pushes to feature branches do not.
- Concurrency cancels in-progress older runs for the same PR/branch.

## Docker image in CI
- For PRs to main only, CI validates a Docker image build locally (no push), reusing the JAR artifact from the build job.

## Deploy (backend-deploy.yml)
- Trigger: push to `main` (after merge), or manual dispatch.
- It tries to download the CI artifact (JAR) from the merged PR. If not found, it falls back to building once in deploy.
- Builds and pushes a runtime image to GHCR using the artifact (no Maven rebuild in the normal path).
- Runs Trivy scan and fails on HIGH/CRITICAL.

## Security & permissions
- Minimal permissions for CI; deploy grants packages + id-token for GHCR.
- Secrets are not used in CI for pushing images; pushing happens only in deploy.

## Troubleshooting
- CI didn’t run on your PR?
  - Check the base branch (must be develop or main).
  - If only docs/meta changed, CI will auto-skip. Add `[force ci]` or label `ci:force`.
  - Ensure your skip token isn’t present by accident in the title or latest commit.
- Deploy didn’t reuse artifacts?
  - The deploy workflow parses the merge commit to detect the PR; if it can’t find artifacts, it will build once as a fallback.

