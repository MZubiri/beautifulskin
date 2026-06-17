CREATE DATABASE IF NOT EXISTS RayosUV_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS RayosUV_db.authusuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_user VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'ROLE_TRABAJADOR'
);

CREATE TABLE IF NOT EXISTS RayosUV_db.categoria (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO RayosUV_db.categoria (id_categoria, nombre) VALUES
(1, 'Niños'), (2, 'Niñas'), (3, 'Mujer'), (4, 'Hombre'), (5, 'Accesorios'), (6, 'Unisex')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

-- Repara las credenciales de demostración sin borrar otros usuarios ni datos.
INSERT INTO RayosUV_db.authusuario (username, password_user, rol) VALUES
('Admin', '$2b$10$jXHHbaFKyC8JYHZpnn927usJ8AGncBMoaMgdTQqUH.OXiNhmMZJA2', 'ROLE_ADMIN'),
('trabajador1', '$2b$10$JqZY8IVivqZh/n62wOEBOebsEqYyTFObNmqQsaQmwk9OBp6jSXTK.', 'ROLE_TRABAJADOR')
ON DUPLICATE KEY UPDATE
password_user = VALUES(password_user),
rol = VALUES(rol);
