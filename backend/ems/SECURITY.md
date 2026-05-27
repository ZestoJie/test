Security recommendations for Firebase service account

1. Rotate the exposed service account key immediately

- Go to Google Cloud Console → IAM & Admin → Service Accounts
- Find `firebase-adminsdk-fbsvc` and revoke/delete the existing key
- Create a new key and keep it secret

2. Remove the file from the repository and history

- The checked-in file `src/main/resources/terminal71-ems-firebase-adminsdk-*.json` was removed from the working tree.
- To purge it from git history, run one of the following on a local clone (pick one):

Using BFG (recommended):

```bash
# Install BFG and run
bfg --delete-files 'terminal71-ems-firebase-adminsdk-*.json'
git reflog expire --expire=now --all && git gc --prune=now --aggressive
git push --force
```

Using git filter-repo:

```bash
git filter-repo --path backend/ems/src/main/resources/terminal71-ems-firebase-adminsdk-fbsvc-87187eda10.json --invert-paths
git push --force
```

3. Use Railway/Firebase environment variables instead of committing JSON

- Set `FIREBASE_SERVICE_ACCOUNT` to the raw JSON value or set `FIREBASE_SERVICE_ACCOUNT_B64` to a base64-encoded JSON string in Railway secret/variables.
- The application already supports these env vars: it will load the JSON directly or decode the B64 value.

4. Verify deployment configuration

- Ensure `.gitignore` contains `backend/ems/src/main/resources/terminal71-ems-firebase-adminsdk-*.json` (already present).
- Remove any local copies from build artifacts before committing.

5. Additional hardening

- Restrict the service account to the minimum required IAM roles.
- Use short-lived credentials or workload identity where possible.
- Enable logging/alerts on suspicious IAM activity.

6. After rotation

- Update Railway env vars with the new JSON/B64 value.
- Redeploy the service.

If you want, I can prepare a small script that converts a base64 env var into a runtime file and sets `GOOGLE_APPLICATION_CREDENTIALS` automatically during container startup.
