#!/bin/bash
# Wrapper: moved to scripts/ci/github-setup.sh
exec "$(dirname "$0")/scripts/ci/github-setup.sh" "$@"
