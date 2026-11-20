#!/bin/sh
# Pruebas gRPC contra el API Gateway (puerto 9090, HTTP/2). Requiere grpcurl:
#   https://github.com/fullstorydev/grpcurl   (Windows: scoop install grpcurl / choco install grpcurl)
# Ejecutar desde la carpeta api-gateway.
G="grpcurl -plaintext -import-path protos"

echo "--- Crear producto"
$G -proto producto.proto -d '{"nombre":"Mouse","precio":250}' localhost:9090 productos.ProductoService/Crear
echo "--- Listar productos"
$G -proto producto.proto -d '{}' localhost:9090 productos.ProductoService/ObtenerTodos
echo "--- Producto por id"
$G -proto producto.proto -d '{"id":1}' localhost:9090 productos.ProductoService/ObtenerPorId
echo "--- Crear orden (Orden consulta el precio al servicio de productos)"
$G -proto orden.proto -d '{"producto_id":1,"cantidad":2}' localhost:9090 ordenes.OrdenService/Crear
echo "--- Listar ordenes"
$G -proto orden.proto -d '{}' localhost:9090 ordenes.OrdenService/ObtenerTodas
echo "--- Error esperado (producto inexistente -> NotFound)"
$G -proto producto.proto -d '{"id":9999}' localhost:9090 productos.ProductoService/ObtenerPorId
