#!/bin/bash

echo "Stopping all microservices on ports 8761, 8050, 8070, 8081, 8082, 8083..."
for port in 8761 8050 8070 8081 8082 8083; do
    pid=$(lsof -t -iTCP:$port -sTCP:LISTEN)
    if [ -n "$pid" ]; then
        echo "Killing process on port $port (PID: $pid)"
        kill $pid 2>/dev/null
    fi
done
echo "All microservices stopped."
