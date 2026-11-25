package com.example.recetasapp.network;

import com.example.recetasapp.model.Categoria;
import com.example.recetasapp.model.PagedResult;
import com.example.recetasapp.model.Receta;
import com.example.recetasapp.model.RecetaRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Catálogo
    @GET("api/categorias")
    Call<List<Categoria>> obtenerCategorias();

    // Lista con búsqueda, filtro y paginación (los parámetros null no se envían)
    @GET("api/recetas")
    Call<PagedResult<Receta>> listarRecetas(
            @Query("search") String search,
            @Query("categoriaId") Integer categoriaId,
            @Query("page") int page,
            @Query("pageSize") int pageSize);

    @GET("api/recetas/{id}")
    Call<Receta> obtenerReceta(@Path("id") int id);

    @POST("api/recetas")
    Call<Receta> crearReceta(@Body RecetaRequest cuerpo);

    @PUT("api/recetas/{id}")
    Call<Void> actualizarReceta(@Path("id") int id, @Body RecetaRequest cuerpo);

    @DELETE("api/recetas/{id}")
    Call<Void> eliminarReceta(@Path("id") int id);
}