-- ===========================================================
--  VELMORE - Datos iniciales de productos
--  Spring Boot ejecuta este archivo automáticamente al arrancar
--  si spring.jpa.hibernate.ddl-auto=update (tabla ya existe)
--  y spring.sql.init.mode=always
-- ===========================================================

-- Evita duplicados si el servidor se reinicia
INSERT IGNORE INTO productos (id, nombre, marca, categoria, precio, descripcion, imagen, stock, activo) VALUES
(1,  'Khamrah',              'Lattafa',      'UNISEX',    169, 'Fragancia dulce, cálida y especiada.',           'khamrah', 12, true),
(2,  'Khamrah Qahwa',        'Lattafa',      'UNISEX',    175, 'Versión intensa con toque de café.',             'qahwa',    8, true),
(3,  '9PM',                  'Afnan',        'MASCULINO', 159, 'Aroma nocturno, juvenil e intenso.',             '9pm',     10, true),
(4,  'Sublime',              'Lattafa',      'FEMENINO',  149, 'Fragancia frutal y floral.',                     'sublime',  9, true),
(5,  'Club de Nuit Intense', 'Armaf',        'MASCULINO', 175, 'Fragancia cítrica y amaderada.',                 'club',     6, true),
(6,  'Yara Candy',           'Lattafa',      'FEMENINO',  155, 'Aroma dulce, moderno y juvenil.',                'yara',     7, true),
(7,  'Amber Oud Gold',       'Al Haramain',  'UNISEX',    210, 'Fragancia frutal, dulce y potente.',             'amber',    5, true),
(8,  'Asad Bourbon',         'Lattafa',      'MASCULINO', 165, 'Aroma especiado y cálido.',                      'asad',     8, true);
