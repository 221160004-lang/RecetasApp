package com.example.recetasapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetasapp.model.Receta;

import java.util.ArrayList;
import java.util.List;

public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder> {

    public interface OnRecetaClick {
        void onClick(Receta receta);
    }

    private final List<Receta> recetas = new ArrayList<>();
    private final OnRecetaClick listener;

    public RecetaAdapter(OnRecetaClick listener) {
        this.listener = listener;
    }

    public void setRecetas(List<Receta> nuevas) {
        recetas.clear();
        recetas.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receta, parent, false);
        return new RecetaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        Receta r = recetas.get(position);
        holder.tvNombre.setText(r.nombre);
        holder.tvInfo.setText(r.categoria + " · " + r.tiempoPreparacionMin + " min · "
                + r.porciones + " porciones");

        boolean sinDescripcion = r.descripcion == null || r.descripcion.isEmpty();
        holder.tvDescripcion.setVisibility(sinDescripcion ? View.GONE : View.VISIBLE);
        holder.tvDescripcion.setText(r.descripcion);

        holder.itemView.setOnClickListener(v -> listener.onClick(r));
    }

    @Override
    public int getItemCount() {
        return recetas.size();
    }

    static class RecetaViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre;
        final TextView tvInfo;
        final TextView tvDescripcion;

        RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
        }
    }
}