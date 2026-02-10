#!/bin/bash

# OpenClaw Lite Restore Script
# Restores from backup

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

if [ -z "$1" ]; then
    echo "Usage: $0 <backup-tarball>"
    exit 1
fi

BACKUP_FILE="$1"

echo -e "${GREEN}Restoring from backup: ${BACKUP_FILE}${NC}"

# Extract backup
echo "Extracting backup..."
tar -xzf "${BACKUP_FILE}"

# Get backup name
BACKUP_NAME=$(basename "${BACKUP_FILE}" .tar.gz)

# Restore database
if [ -f "${BACKUP_NAME}/openclaw.db" ]; then
    echo "Restoring database..."
    mkdir -p data
    cp "${BACKUP_NAME}/openclaw.db" "data/openclaw.db"
fi

# Restore data
if [ -d "${BACKUP_NAME}/data" ]; then
    echo "Restoring data files..."
    cp -r "${BACKUP_NAME}/data/"* data/ 2>/dev/null || true
fi

# Restore configuration
if [ -f "${BACKUP_NAME}/pom.xml" ]; then
    echo "Restoring pom.xml..."
    cp "${BACKUP_NAME}/pom.xml" .
fi

echo -e "${GREEN}Restore completed!${NC}"
echo ""
echo "Restart service:"
echo "  ./deploy/scripts/stop.sh"
echo "  ./deploy/scripts/start.sh"
