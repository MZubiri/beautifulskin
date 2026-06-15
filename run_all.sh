#!/bin/bash

# Kill any existing processes running on our ports
echo "Cleaning up ports 8761, 8050, 8070, 8081, 8082, 8083..."
for port in 8761 8050 8070 8081 8082 8083; do
    pid=$(lsof -t -iTCP:$port -sTCP:LISTEN)
    if [ -n "$pid" ]; then
        echo "Killing process on port $port (PID: $pid)"
        kill $pid 2>/dev/null
    fi
done

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Compiling current source code..."
for service in eureka-service login-service productos-service kardex-service proveedores-service api-gateway; do
    echo " - Building $service"
    (cd "$BASE_DIR/$service" && ./mvnw clean package -q -DskipTests) || exit 1
done

echo "1. Starting Eureka Server on port 8761..."
cd "$BASE_DIR/eureka-service" && nohup java -jar target/eureka-service-0.0.1-SNAPSHOT.jar > "$BASE_DIR/eureka-service.log" 2>&1 &
sleep 10

echo "2. Starting Login Service (Auth) on port 8070..."
cd "$BASE_DIR/login-service" && nohup java -jar target/login-service-0.0.1-SNAPSHOT.jar > "$BASE_DIR/login-service.log" 2>&1 &

echo "3. Starting Productos Service (Business) on port 8081..."
cd "$BASE_DIR/productos-service" && nohup java -jar target/Productos-Rest-0.0.1-SNAPSHOT.jar > "$BASE_DIR/productos-service.log" 2>&1 &

echo "4. Starting Kardex Service on port 8082..."
cd "$BASE_DIR/kardex-service" && nohup java -jar target/kardex-service-0.0.1-SNAPSHOT.jar > "$BASE_DIR/kardex-service.log" 2>&1 &

echo "5. Starting Proveedores Service on port 8083..."
cd "$BASE_DIR/proveedores-service" && nohup java -jar target/proveedores-service-0.0.1-SNAPSHOT.jar > "$BASE_DIR/proveedores-service.log" 2>&1 &
sleep 5

echo "6. Starting API Gateway on port 8050..."
cd "$BASE_DIR/api-gateway" && nohup java -jar target/Api_Gateway-0.0.1-SNAPSHOT.jar > "$BASE_DIR/api-gateway.log" 2>&1 &

echo "--------------------------------------------------"
echo "All microservices are starting up from packaged JAR files!"
echo "Logs are available at:"
echo " - $BASE_DIR/eureka-service.log"
echo " - $BASE_DIR/login-service.log"
echo " - $BASE_DIR/productos-service.log"
echo " - $BASE_DIR/kardex-service.log"
echo " - $BASE_DIR/proveedores-service.log"
echo " - $BASE_DIR/api-gateway.log"
echo ""
echo "Verify registration in: http://localhost:8761"
echo "To stop all services, run: ./stop_all.sh"
echo "--------------------------------------------------"

# Keep the shell script alive so the background processes aren't reaped
tail -f /dev/null
