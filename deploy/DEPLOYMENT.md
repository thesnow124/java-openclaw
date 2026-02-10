# OpenClaw Lite - Deployment Guide

## Prerequisites

### Software Requirements
- Java 21+ (Eclipse Temurin or OpenJDK)
- Maven 3.9+
- Docker & Docker Compose (optional, for containerized deployment)
- Git

### Hardware Requirements (Minimum)
- CPU: 2 cores
- RAM: 2 GB
- Disk: 10 GB

### Hardware Requirements (Recommended for Production)
- CPU: 4+ cores
- RAM: 4 GB
- Disk: 50 GB SSD

## Installation

### Option 1: Standalone JAR

1. **Build the application**:
   ```bash
   git clone <repository-url>
   cd java-openclaw-lite
   mvn clean package
   ```

2. **Run the application**:
   ```bash
   java -jar target/openclaw-lite-1.0.0.jar start
   ```

3. **Run as service (Linux)**:
   ```bash
   # Deploy scripts
   cp deploy/scripts/*.sh /usr/local/bin/
   
   # Start
   start.sh
   
   # Stop
   stop.sh
   ```

### Option 2: Docker Deployment

1. **Using Docker Compose**:
   ```bash
   git clone <repository-url>
   cd java-openclaw-lite
   
   # Start all services
   docker-compose up -d
   
   # View logs
   docker-compose logs -f openclaw-lite
   
   # Stop services
   docker-compose down
   ```

2. **Using Docker**:
   ```bash
   # Build image
   docker build -t openclaw-lite:latest .
   
   # Run container
   docker run -d \
     --name openclaw-lite \
     -p 8080:8080 \
     -v $(pwd)/data:/app/data \
     openclaw-lite:latest
   ```

### Option 3: Systemd Service (Linux)

Create `/etc/systemd/system/openclaw-lite.service`:

```ini
[Unit]
Description=OpenClaw Lite AI Agent System
After=network.target

[Service]
Type=simple
User=openclaw
WorkingDirectory=/opt/openclaw-lite
ExecStart=/usr/bin/java -jar /opt/openclaw-lite/app.jar start
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable openclaw-lite
sudo systemctl start openclaw-lite
sudo systemctl status openclaw-lite
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | 8080 |
| `ZHIPUAI_API_KEY` | ZhipuAI API key | (empty) |
| `OPENAI_API_KEY` | OpenAI API key for embeddings | (empty) |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `default` |

### Configuration Files

- `application.yml` - Default configuration
- `application-prod.yml` - Production configuration
- `application-dev.yml` - Development configuration

## Monitoring

### Prometheus Metrics

Access metrics at: `http://localhost:8080/actuator/prometheus`

### Key Metrics

| Metric | Description |
|--------|-------------|
| `jvm_memory_used_bytes` | JVM memory usage |
| `jvm_threads_live_threads` | Active thread count |
| `http_server_requests_seconds_count` | HTTP request rate |
| `system_cpu_usage` | CPU usage percentage |

### Health Checks

Health check endpoint: `http://localhost:8080/actuator/health`

Components:
- Database health
- Data directory status
- Plugins directory status

### Prometheus + Grafana Setup

1. **Add to docker-compose.yml**:
   ```yaml
   prometheus:
     image: prom/prometheus:latest
     ports:
       - "9090:9090"
     volumes:
       - ./deploy/prometheus:/etc/prometheus
       
   grafana:
     image: grafana/grafana:latest
     ports:
       - "3001:3000"
     environment:
       - GF_SECURITY_ADMIN_PASSWORD=admin
     volumes:
       - grafana-data:/var/lib/grafana
       - ./deploy/monitoring/grafana/provisioning:/etc/grafana/provisioning
   ```

2. **Import Dashboard**:
   - Access Grafana at http://localhost:3001
   - Login (admin/admin)
   - Add Prometheus data source
   - Import dashboard from `deploy/monitoring/grafana/dashboards/openclaw-dashboard.json`

