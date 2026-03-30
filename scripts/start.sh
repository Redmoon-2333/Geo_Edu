#!/bin/bash

# ============================================
# GeoEdu Backend - Start Script (Linux/Mac)
# ============================================

set -e

APP_NAME="GeoEdu"
JAR_FILE="geoedu-0.0.1-SNAPSHOT.jar"
PID_FILE="geoedu.pid"
LOG_FILE="logs/geoedu.log"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_java() {
    if ! command -v java &> /dev/null; then
        log_error "Java not found. Please install Java 17 or later."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        log_error "Java version must be 17 or later. Current version: $JAVA_VERSION"
        exit 1
    fi
    log_info "Java version check passed (version $JAVA_VERSION)"
}

check_port() {
    PORT=${SERVER_PORT:-8080}
    if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warn "Port $PORT is already in use"
        lsof -Pi :$PORT -sTCP:LISTEN
        read -p "Do you want to continue? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        log_info "Port $PORT is available"
    fi
}

wait_for_service() {
    local host=$1
    local port=$2
    local service=$3
    local max_attempts=${4:-30}
    local attempt=1
    
    log_info "Waiting for $service at $host:$port..."
    
    while ! nc -z $host $port 2>/dev/null; do
        if [ $attempt -ge $max_attempts ]; then
            log_error "$service is not available after $max_attempts attempts"
            return 1
        fi
        echo -n "."
        sleep 1
        ((attempt++))
    done
    echo
    log_info "$service is available"
    return 0
}

check_dependencies() {
    log_info "Checking dependencies..."
    
    POSTGRES_HOST=${POSTGRES_HOST:-localhost}
    POSTGRES_PORT=${POSTGRES_PORT:-5432}
    REDIS_HOST=${REDIS_HOST:-localhost}
    REDIS_PORT=${REDIS_PORT:-6379}
    
    if command -v nc &> /dev/null; then
        wait_for_service $POSTGRES_HOST $POSTGRES_PORT "PostgreSQL" 10 || true
        wait_for_service $REDIS_HOST $REDIS_PORT "Redis" 10 || true
    else
        log_warn "nc (netcat) not found, skipping dependency checks"
    fi
}

start() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            log_warn "$APP_NAME is already running (PID: $PID)"
            exit 0
        else
            rm -f $PID_FILE
        fi
    fi
    
    check_java
    check_port
    check_dependencies
    
    if [ ! -f "target/$JAR_FILE" ]; then
        log_info "JAR file not found, building..."
        mvn clean package -DskipTests
    fi
    
    mkdir -p logs
    mkdir -p /data/images 2>/dev/null || true
    
    JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m}"
    
    log_info "Starting $APP_NAME..."
    log_info "JAVA_OPTS: $JAVA_OPTS"
    
    nohup java $JAVA_OPTS -jar target/$JAR_FILE > $LOG_FILE 2>&1 &
    echo $! > $PID_FILE
    
    sleep 3
    
    if ps -p $(cat $PID_FILE) > /dev/null 2>&1; then
        log_info "$APP_NAME started successfully (PID: $(cat $PID_FILE))"
        log_info "Logs: $LOG_FILE"
        log_info "Health check: http://localhost:${SERVER_PORT:-8080}/api/v1/admin/health"
    else
        log_error "Failed to start $APP_NAME. Check logs for details."
        cat $LOG_FILE
        exit 1
    fi
}

stop() {
    if [ ! -f "$PID_FILE" ]; then
        log_warn "$APP_NAME is not running"
        exit 0
    fi
    
    PID=$(cat $PID_FILE)
    
    if ! ps -p $PID > /dev/null 2>&1; then
        log_warn "$APP_NAME is not running"
        rm -f $PID_FILE
        exit 0
    fi
    
    log_info "Stopping $APP_NAME (PID: $PID)..."
    
    kill $PID
    
    for i in {1..30}; do
        if ! ps -p $PID > /dev/null 2>&1; then
            break
        fi
        sleep 1
    done
    
    if ps -p $PID > /dev/null 2>&1; then
        log_warn "Graceful shutdown failed, forcing..."
        kill -9 $PID
    fi
    
    rm -f $PID_FILE
    log_info "$APP_NAME stopped"
}

status() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            log_info "$APP_NAME is running (PID: $PID)"
            exit 0
        fi
    fi
    log_info "$APP_NAME is not running"
    exit 1
}

case "${1:-start}" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    status)
        status
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
