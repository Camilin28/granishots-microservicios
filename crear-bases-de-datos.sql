-- Ejecuta en pgAdmin → clic derecho postgres → Query Tool → F5

CREATE DATABASE catalog_db;
CREATE DATABASE inventory_db;
CREATE DATABASE orders_db;
CREATE DATABASE notifications_db;
CREATE DATABASE login_db;

-- Verificar
SELECT datname FROM pg_database 
WHERE datname IN ('catalog_db','inventory_db','orders_db','notifications_db','login_db')
ORDER BY datname;
