# INSTRUCCIONES DE EJECUCIÓN

## Pasos

### 1. Descargar los dos proyectos (EjemploQuarkus y OrdenQuarkus) y el script (Script db.sql)
### 2. Ejecutar script para crear las bases de datos y hacer clean and build ambos proyectos
### 3. Hacerle Run primero a EjemploQuarkus porque es el que contiene la parte de productos
### 4. Hacerle Run a OrdenQuarkus
### 5. API Gateway (NGINX) con gRPC / HTTP2
Los clientes consumen los servicios a través del gateway: **gRPC (HTTP/2) en el puerto 9090** y REST en el 8080
(`/api/productos`, `/api/ordenes`). El gateway enruta a los microservicios por HTTP/2 con `grpc_pass`.
Instrucciones, rutas y pruebas en [`api-gateway/README.md`](api-gateway/README.md).
