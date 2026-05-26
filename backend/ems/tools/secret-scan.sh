#!/usr/bin/env bash
# Quick secret scanner (grep-based). Run locally before committing.
echo "Scanning repo for common secret patterns..."
set -e
grep -RIn --exclude-dir=target --exclude-dir=node_modules -E "API_KEY|AIza|PRIVATE_KEY|private_key|JWT_SECRET|FIREBASE_SERVICE_ACCOUNT|PASSWORD|ACCESS_TOKEN|client_email" . || true
echo "Scan complete. Inspect matches above and remove secrets from repo."
