#!/bin/bash

# OpenClaw Lite Stop Script
# Gracefully stops the application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Stopping OpenClaw Lite...${NC}"

# Configuration
PID_FILE="openclaw-lite.pid"

# Check if PID file exists
if [ ! -f "${PID_FILE}" ]; then
    echo -e "${YELLOW}OpenClaw Lite is not running${NC}"
    exit 0
fi

# Read PID
PID=$(cat "${PID_FILE}")

# Check if process is running
if ! ps -p ${PID} > /dev/null; then
    echo -e "${YELLOW}OpenClaw Lite is not running (removing stale PID file)${NC}"
    rm "${PID_FILE}"
    exit 0
fi

# Graceful shutdown
echo "Sending TERM signal to PID ${PID}..."
kill -TERM ${PID}

# Wait for graceful shutdown
TIMEOUT=30
COUNT=0
while [ ${COUNT} -lt ${TIMEOUT} ]; do
    if ! ps -p ${PID} > /dev/null; then
        echo -e "${GREEN}OpenClaw Lite stopped gracefully${NC}"
        rm "${PID_FILE}"
        exit 0
    fi
    sleep 1
    ((COUNT++))
done

# Force kill if still running
if ps -p ${PID} > /dev/null; then
    echo -e "${RED}Force killing OpenClaw Lite...${NC}"
    kill -9 ${PID}
    sleep 2
    
    if ps -p ${PID} > /dev/null; then
        echo -e "${RED}Failed to stop OpenClaw Lite${NC}"
        exit 1
    fi
fi

rm "${PID_FILE}"
echo -e "${GREEN}OpenClaw Lite stopped${NC}"
