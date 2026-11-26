package com.example.recetasapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recetasapp.model.Categoria;
import com.example.recetasapp.model.Receta;
import com.example.recetasapp.model.RecetaRequest;
import com.example.recetasapp.network.ApiError;
import com.example.recetasapp.repository.RecetaRepository;
import com.example.recetasapp.repository.RepoCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class FormularioActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "receta_id";

    private final RecetaRepository repository = new RecetaRepository();
    private final List<Categoria> categorias = new ArrayList<>();

    private int recetaId = 0;   // 0 = crear, mayor que 0 = editar

    private View contenido, layoutError;
    private ProgressBar progress;
    private TextView tvError, tvErrorCategoria;
    private TextInputLayout tilNombre, tilDescripcion, tilIngredientes, tilPreparacion, tilTiempo, tilPorciones;
    private Spinner spCategoria;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        recetaId = getIntent().getIntExtra(EXTRA_ID, 0);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(recetaId == 0 ? "Nueva receta" : "Editar receta");
        }

        contenido = findViewById(R.id.scrollContenido);
        layoutError = findViewById(R.id.layoutError);
        progress = findViewById(R.id.progress);
        tvError = findViewById(R.id.tvError);
        tvErrorCategoria = findViewById(R.id.tvErrorCategoria);
        tilNombre = findViewById(R.id.tilNombre);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        tilIngredientes = findViewById(R.id.tilIngredientes);
        tilPreparacion = findViewById(R.id.tilPreparacion);
        tilTiempo = findViewById(R.id.tilTiempo);
        tilPorciones = findViewById(R.id.tilPorciones);
        spCategoria = findViewById(R.id.spCategoria);
        btnGuardar = findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> guardar());
        findViewById(R.id.btnReintentar).setOnClickListener(v -> cargarDatos());

        cargarDatos();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ---------- Carga de datos (catálogo y, si se edita, la receta) ----------

    private void cargarDatos() {
        mostrarCargando();
        repository.obtenerCategorias(new RepoCallback<List<Categoria>>() {
            @Override
            public void onSuccess(List<Categoria> datos) {
                if (isFinishing() || datos == null) return;
                categorias.clear();
                categorias.addAll(datos);
                llenarSpinner();

                if (recetaId == 0) {
                    mostrarFormulario();
                } else {
                    cargarReceta();
                }
            }

            @Override
            public void onError(ApiError error) {
                if (isFinishing()) return;
                mostrarError(error.mensaje);
            }
        });
    }

    private void cargarReceta() {
        repository.obtener(recetaId, new RepoCallback<Receta>() {
            @Override
            public void onSuccess(Receta receta) {
                if (isFinishing() || receta == null) return;
                llenarCampos(receta);
                mostrarFormulario();
            }

            @Override
            public void onError(ApiError error) {
                if (isFinishing()) return;
                if (error.codigo == 404) {
                    Toast.makeText(FormularioActivity.this, error.mensaje, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    mostrarError(error.mensaje);
                }
            }
        });
    }

    private void llenarSpinner() {
        List<String> nombres = new ArrayList<>();
        nombres.add("Selecciona una categoría");
        for (Categoria c : categorias) {
            nombres.add(c.nombre);
        }
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nombres);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adaptador);
    }

    private void llenarCampos(Receta r) {
        tilNombre.getEditText().setText(r.nombre);
        tilDescripcion.getEditText().setText(r.descripcion);
        tilIngredientes.getEditText().setText(r.ingredientes);
        tilPreparacion.getEditText().setText(r.preparacion);
        tilTiempo.getEditText().setText(String.valueOf(r.tiempoPreparacionMin));
        tilPorciones.getEditText().setText(String.valueOf(r.porciones));

        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).id == r.categoriaId) {
                spCategoria.setSelection(i + 1);   // la posición 0 es el texto "Selecciona..."
                break;
            }
        }
    }

    // ---------- Validación antes de enviar ----------

    private boolean validar() {
        boolean ok = true;

        String nombre = texto(tilNombre);
        if (nombre.length() < 3 || nombre.length() > 120) {
            tilNombre.setError("El nombre debe tener entre 3 y 120 caracteres.");
            ok = false;
        } else {
            tilNombre.setError(null);
        }

        if (texto(tilDescripcion).length() > 500) {
            tilDescripcion.setError("La descripción no puede pasar de 500 caracteres.");
            ok = false;
        } else {
            tilDescripcion.setError(null);
        }

        String ingredientes = texto(tilIngredientes);
        if (ingredientes.length() < 5 || ingredientes.length() > 2000) {
            tilIngredientes.setError("Los ingredientes deben tener entre 5 y 2000 caracteres.");
            ok = false;
        } else {
            tilIngredientes.setError(null);
        }

        String preparacion = texto(tilPreparacion);
        if (preparacion.length() < 10 || preparacion.length() > 4000) {
            tilPreparacion.setError("La preparación debe tener entre 10 y 4000 caracteres.");
            ok = false;
        } else {
            tilPreparacion.setError(null);
        }

        Integer tiempo = entero(tilTiempo);
        if (tiempo == null || tiempo < 1 || tiempo > 1440) {
            tilTiempo.setError("El tiempo debe estar entre 1 y 1440 minutos.");
            ok = false;
        } else {
            tilTiempo.setError(null);
        }

        Integer porciones = entero(tilPorciones);
        if (porciones == null || porciones < 1 || porciones > 100) {
            tilPorciones.setError("Las porciones deben estar entre 1 y 100.");
            ok = false;
        } else {
            tilPorciones.setError(null);
        }

        if (spCategoria.getSelectedItemPosition() == 0) {
            tvErrorCategoria.setVisibility(View.VISIBLE);
            ok = false;
        } else {
            tvErrorCategoria.setVisibility(View.GONE);
        }

        return ok;
    }

    private String texto(TextInputLayout campo) {
        return campo.getEditText() == null ? "" : campo.getEditText().getText().toString().trim();
    }

    private Integer entero(TextInputLayout campo) {
        try {
            return Integer.parseInt(texto(campo));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ---------- Guardar ----------

    private void guardar() {
        if (!validar()) return;

        int posicion = spCategoria.getSelectedItemPosition();
        RecetaRequest cuerpo = new RecetaRequest(
                texto(tilNombre),
                texto(tilDescripcion),
                texto(tilIngredientes),
                texto(tilPreparacion),
                entero(tilTiempo),
                entero(tilPorciones),
                categorias.get(posicion - 1).id);

        btnGuardar.setEnabled(false);

        if (recetaId == 0) {
            repository.crear(cuerpo, new RepoCallback<Receta>() {
                @Override
                public void onSuccess(Receta datos) {
                    if (isFinishing()) return;
                    Toast.makeText(FormularioActivity.this, "Receta creada", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(ApiError error) {
                    manejarErrorGuardado(error);
                }
            });
        } else {
            repository.actualizar(recetaId, cuerpo, new RepoCallback<Void>() {
                @Override
                public void onSuccess(Void datos) {
                    if (isFinishing()) return;
                    Toast.makeText(FormularioActivity.this, "Receta actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(ApiError error) {
                    manejarErrorGuardado(error);
                }
            });
        }
    }

    private void manejarErrorGuardado(ApiError error) {
        if (isFinishing()) return;
        btnGuardar.setEnabled(true);

        switch (error.codigo) {
            case 409:   // nombre repetido
                tilNombre.setError(error.mensaje);
                Toast.makeText(this, error.mensaje, Toast.LENGTH_LONG).show();
                break;
            case 404:   // la receta ya no existe
                Toast.makeText(this, error.mensaje, Toast.LENGTH_LONG).show();
                finish();
                break;
            default:    // 400, sin conexión y otros errores
                new MaterialAlertDialogBuilder(this)
                        .setTitle(error.sinConexion ? "Sin conexión" : "No se pudo guardar")
                        .setMessage(error.mensaje)
                        .setPositiveButton("Entendido", null)
                        .show();
        }
    }

    // ---------- Estados de la pantalla ----------

    private void mostrarCargando() {
        progress.setVisibility(View.VISIBLE);
        contenido.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void mostrarFormulario() {
        progress.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        contenido.setVisibility(View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        progress.setVisibility(View.GONE);
        contenido.setVisibility(View.GONE);
        tvError.setText(mensaje);
        layoutError.setVisibility(View.VISIBLE);
    }
}