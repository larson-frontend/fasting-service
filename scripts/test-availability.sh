#!/bin/bash
# Wrapper: moved to scripts/tests/test-availability.sh
exec "$(dirname "$0")/tests/test-availability.sh" "$@"
