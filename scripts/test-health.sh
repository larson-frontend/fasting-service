#!/bin/bash
# Wrapper: moved to scripts/tests/test-health.sh
exec "$(dirname "$0")/tests/test-health.sh" "$@"
