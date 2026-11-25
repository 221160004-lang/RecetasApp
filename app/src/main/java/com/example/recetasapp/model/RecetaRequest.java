package com.example.recetasapp.model;

// Cuerpo que se envía al crear y al editar
public class RecetaRequest {
    public String nombre;
    public String descripcion;
    public String ingredientes;
    public String preparacion;
    public int tiempoPreparacionMin;
    public int porciones;
    public int categoriaId;

    public RecetaRequest(String nombre, String descripcion, String ingredientes, String preparacion,
                         int tiempoPreparacionMin, int porciones, int categoriaId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ingredientes = ingredientes;
        this.preparacion = preparacion;
        this.tiempoPreparacionMin = tiempoPreparacionMin;
        this.porciones = porciones;
        this.categoriaId = categoriaId;
    }
}