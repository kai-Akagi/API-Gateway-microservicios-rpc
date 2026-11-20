# API Gateway con NGINX: gRPC (HTTP/2) + REST

El cliente envía las peticiones al gateway y NGINX las enruta al microservicio correspondiente.
Para bajar la latencia, el tramo gateway → microservicio usa **gRPC sobre HTTP/2** (`grpc_pass`):
una conexión persistente y multiplexada, con mensajes binarios (Protocol Buffers) en lugar de JSON.

```
                       ┌─────────────────────────────────┐
 Cliente gRPC ──HTTP/2─► :9090                           │ ──HTTP/2 (gRPC)──► EjemploQuarkus :9081  productos.ProductoService
                       │        NGINX (API Gateway)      │ ──HTTP/2 (gRPC)──► OrdenQuarkus   :9082  ordenes.OrdenService
 Cliente REST ─HTTP/1.1► :8080                           │ ──HTTP/1.1 (REST)► :8081 /productos   :8082 /ordenes
                       └─────────────────────────────────┘
```

| Puerto del gateway | Protocolo | Ruta | Destino |
|---|---|---|---|
| **9090** | HTTP/2 (gRPC) | `/productos.ProductoService/*` | `127.0.0.1:9081` |
| **9090** | HTTP/2 (gRPC) | `/ordenes.OrdenService/*` | `127.0.0.1:9082` |
| 8080 | HTTP/1.1 (REST) | `/api/productos[/id]` | `127.0.0.1:8081/productos` |
| 8080 | HTTP/1.1 (REST) | `/api/ordenes` | `127.0.0.1:8082/ordenes` |

> **Por qué dos puertos:** NGINX no puede hablar HTTP/2 con el backend usando `proxy_pass` (solo HTTP/1.1);
> HTTP/2 hacia los servicios se logra con `grpc_pass`, que exige que el cliente también hable gRPC.
> Además, un puerto en texto plano con `http2` solo acepta HTTP/2, por eso el REST (HTTP/1.1) queda en el 8080.
> NGINX no convierte JSON/REST a gRPC: una petición REST sigue llegando al microservicio por HTTP/1.1.

## Qué cambió en los microservicios
- `pom.xml`: dependencia `quarkus-grpc`.
- `src/main/proto/producto.proto` y `orden.proto`: definición de los servicios RPC (Quarkus genera las clases al compilar).
- `ProductoGrpcService` y `OrdenGrpcService` (`@GrpcService`, `@Blocking`) con la misma lógica Panache que el REST,
  apoyadas en `ProductoServicio` y `OrdenServicio`. El REST existente **no se tocó**.
- `application.properties`: servidor gRPC en `9081` (Productos) y `9082` (Órdenes), con reflexión habilitada.

## Cómo ejecutarlo
1. MySQL con `Script db.sql`, luego levanta **EjemploQuarkus** y **OrdenQuarkus** (la primera compilación descarga `protoc` automáticamente; requiere internet).
   Al arrancar, cada servicio debe mostrar el servidor gRPC en 9081 / 9082.
2. Gateway, desde esta carpeta (`api-gateway`):
   - **NGINX instalado:** `nginx -p . -c conf/nginx.conf` (detener: `-s stop`; validar: `-t`).
   - **Docker:** `docker compose up -d` (usa `conf/nginx.docker.conf`, que apunta a `host.docker.internal`).

## Probar
**gRPC (HTTP/2, puerto 9090)** con [grpcurl](https://github.com/fullstorydev/grpcurl) y los `.proto` de `protos/`:
```
grpcurl -plaintext -import-path protos -proto producto.proto -d '{"nombre":"Mouse","precio":250}' localhost:9090 productos.ProductoService/Crear
grpcurl -plaintext -import-path protos -proto producto.proto -d '{}' localhost:9090 productos.ProductoService/ObtenerTodos
grpcurl -plaintext -import-path protos -proto orden.proto -d '{"producto_id":1,"cantidad":2}' localhost:9090 ordenes.OrdenService/Crear
```
Script completo: `pruebas-grpc.sh`. También sirve Postman (New → gRPC, importar el `.proto`, servidor `localhost:9090`, sin TLS).

**REST (HTTP/1.1, puerto 8080)**, igual que antes: `pruebas.http` o
`curl http://localhost:8080/api/productos`.

En `logs/access.log` cada línea muestra el protocolo del cliente y el microservicio destino, por ejemplo:
`"POST /ordenes.OrdenService/Crear HTTP/2.0" HTTP/2.0 200 -> 127.0.0.1:9082`.

Si un microservicio está caído: gRPC responde `Unavailable` y REST responde `503` en JSON; el otro servicio sigue funcionando.

## Nota
`proto` y servicio: si cambias un `.proto` en un microservicio, copia el cambio a `protos/` para los clientes.
En un despliegue real solo 8080/9090 del gateway deberían ser accesibles desde fuera.
