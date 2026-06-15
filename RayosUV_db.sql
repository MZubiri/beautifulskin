-- IMPORTANTE PARA DBEAVER:
-- Ejecute TODO este archivo con "Ejecutar script SQL" (Alt+X).
--    No use "Ejecutar sentencia SQL" (Ctrl+Enter) para todo el archivo.
-- ADVERTENCIA: cada ejecución borra y reconstruye RayosUV_db completamente.

DROP DATABASE IF EXISTS RayosUV_db;

CREATE DATABASE RayosUV_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS RayosUV_db.categoria (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS RayosUV_db.authusuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_user VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'ROLE_TRABAJADOR'
);

CREATE TABLE IF NOT EXISTS RayosUV_db.producto (
    id_producto BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200),
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    id_categoria BIGINT NOT NULL,
    stock_minimo INT NOT NULL DEFAULT 5,
    codigo_barras VARCHAR(50) UNIQUE,
    imagen_url VARCHAR(255),
    FOREIGN KEY (id_categoria) REFERENCES RayosUV_db.categoria(id_categoria)
);

CREATE TABLE IF NOT EXISTS RayosUV_db.proveedor (
    id_proveedor BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ruc VARCHAR(11) NOT NULL UNIQUE,
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS RayosUV_db.orden_compra (
    id_orden BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_recepcion DATETIME,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (id_proveedor) REFERENCES RayosUV_db.proveedor(id_proveedor)
);

CREATE TABLE IF NOT EXISTS RayosUV_db.detalle_orden_compra (
    id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_orden BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_orden) REFERENCES RayosUV_db.orden_compra(id_orden),
    FOREIGN KEY (id_producto) REFERENCES RayosUV_db.producto(id_producto)
);

CREATE TABLE IF NOT EXISTS RayosUV_db.kardex (
    id_kardex BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_producto BIGINT NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    stock_anterior INT NOT NULL,
    stock_posterior INT NOT NULL,
    justificacion VARCHAR(255) NOT NULL,
    id_usuario INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_producto) REFERENCES RayosUV_db.producto(id_producto),
    FOREIGN KEY (id_usuario) REFERENCES RayosUV_db.authusuario(id)
);

DROP TABLE IF EXISTS RayosUV_db.refresh_token;

INSERT INTO RayosUV_db.categoria (id_categoria, nombre) VALUES
(1, 'Niños'), (2, 'Niñas'), (3, 'Mujer'), (4, 'Hombre'), (5, 'Accesorios')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- Credenciales demo: Admin/admin y trabajador1/trabajador
INSERT INTO RayosUV_db.authusuario (username, password_user, rol) VALUES
('Admin', '$2b$10$jXHHbaFKyC8JYHZpnn927usJ8AGncBMoaMgdTQqUH.OXiNhmMZJA2', 'ROLE_ADMIN'),
('trabajador1', '$2b$10$JqZY8IVivqZh/n62wOEBOebsEqYyTFObNmqQsaQmwk9OBp6jSXTK.', 'ROLE_TRABAJADOR')
ON DUPLICATE KEY UPDATE password_user = VALUES(password_user), rol = VALUES(rol);

INSERT INTO RayosUV_db.producto
(id_producto, nombre, descripcion, precio, stock, id_categoria, stock_minimo, codigo_barras, imagen_url) VALUES
(1, 'Protector Solar FPS 50+ Toque Seco', 'Protector solar facial de toque seco. 50ml.', 65.90, 12, 3, 20, '7750123456789', NULL),
(2, 'Bloqueador Infantil SPF 50+', 'Protección para piel sensible. 120ml.', 45.00, 3, 1, 10, '7750123456790', NULL),
(3, 'Gafas de Sol Deportivas UV400', 'Gafas polarizadas para exteriores.', 89.90, 45, 5, 15, '7750123456791', NULL),
(4, 'Gel Refrescante Post Solar', 'Gel calmante con aloe vera. 250ml.', 39.90, 0, 3, 15, '7750123456792', NULL),
(5, 'Loción Solar en Spray FPS 50', 'Loción corporal resistente al agua. 200ml.', 72.50, 4, 4, 12, '7750123456793', NULL),
(6, 'Sombrero de Ala Ancha UV', 'Sombrero liviano con protección UV.', 55.00, 22, 5, 10, '7750123456794', NULL),
(7, 'Protector Labial FPS 30', 'Bálsamo hidratante con protección solar.', 18.90, 8, 3, 12, '7750123456795', NULL),
(8, 'Camiseta Deportiva UPF 50+', 'Camiseta de manga larga para exteriores.', 79.90, 35, 4, 10, '7750123456796', NULL),
(9, 'Medidor Portátil de Radiación UV', 'Indicador digital de intensidad UV.', 125.00, 5, 5, 5, '7750123456797', NULL),
(10, 'Sombrilla con Protección UV', 'Sombrilla compacta con recubrimiento UV.', 69.90, 60, 5, 20, '7750123456798', NULL)
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), precio = VALUES(precio), stock_minimo = VALUES(stock_minimo);

