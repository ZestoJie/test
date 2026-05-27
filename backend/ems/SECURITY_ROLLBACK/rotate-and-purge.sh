#!/usr/bin/env bash
set -euo pipefail
echo "1) Rotate Firebase service account key (manual step):"
echo "   - Go to https://console.cloud.google.com/iam-admin/serviceaccounts"
echo "   - Select the service account used by Firebase Admin and revoke/delete the compromised key"
echo "   - Create a new JSON key and store it securely (do NOT commit)"

echo "\n2) Store new key in Railway (or CI/CD) as base64 and update env vars:\n"
echo "   # locally encode"
echo "   base64 service-account.json | tr -d '\n' > svc.b64"
echo "   # then set Railway env var FIREBASE_SERVICE_ACCOUNT_B64 to contents of svc.b64"

echo "\n3) Purge from git history using BFG (example):\n"
echo "   # Install BFG, then run:\n"
echo "   bfg --delete-files 'terminal71-ems-firebase-adminsdk-*.json'"
echo "   git reflog expire --expire=now --all && git gc --prune=now --aggressive"
echo "   git push --force"

echo "Alternative using git filter-repo:\n"
echo "   git filter-repo --invert-paths --path backend/ems/src/main/resources/terminal71-ems-firebase-adminsdk-fbsvc-87187eda10.json"
echo "   git push --force"

echo "After purge: rotate any keys that may have been exposed and notify stakeholders."
