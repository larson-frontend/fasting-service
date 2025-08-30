#!/bin/bash
# Wrapper: moved to scripts/tests/integration-test.sh
exec "$(dirname "$0")/scripts/tests/integration-test.sh" "$@"
