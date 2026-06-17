CREATE DATABASE IF NOT EXISTS RayosUV_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS RayosUV_db.categoria (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO RayosUV_db.categoria (id_categoria, nombre) VALUES
(1, 'Niños'), (2, 'Niñas'), (3, 'Mujer'), (4, 'Hombre'), (5, 'Accesorios'), (6, 'Unisex')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

SET @producto_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = 'RayosUV_db'
      AND table_name = 'producto'
);

SET @activo_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'RayosUV_db'
      AND table_name = 'producto'
      AND column_name = 'activo'
);

SET @add_activo_sql = IF(
    @producto_table_exists = 1 AND @activo_column_exists = 0,
    'ALTER TABLE RayosUV_db.producto ADD COLUMN activo BOOLEAN DEFAULT TRUE',
    'SELECT 1'
);
PREPARE add_activo_stmt FROM @add_activo_sql;
EXECUTE add_activo_stmt;
DEALLOCATE PREPARE add_activo_stmt;

SET @repair_activo_sql = IF(
    @producto_table_exists = 1,
    'UPDATE RayosUV_db.producto SET activo = TRUE WHERE activo IS NULL',
    'SELECT 1'
);
PREPARE repair_activo_stmt FROM @repair_activo_sql;
EXECUTE repair_activo_stmt;
DEALLOCATE PREPARE repair_activo_stmt;
