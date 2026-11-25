package com.example.recetasapp.network;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

// Convierte cualquier fallo en un mensaje claro para el usuario
public class ApiError {

    public final int codigo;          // código HTTP; 0 si no hubo conexión
    public final String mensaje;
    public final boolean sinConexion;

    private ApiError(int codigo, String mensaje, boolean sinConexion) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.sinConexion = sinConexion;
    }

    public static ApiError fallaDeConexion() {
        return new ApiError(0,
                "No se pudo conectar con el servidor. Revisa tu conexión y que la API esté encendida.",
                true);
    }

    public static ApiError desdeRespuesta(Response<?> respuesta) {
        int codigo = respuesta.code();
        Problema problema = leerProblema(respuesta);
        String detalle = (problema != null && problema.detail != null && !problema.detail.isEmpty())
                ? problema.detail : null;

        String mensaje;
        switch (codigo) {
            case 400:
                if (problema != null && problema.errors != null && !problema.errors.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Hay datos inválidos:");
                    for (List<String> lista : problema.errors.values()) {
                        for (String m : lista) {
                            sb.append("\n• ").append(m);
                        }
                    }
                    mensaje = sb.toString();
                } else {
                    mensaje = detalle != null ? detalle : "La solicitud no es válida (400).";
                }
                break;
            case 404:
                mensaje = detalle != null ? detalle : "No se encontró el registro (404).";
                break;
            case 409:
                mensaje = detalle != null ? detalle : "Ya existe un registro con esos datos (409).";
                break;
            default:
                mensaje = codigo >= 500
                        ? "El servidor tuvo un problema. Intenta de nuevo más tarde."
                        : "Ocurrió un error inesperado (código " + codigo + ").";
        }
        return new ApiError(codigo, mensaje, false);
    }

    private static Problema leerProblema(Response<?> respuesta) {
        try {
            if (respuesta.errorBody() == null) return null;
            return new Gson().fromJson(respuesta.errorBody().string(), Problema.class);
        } catch (IOException | JsonParseException e) {
            return null;
        }
    }

    // Forma del ProblemDetails que devuelve la API
    private static class Problema {
        String title;
        String detail;
        Map<String, List<String>> errors;
    }
}