# Beautiful Skin - Proyecto DAW II

Sistema web de inventario para productos de cuidado de la piel. La aplicación
usa Angular, Spring Boot, JWT, BCrypt, Eureka, API Gateway, MySQL, kárdex,
proveedores y órdenes de compra.

## Arquitectura

El frontend Angular consume una sola entrada: el API Gateway.

```text
Angular -> API Gateway -> microservicios -> MySQL
```

Microservicios incluidos:

- `eureka-service`: registro de servicios.
- `api-gateway`: entrada central para Angular.
- `login-service`: login, BCrypt y generación de JWT.
- `productos-service`: catálogo, CRUD de productos, stock e imágenes.
- `kardex-service`: movimientos de inventario y actualización de stock.
- `proveedores-service`: proveedores y órdenes de compra.

Flujo principal de inventario:

```text
proveedores-service -> kardex-service -> productos-service
```

Cuando se recibe una orden de compra, proveedores solicita a kárdex registrar
entradas. Kárdex consulta y actualiza el stock usando productos.

## Requisitos

- Java 21.
- MySQL 8.
- Node.js 20 o superior.
- npm.
- Puertos libres: `8761`, `8050`, `8070`, `8081`, `8082`, `8083`, `4200`.

## Base de Datos Local

1. Abrir MySQL, DBeaver o MySQL Workbench.
2. Ejecutar el archivo completo:

```text
RayosUV_db.sql
```

En DBeaver usar **Ejecutar script SQL** (`Alt+X`). No usar `Ctrl+Enter` para
todo el archivo, porque puede enviar varias sentencias como una sola consulta.

El script crea la base:

```text
RayosUV_db
```

Credenciales demo:

```text
Administrador: Admin / admin
Trabajador:    trabajador1 / trabajador
```

Si se desea reiniciar completamente la demo local, borrar la base y volver a
ejecutar `RayosUV_db.sql`.

## Configuración de MySQL

Los servicios locales están configurados para:

```text
Usuario: root
Clave:   root123
Base:    RayosUV_db
```

Si tu MySQL tiene otra clave, cambiar `spring.datasource.password` en:

- `login-service/src/main/resources/application.yml`
- `productos-service/src/main/resources/application.yaml`
- `kardex-service/src/main/resources/application.yaml`
- `proveedores-service/src/main/resources/application.yaml`

## Ejecución Local

Desde la carpeta raíz del proyecto:

```bash
./run_all.sh
```

Ese script:

1. Libera los puertos usados por los servicios.
2. Compila los microservicios.
3. Inicia Eureka.
4. Inicia login, productos, kárdex y proveedores.
5. Inicia API Gateway.

Los logs quedan en la raíz:

```text
eureka-service.log
login-service.log
productos-service.log
kardex-service.log
proveedores-service.log
api-gateway.log
```

Para detener los microservicios:

```bash
./stop_all.sh
```

## Ejecutar Angular

En otra terminal:

```bash
cd frontend
npm install
npm start
```

Abrir:

```text
http://localhost:4200
```

El frontend llamará al Gateway en:

```text
http://localhost:8050
```

## Puertos

```text
Eureka:        http://localhost:8761
API Gateway:   http://localhost:8050
Login:         http://localhost:8070
Productos:     http://localhost:8081
Kárdex:        http://localhost:8082
Proveedores:   http://localhost:8083
Angular:       http://localhost:4200
```

## Funcionalidades Principales

- Login con BCrypt y JWT.
- Protección de endpoints con `Authorization: Bearer`.
- CRUD de productos.
- Subida de imagen o captura con cámara para productos.
- Búsqueda por código de barras.
- Importación y exportación Excel.
- Kárdex de entradas, salidas y ajustes.
- Validación de stock insuficiente.
- Proveedores.
- Órdenes de compra.
- Recepción de órdenes mediante kárdex.
- Reportes de stock y valorización.
- Modo claro / oscuro.

## Flujo Sugerido Para Demostrar

1. Iniciar sesión con `Admin / admin`.
2. Crear un producto.
3. Editar el producto y comprobar que el stock no cambia desde edición.
4. Registrar un movimiento en kárdex.
5. Crear un proveedor.
6. Crear una orden de compra.
7. Recibir la orden.
8. Ver que kárdex registró entradas y productos actualizó stock.
9. Exportar Excel.

## Pruebas

Ejecutar pruebas unitarias:

```bash
for service in login-service productos-service kardex-service proveedores-service api-gateway; do
  (cd "$service" && ./mvnw test)
done
```

Compilar Angular:

```bash
cd frontend
npm run build
```

## Postman

Importar:

```text
BeautifulSkin-DAWII.postman_collection.json
```

Usar el Gateway:

```text
http://localhost:8050
```

Primero ejecutar Login. La colección guarda el JWT y lo reutiliza en los demás
endpoints.

## Docker / Coolify

El proyecto incluye `docker-compose.yml` para despliegue con Docker Compose o
Coolify. Para entorno local de clase se recomienda usar los pasos anteriores
con MySQL local, `run_all.sh` y Angular con `npm start`.

Variables requeridas para Docker/Coolify:

```text
MYSQL_ROOT_PASSWORD
JWT_SECRET
```

## Generar ZIP de Entrega

Desde la raíz:

```bash
./package_delivery.sh
```

Se generará:

```text
BeautifulSkin_DAWII_entrega.zip
```

El ZIP excluye:

- `node_modules`
- `target`
- `dist`
- `.git`
- `.idea`
- logs
- cachés
- carpetas `bin`
- imágenes subidas localmente

Ese ZIP está pensado para ser abierto en otra máquina, ejecutar el SQL, levantar
los servicios y correr Angular en local.
