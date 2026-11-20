package com.mycompany.ejemploquarkus.servicios;

import com.mycompany.ejemploquarkus.entidades.Producto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 * Lógica de negocio con Panache, compartida por el servicio gRPC.
 */
@ApplicationScoped
public class ProductoServicio {

    @Transactional
    public List<Producto> listar() {
        return Producto.listAll();
    }

    @Transactional
    public Producto buscar(Long id) {
        return Producto.findById(id);
    }

    @Transactional
    public Producto crear(String nombre, double precio) {
        Producto producto = new Producto();
        producto.nombre = nombre;
        producto.precio = precio;
        producto.persist();
        return producto;
    }
}
