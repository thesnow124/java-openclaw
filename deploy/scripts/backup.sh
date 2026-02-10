#!/bin/bash

# OpenClaw Lite Backup Script
# Backs up data and configuration

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BACKUP_DIR="backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="openclaw-lite-backup-${TIMESTAMP}"
DATA_DIR="data"

echo -e "${GREEN}Creating backup...${NC}"

# Create backup directory
mkdir -p "${BACKUP_DIR}/${BACKUP_NAME}"

# Backup database
if [ -f "data/openclaw.db" ]; then
    echo "Backing up database..."
    cp "data/openclaw.db" "${BACKUP_DIR}/${BACKUP_NAME}/openclaw.db"
fi

# Backup configuration files
echo "Backing up configuration..."
cp -r src/main/resources "${BACKUP_DIR}/${BACKUPNAME}/"
cp pom.xml "${BACKUP_DIR}/${BACKUPNAME}/"

# Create backup metadata
cat > "${BACKUP_DIR}/${BACKUP_NAME}/metadata.txt" << METAEOF
Backup Name: ${BACKUP_NAME}
Timestamp: ${TIMESTAMP}
Date: $(date)
Hostname: $(hostname)
User: $(whoami)
METAEOF

# Compress backup
echo "Compressing backup..."
cd "${BACKUP_DIR}"
tar -czf "${BACKUP_NAME}.tar.gz" "${BACKUP_NAME}"
rm -rf "${BACKUP_NAME}"

echo -e "${GREEN}Backup created: ${BACKUP_DIR}/${BACKUP_NAME}.tar.gz${NC}"

# Cleanup old backups (keep last 7 days)
echo "Cleaning up old backups..."
find "${BACKUP_DIR}" -name "openclaw-lite-backup-*.tar.gz" -mtime +7 -delete

echo -e "${GREEN}Backup completed!${NC}"
