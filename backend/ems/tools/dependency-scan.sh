#!/usr/bin/env bash
set -e
echo "Running maven dependency tree and saving to reports/dependency-tree.txt"
mkdir -p REPORTS
mvn -DskipTests dependency:tree > REPORTS/dependency-tree.txt || true
echo "Dependency tree saved to REPORTS/dependency-tree.txt"

echo "Suggested next steps: run 'mvn -DskipTests org.owasp:dependency-check-maven:check' or use Snyk/GitHub Dependabot for automated CVE checks."