## Backup and Recovery

### Automated Backup

Run backup script:
```bash
./deploy/scripts/backup.sh
```

Backup locations:
- `backups/openclaw-lite-backup-YYYYMMDD_HHMMSS.tar.gz`
- Retained for 7 days automatically

### Manual Backup

Backup database and config:
```bash
# Backup database
cp data/openclaw.db backups/openclaw-latest.db

# Backup agents and plugins
tar -czf backups/agents-plugins-$(date +%Y%m%d).tar.gz \
  data/agents/ \
  data/plugins/
```

### Restore from Backup

```bash
# Extract backup
cd backups
tar -xzf openclaw-lite-backup-YYYYMMDD_HHMMSS.tar.gz

# Restore database
cp openclaw-lite-backup-YYYYMMDD_HHMMSS/openclaw.db ../data/openclaw.db

# Restart service
./stop.sh
./start.sh
```

## Scaling

### Horizontal Scaling

OpenClaw Lite supports horizontal scaling through:

1. **Stateless API**: REST API is stateless
2. **Session Affinity**: WebSocket connections can use sticky sessions
3. **Load Balancer**: Place a load balancer (Nginx/HAProxy) in front

### Nginx Configuration Example

```nginx
upstream openclaw_lite {
    server 192.168.1.10:8080;
    server 192.168.1.11:8080;
    server 192.168.1.12:8080;
}

server {
    listen 80;
    server_name openclaw.example.com;
    
    location / {
        proxy_pass http://openclaw_lite;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
    
    location /ws/ {
        proxy_pass http://openclaw_lite/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

## Troubleshooting

### Common Issues

**1. Application won't start**

Check logs:
```bash
tail -f logs/application.log
```

Common causes:
- Port 8080 already in use
- Java version too old
- Missing dependencies

**2. Out of memory**

Increase heap size:
```bash
export JAVA_OPTS="-Xmx4g"
java -jar target/openclaw-lite-1.0.0.jar start
```

**3. Database locked**

SQLite error: "database is locked"

- Ensure only one instance is running
- Check for zombie processes

**4. Channels not connecting**

- Verify API tokens
- Check network connectivity
- Review channel logs

### Debug Mode

Enable debug logging:
```yaml
logging:
  level:
    com.openclawlite: DEBUG
```

## Security

### Production Security Checklist

- [ ] Change default passwords
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Set up API key rotation
- [ ] Enable rate limiting
- [ ] Configure CORS appropriately
- [ ] Enable audit logging
- [ ] Regular security updates

### API Security

- Use environment variables for secrets
- Restrict API exposure with `application-prod.yml`
- Use reverse proxy for SSL termination
- Implement rate limiting (Nginx/CloudFlare)

## Maintenance

### Regular Tasks

| Task | Frequency | Command |
|------|-----------|---------|
| Backup | Daily | `./deploy/scripts/backup.sh` |
| Log rotation | Automatic | Configured in logback |
| Updates | Weekly | `git pull && mvn clean package` |
| Health Check | Continuous | `/actuator/health` |

### Update Procedure

```bash
# 1. Stop service
./deploy/scripts/stop.sh

# 2. Backup current version
./deploy/scripts/backup.sh

# 3. Update code
git pull

# 4. Build new version
mvn clean package

# 5. Start service
./deploy/scripts/start.sh

# 6. Verify
curl http://localhost:8080/api/admin/status
```

## Support

### Getting Help

- Documentation: [README.md](../../README.md)
- Issues: [GitHub Issues](https://github.com/openclawlite/java-openclaw-lite/issues)
- Logs: `logs/application.log`

### Debug Information Collection

When reporting issues, include:
- OpenClaw Lite version: `java -jar target/openclaw-lite-1.0.0.jar --version`
- Java version: `java -version`
- OS information: `uname -a`
- Logs: Last 100 lines from `logs/application.log`
