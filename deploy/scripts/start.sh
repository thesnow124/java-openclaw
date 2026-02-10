#!/bin/bash

# OpenClaw Lite Start Script
# Production deployment script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting OpenClaw Lite...${NC}"

# Configuration
APP_NAME="openclaw-lite"
JAR_NAME="openclaw-lite-1.0.0.jar"
JAR_FILE="target/${JAR_NAME}"
PID_FILE="openclaw-lite.pid"
LOG_DIR="logs"
DATA_DIR="data"

# Create necessary directories
mkdir -p "${LOG_DIR}"
mkdir -p "${DATA_DIR}/plugins"
mkdir -p "${DATA_DIR}/agents"
mkdir - "${DATA_DIR}/sessions"

# Check if JAR exists
if [ ! -f "${JAR_FILE}" ]; then
    echo -e "${RED}Error: JAR file not found: ${JAR_FILE}${NC}"
    echo "Please run 'mvn clean package' first."
    exit 1
fi

# Check if already running
if [ -f "${PID_FILE}" ]; then
    PID=$(cat "${PID_FILE}")
    if ps -p ${PID} > /dev/null; then
        echo -e "${YELLOW}OpenClaw Lite is already running (PID: ${PID})${NC}"
        exit 0
    else
        echo -e "${YELLOW}Removing stale PID file${NC}"
        rm "${PID_FILE}"
    fi
fi

# JVM options for production
JAVA_OPTS="${JAVA_OPTS} -Xms512m -Xmx2g"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseG1GC"
JAVA_OPTS="${JAVA_OPTS} -XX:MaxGCPauseMillis=200"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseStringDeduplication"
JAVA_OPTS="${JAVA_OPTS} -Djava.awt.headless=true"
JAVA_OPTS="${JAVA_OPTS} -Dspring.main.web-application-type=reactive"

# Start the application
echo "Starting with options: ${JAVA_OPTS}"
nohup java ${JAVA_OPTS} -jar "${JAR_FILE}" start \
    > "${LOG_DIR}/application.log" 2>&1 &

# Save PID
echo $! > "${PID_FILE}"

echo -e "${GREEN}OpenClaw Lite started successfully!${NC}"
echo "PID: $(cat ${PID_FILE})"
echo "Logs: ${LOG_DIR}/application.log"
echo ""
echo "Check status:"
echo "  curl http://localhost:8080/api/admin/status"
echo ""
echo "Stop service:"
echo "  ./deploy/scripts/stop.sh"