INSERT INTO RayosUV_db.proveedor (id_proveedor, nombre, ruc, direccion, telefono, email) VALUES
(1, 'Dermatología Cosmética del Perú S.A.C.', '20551234567', 'Av. Javier Prado Este 1234', '01-4445566', 'pedidos@dermacosmetica.pe'),
(2, 'Distribuidora Solar y Accesorios S.A.', '20109876543', 'Jr. Carabaya 567', '01-3332211', 'ventas@solar.pe'),
(3, 'Laboratorios Protección Total S.A.C.', '20604567891', 'Av. Arequipa 2450', '01-5557788', 'compras@protecciontotal.pe'),
(4, 'Importaciones Vida al Aire Libre E.I.R.L.', '20405678912', 'Av. Argentina 850', '01-6223344', 'pedidos@vidaairelibre.pe')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre), email = VALUES(email);

-- Órdenes recibidas permiten revisar el historial; las pendientes pueden recibirse desde Angular.
INSERT INTO RayosUV_db.orden_compra
(id_orden, id_proveedor, fecha_creacion, fecha_recepcion, estado, total) VALUES
(1, 1, '2026-05-05 09:00:00', '2026-05-06 11:30:00', 'RECIBIDA', 1800.00),
(2, 2, '2026-06-10 10:15:00', NULL, 'PENDIENTE', 1000.00),
(3, 3, '2026-05-20 14:00:00', '2026-05-21 09:20:00', 'RECIBIDA', 3360.00),
(4, 4, '2026-06-12 16:30:00', NULL, 'PENDIENTE', 1000.00);

INSERT INTO RayosUV_db.detalle_orden_compra
(id_detalle, id_orden, id_producto, cantidad, precio_unitario) VALUES
(1, 1, 1, 30, 45.00),
(2, 1, 2, 15, 30.00),
(3, 2, 3, 10, 60.00),
(4, 2, 6, 20, 20.00),
(5, 3, 5, 80, 40.00),
(6, 3, 7, 20, 8.00),
(7, 4, 8, 10, 40.00),
(8, 4, 9, 10, 30.00),
(9, 4, 10, 25, 12.00);

-- Historial coherente con el stock actual de cada producto.
INSERT INTO RayosUV_db.kardex
(id_kardex, id_producto, tipo_movimiento, cantidad, stock_anterior, stock_posterior, justificacion, id_usuario, fecha) VALUES
(1, 1, 'ENTRADA', 30, 0, 30, 'Recepción de orden de compra #1', 1, '2026-05-06 11:30:00'),
(2, 1, 'SALIDA', 18, 30, 12, 'Ventas de mayo', 2, '2026-05-31 18:00:00'),
(3, 2, 'ENTRADA', 15, 0, 15, 'Recepción de orden de compra #1', 1, '2026-05-06 11:31:00'),
(4, 2, 'SALIDA', 12, 15, 3, 'Ventas de mayo', 2, '2026-05-31 18:05:00'),
(5, 3, 'ENTRADA', 45, 0, 45, 'Inventario inicial', 1, '2026-05-01 08:00:00'),
(6, 4, 'ENTRADA', 20, 0, 20, 'Inventario inicial', 1, '2026-05-01 08:05:00'),
(7, 4, 'SALIDA', 20, 20, 0, 'Ventas de campaña', 2, '2026-06-08 17:00:00'),
(8, 5, 'ENTRADA', 80, 0, 80, 'Recepción de orden de compra #3', 1, '2026-05-21 09:20:00'),
(9, 5, 'SALIDA', 76, 80, 4, 'Ventas de campaña', 2, '2026-06-09 17:00:00'),
(10, 6, 'ENTRADA', 25, 0, 25, 'Inventario inicial', 1, '2026-05-01 08:10:00'),
(11, 6, 'SALIDA', 3, 25, 22, 'Venta mostrador', 2, '2026-06-02 12:00:00'),
(12, 7, 'ENTRADA', 20, 0, 20, 'Recepción de orden de compra #3', 1, '2026-05-21 09:21:00'),
(13, 7, 'SALIDA', 12, 20, 8, 'Ventas de mayo', 2, '2026-05-31 18:10:00'),
(14, 8, 'ENTRADA', 35, 0, 35, 'Inventario inicial', 1, '2026-05-01 08:15:00'),
(15, 9, 'ENTRADA', 7, 0, 7, 'Inventario inicial', 1, '2026-05-01 08:20:00'),
(16, 9, 'AJUSTE', 5, 7, 5, 'Conteo físico de almacén', 1, '2026-06-01 09:00:00'),
(17, 10, 'ENTRADA', 60, 0, 60, 'Inventario inicial', 1, '2026-05-01 08:25:00');

-- Resumen esperado tras ejecutar el script:
-- 10 productos, 4 proveedores, 4 órdenes, 9 detalles y 17 movimientos de kárdex.
