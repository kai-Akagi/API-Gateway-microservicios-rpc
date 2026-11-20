package com.mycompany.ordenquarkus.servicios;

import com.mycompany.ordenquarkus.entidades.Orden;
import io.grpc.Status;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Lógica de negocio de órdenes, compartida por el servicio gRPC.
 * Consulta el precio al microservicio de productos igual que OrdenResource.
 */
@ApplicationScoped
public class OrdenServicio {

    @Inject
    @RestClient
    ProductoServiceClient productoService;

    @Transactional
    public List<Orden> listar() {
        return Orden.listAll();
    }

    @Transactional
    public Orden crear(Long productoId, int cantidad) {
        ProductoDTO producto;
        try {
            producto = productoService.obtenerProducto(productoId);
        } catch (WebApplicationException e) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("El producto " + productoId + " no existe")
                    .asRuntimeException();
        } catch (ProcessingException e) {
            throw Status.UNAVAILABLE
                    .withDescription("El servicio de productos no está disponible")
                    .asRuntimeException();
        }
        if (producto == null) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("El producto " + productoId + " no existe")
                    .asRuntimeException();
        }

        Orden orden = new Orden();
        orden.productoId = productoId;
        orden.cantidad = cantidad;
        orden.total = producto.precio * cantidad;
        orden.persist();
        return orden;
    }
}
