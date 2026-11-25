package com.example.recetasapp.repository;

import androidx.annotation.NonNull;

import com.example.recetasapp.model.Categoria;
import com.example.recetasapp.model.PagedResult;
import com.example.recetasapp.model.Receta;
import com.example.recetasapp.model.RecetaRequest;
import com.example.recetasapp.network.ApiError;
import com.example.recetasapp.network.ApiService;
import com.example.recetasapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecetaRepository {

    private final ApiService api = RetrofitClient.getApi();

    public void obtenerCategorias(RepoCallback<List<Categoria>> callback) {
        api.obtenerCategorias().enqueue(envolver(callback));
    }

    public void listar(String search, Integer categoriaId, int page, int pageSize,
                       RepoCallback<PagedResult<Receta>> callback) {
        api.listarRecetas(search, categoriaId, page, pageSize).enqueue(envolver(callback));
    }

    public void obtener(int id, RepoCallback<Receta> callback) {
        api.obtenerReceta(id).enqueue(envolver(callback));
    }

    public void crear(RecetaRequest cuerpo, RepoCallback<Receta> callback) {
        api.crearReceta(cuerpo).enqueue(envolver(callback));
    }

    public void actualizar(int id, RecetaRequest cuerpo, RepoCallback<Void> callback) {
        api.actualizarReceta(id, cuerpo).enqueue(envolver(callback));
    }

    public void eliminar(int id, RepoCallback<Void> callback) {
        api.eliminarReceta(id).enqueue(envolver(callback));
    }

    // Traduce la respuesta de Retrofit a éxito o ApiError (el callback llega en el hilo principal)
    private <T> Callback<T> envolver(RepoCallback<T> callback) {
        return new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ApiError.desdeRespuesta(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                callback.onError(ApiError.fallaDeConexion());
            }
        };
    }
}