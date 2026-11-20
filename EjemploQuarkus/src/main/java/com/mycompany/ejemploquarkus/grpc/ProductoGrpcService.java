package com.mycompany.ejemploquarkus.grpc;

import com.mycompany.ejemploquarkus.entidades.Producto;
import com.mycompany.ejemploquarkus.servicios.ProductoServicio;
import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

/**
 * Servicio RPC (gRPC sobre HTTP/2) de productos.
 * Las clases ProductoService, ListaProductosReply, ProductoMensaje, etc.
 * se generan a partir de src/main/proto/producto.proto al compilar.
 * Se usa @Blocking porque Panache/JDBC es código bloqueante.
 */
@GrpcService
public class ProductoGrpcService implements ProductoService {

    @Inject
    ProductoServicio servicio;

    @Override
    @Blocking
    public Uni<ListaProductosReply> obtenerTodos(VacioRequest request) {
        ListaProductosReply.Builder respuesta = ListaProductosReply.newBuilder();
        for (Producto p : servicio.listar()) {
            respuesta.addProductos(aMensaje(p));
        }
        return Uni.createFrom().item(respuesta.build());
    }

    @Override
    @Blocking
    public Uni<ProductoMensaje> obtenerPorId(ProductoIdRequest request) {
        Producto producto = servicio.buscar(request.getId());
        if (producto == null) {
            return Uni.createFrom().failure(Status.NOT_FOUND
                    .withDescription("Producto no encontrado: " + request.getId())
                    .asRuntimeException());
        }
        return Uni.createFrom().item(aMensaje(producto));
    }

    @Override
    @Blocking
    public Uni<ProductoMensaje> crear(NuevoProductoRequest request) {
        if (request.getNombre().isBlank()) {
            return Uni.createFrom().failure(Status.INVALID_ARGUMENT
                    .withDescription("El nombre es obligatorio")
                    .asRuntimeException());
        }
        Producto producto = servicio.crear(request.getNombre(), request.getPrecio());
        return Uni.createFrom().item(aMensaje(producto));
    }

    private static ProductoMensaje aMensaje(Producto p) {
        return ProductoMensaje.newBuilder()
                .setId(p.id)
                .setNombre(p.nombre == null ? "" : p.nombre)
                .setPrecio(p.precio)
                .build();
    }
}
