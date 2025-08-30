#!/bin/bash
# Wrapper: moved to scripts/tests/test-create-user.sh
exec "$(dirname "$0")/tests/test-create-user.sh" "$@"
