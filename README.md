# GraniShots — Microservicios

## Estructura del proyecto
```
granishots-microservicios/
├── build.gradle                  ← configuración raíz
├── settings.gradle               ← incluye los 5 módulos
├── gradlew.bat                   ← ejecutar en Windows
├── crear-bases-de-datos.sql      ← script para PostgreSQL
└── granishots/
    ├── granishots-catalog-service       → puerto 8081
    ├── granishots-inventory-service     → puerto 8082
    ├── granishots-orders-service        → puerto 8083
    ├── granishots-billing-service       → puerto 8084
    └── granishots-notifications-service → puerto 8085
```

## Paso 1 — Crear las bases de datos
Abre pgAdmin → clic derecho en postgres → Query Tool
Ejecuta el archivo: `crear-bases-de-datos.sql`

## Paso 2 — Abrir en IntelliJ
```
File → Open → selecciona la carpeta granishots-microservicios
             (donde está el settings.gradle raíz)
→ Open as Project → Trust Project
```
Espera que IntelliJ descargue todas las dependencias (2-3 min).

## Paso 3 — Ejecutar los servicios

### Opción A — Uno por uno (recomendado para empezar)
En el panel de Gradle (elefante 🐘 derecha):
```
granishots-catalog-service → Tasks → application → bootRun
```
Repite para cada servicio.

### Opción B — Todos a la vez con la configuración compuesta
En la barra superior → selector de configuraciones → 
"🚀 GraniShots — Todos los servicios" → ▶️

### Opción C — Terminal
```powershell
# Catalog (8081)
cd granishots\granishots-catalog-service
.\gradlew.bat bootRun

# Inventory (8082) — abre otra terminal
cd granishots\granishots-inventory-service
.\gradlew.bat bootRun
```

## Verificar que funcionan
```
GET http://localhost:8081/api/v1/products
GET http://localhost:8082/api/v1/supplies
GET http://localhost:8083/api/v1/orders
GET http://localhost:8084/api/v1/invoices
GET http://localhost:8085/api/v1/notifications
```
Todos deben responder con status 200 y data: []

## Puertos y bases de datos
| Servicio       | Puerto | Base de datos    |
|----------------|--------|------------------|
| catalog        | 8081   | catalog_db       |
| inventory      | 8082   | inventory_db     |
| orders         | 8083   | orders_db        |
| billing        | 8084   | billing_db       |
| notifications  | 8085   | notifications_db |

## Credenciales PostgreSQL (ajustar en application.yaml de cada servicio)
```yaml
username: postgres
password: TU_PASSWORD
```
