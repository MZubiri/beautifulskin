# Beautiful Skin - Proyecto DAW II

Aplicación Angular con microservicios Spring Boot, autenticación JWT, Eureka,
API Gateway, CRUD de productos, kárdex y proveedores.

## Requisitos

- Java 21
- MySQL 8
- Node.js y npm

## Preparar la base de datos

En DBeaver:

1. Abrir `RayosUV_db.sql`.
2. Usar **Ejecutar script SQL** (`Alt+X`) para ejecutar el archivo completo.

No ejecutar todo el contenido con **Ejecutar sentencia SQL** (`Ctrl+Enter`),
porque el driver intentará enviar varias sentencias como una sola consulta.

El script principal es repetible y crea usuarios demo:

- Administrador: `Admin` / `admin`
- Trabajador: `trabajador1` / `trabajador`

## Ejecutar

1. Compilar los servicios:

```bash
for service in eureka-service login-service productos-service kardex-service proveedores-service api-gateway; do
  (cd "$service" && ./mvnw clean package)
done
```

2. Iniciar Eureka y los microservicios:

```bash
./run_all.sh
```

`run_all.sh` compila siempre el código actual antes de iniciar los servicios,
evitando ejecutar JAR antiguos.

3. Iniciar Angular en otra terminal:

```bash
cd frontend
npm install
npm start
```

4. Abrir `http://localhost:4200`.

## Desplegar con Coolify

El repositorio incluye `docker-compose.yml` para desplegar toda la aplicación
en un único recurso Docker Compose. La ejecución local descrita arriba continúa
funcionando sin Docker.

1. Crear en Coolify un recurso desde este repositorio y seleccionar Docker Compose.
2. Configurar `MYSQL_ROOT_PASSWORD` y `JWT_SECRET` con valores largos y privados.
3. Asignar el dominio público al servicio `frontend`, puerto `80`.
4. Desplegar. MySQL cargará `RayosUV_db.sql` únicamente al crear por primera vez
   el volumen `mysql-data`.

Los volúmenes `mysql-data` y `product-images` conservan la base de datos y las
imágenes entre despliegues. Para reconstruir los datos demo hay que eliminar
deliberadamente el volumen `mysql-data` antes de desplegar otra vez.

En Oracle Cloud, la instancia debe cumplir los requisitos de Coolify, tener
Docker disponible y permitir conexiones SSH, HTTP y HTTPS en sus reglas de red.

## Verificación

```bash
for service in login-service productos-service kardex-service proveedores-service api-gateway; do
  (cd "$service" && ./mvnw test)
done
(cd frontend && npm run build)
```

Importar `BeautifulSkin-DAWII.postman_collection.json` para probar el flujo por
el Gateway (`http://localhost:8050`). Primero ejecutar Login; la colección
guarda automáticamente el JWT utilizado por los demás endpoints.

## Flujo principal de demostración

1. Iniciar sesión.
2. Listar, crear, editar y eliminar un producto.
3. Subir una imagen o tomar una foto al crear o editar un producto.
4. Comprobar que editar el producto no cambia su stock.
5. Registrar un movimiento de kárdex.
6. Crear y recibir una orden de compra.

El stock inicial se guarda al crear el producto. Los cambios posteriores se
realizan desde kárdex. La comunicación distribuida es síncrona y no implementa
atomicidad entre microservicios.

Las imágenes de productos se guardan localmente en
`productos-service/uploads/productos`. Se aceptan JPG, PNG, WEBP y GIF de hasta
5 MB. La carpeta se crea automáticamente al iniciar el servicio.

## Preparar ZIP

```bash
./package_delivery.sh
```

El ZIP excluye `node_modules`, `target`, `dist`, logs, cachés y carpetas `bin`.
