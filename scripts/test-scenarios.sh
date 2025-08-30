#!/bin/bash
# Wrapper: moved to scripts/tests/test-scenarios.sh
exec "$(dirname "$0")/tests/test-scenarios.sh" "$@"
