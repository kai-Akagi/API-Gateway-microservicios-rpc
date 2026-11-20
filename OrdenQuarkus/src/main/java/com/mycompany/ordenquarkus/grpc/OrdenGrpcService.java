package com.mycompany.ordenquarkus.grpc;

import com.mycompany.ordenquarkus.entidades.Orden;
import com.mycompany.ordenquarkus.servicios.OrdenServicio;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

/**
 * Servicio RPC (gRPC sobre HTTP/2) de órdenes.
 * Las clases OrdenService, ListaOrdenesReply, OrdenMensaje, etc.
 * se generan a partir de src/main/proto/orden.proto al compilar.
 */
@GrpcService
public class OrdenGrpcService implements OrdenService {

    @Inject
    OrdenServicio servicio;

    @Override
    @Blocking
    public Uni<ListaOrdenesReply> obtenerTodas(VacioRequest request) {
        ListaOrdenesReply.Builder respuesta = ListaOrdenesReply.newBuilder();
        for (Orden o : servicio.listar()) {
            respuesta.addOrdenes(aMensaje(o));
        }
        return Uni.createFrom().item(respuesta.build());
    }

    @Override
    @Blocking
    public Uni<OrdenMensaje> crear(NuevaOrdenRequest request) {
        if (request.getProductoId() <= 0 || request.getCantidad() <= 0) {
            return Uni.createFrom().failure(Status.INVALID_ARGUMENT
                    .withDescription("producto_id y cantidad deben ser mayores a 0")
                    .asRuntimeException());
        }
        try {
            Orden orden = servicio.crear(request.getProductoId(), request.getCantidad());
            return Uni.createFrom().item(aMensaje(orden));
        } catch (StatusRuntimeException e) {
            return Uni.createFrom().failure(e);
        }
    }

    private static OrdenMensaje aMensaje(Orden o) {
        return OrdenMensaje.newBuilder()
                .setId(o.id)
                .setProductoId(o.productoId)
                .setCantidad(o.cantidad)
                .setTotal(o.total)
                .build();
    }
}
