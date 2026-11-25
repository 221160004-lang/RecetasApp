package com.example.recetasapp.repository;

import com.example.recetasapp.network.ApiError;

public interface RepoCallback<T> {
    void onSuccess(T datos);

    void onError(ApiError error);
}