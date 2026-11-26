package com.example.recetasapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recetasapp.model.Receta;
import com.example.recetasapp.network.ApiError;
import com.example.recetasapp.repository.RecetaRepository;
import com.example.recetasapp.repository.RepoCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "receta_id";

    private final RecetaRepository repository = new RecetaRepository();

    private int recetaId;
    private View contenido, layoutError;
    private ProgressBar progress;
    private Button btnEliminar;
    private TextView tvError, tvNombre, tvCategoria, tvDatos, tvDescripcion, tvIngredientes, tvPreparacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle de receta");
        }

        recetaId = getIntent().getIntExtra(EXTRA_ID, 0);

        contenido = findViewById(R.id.scrollContenido);
        layoutError = findViewById(R.id.layoutError);
        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);
        tvNombre = findViewById(R.id.tvNombre);
        tvCategoria = findViewById(R.id.tvCategoria);
        tvDatos = findViewById(R.id.tvDatos);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvIngredientes = findViewById(R.id.tvIngredientes);
        tvPreparacion = findViewById(R.id.tvPreparacion);
        btnEliminar = findViewById(R.id.btnEliminar);

        findViewById(R.id.btnEditar).setOnClickListener(v -> {
            Intent intent = new Intent(this, FormularioActivity.class);
            intent.putExtra(FormularioActivity.EXTRA_ID, recetaId);
            startActivity(intent);
        });
        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        findViewById(R.id.btnReintentar).setOnClickListener(v -> cargarReceta());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se recarga al volver de editar
        cargarReceta();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void cargarReceta() {
        mostrarCargando();
        repository.obtener(recetaId, new RepoCallback<Receta>() {
            @Override
            public void onSuccess(Receta receta) {
                if (isFinishing() || receta == null) return;
                mostrarReceta(receta);
            }

            @Override
            public void onError(ApiError error) {
                if (isFinishing()) return;
                if (error.codigo == 404) {
                    Toast.makeText(DetalleActivity.this, error.mensaje, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    mostrarError(error.mensaje);
                }
            }
        });
    }

    private void confirmarEliminacion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar receta")
                .setMessage("¿Seguro que quieres eliminar esta receta? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialogo, cual) -> eliminar())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminar() {
        btnEliminar.setEnabled(false);
        repository.eliminar(recetaId, new RepoCallback<Void>() {
            @Override
            public void onSuccess(Void datos) {
                if (isFinishing()) return;
                Toast.makeText(DetalleActivity.this, "Receta eliminada", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(ApiError error) {
                if (isFinishing()) return;
                btnEliminar.setEnabled(true);
                Toast.makeText(DetalleActivity.this, error.mensaje, Toast.LENGTH_LONG).show();
                if (error.codigo == 404) {
                    finish();
                }
            }
        });
    }

    // ---------- Estados de la pantalla ----------

    private void mostrarCargando() {
        progress.setVisibility(View.VISIBLE);
        contenido.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void mostrarError(String mensaje) {
        progress.setVisibility(View.GONE);
        contenido.setVisibility(View.GONE);
        tvError.setText(mensaje);
        layoutError.setVisibility(View.VISIBLE);
    }

    private void mostrarReceta(Receta r) {
        progress.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        contenido.setVisibility(View.VISIBLE);
        btnEliminar.setEnabled(true);

        String fecha = (r.fechaCreacion != null && r.fechaCreacion.length() >= 10)
                ? r.fechaCreacion.substring(0, 10) : "";

        tvNombre.setText(r.nombre);
        tvCategoria.setText(r.categoria);
        tvDatos.setText(r.tiempoPreparacionMin + " min · " + r.porciones
                + " porciones · Creada el " + fecha);
        tvDescripcion.setText((r.descripcion == null || r.descripcion.isEmpty())
                ? "Sin descripción" : r.descripcion);
        tvIngredientes.setText(r.ingredientes);
        tvPreparacion.setText(r.preparacion);
    }
}