package com.example.recetasapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetasapp.model.Categoria;
import com.example.recetasapp.model.PagedResult;
import com.example.recetasapp.model.Receta;
import com.example.recetasapp.network.ApiError;
import com.example.recetasapp.repository.RecetaRepository;
import com.example.recetasapp.repository.RepoCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int TAMANO_PAGINA = 50;
    private static final long RETRASO_BUSQUEDA_MS = 400;

    private final RecetaRepository repository = new RecetaRepository();
    private final List<Categoria> categorias = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable busquedaPendiente;
    private int contadorSolicitudes = 0;
    private Integer filtroCategoriaId = null;

    private RecetaAdapter adapter;
    private EditText etBuscar;
    private Spinner spCategoria;
    private TextView tvConteo, tvVacio, tvError;
    private RecyclerView rvRecetas;
    private ProgressBar progress;
    private View layoutError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        etBuscar = findViewById(R.id.etBuscar);
        spCategoria = findViewById(R.id.spCategoria);
        tvConteo = findViewById(R.id.tvConteo);
        tvVacio = findViewById(R.id.tvVacio);
        tvError = findViewById(R.id.tvError);
        rvRecetas = findViewById(R.id.rvRecetas);
        progress = findViewById(R.id.progress);
        layoutError = findViewById(R.id.layoutError);

        adapter = new RecetaAdapter(receta -> {
            Intent intent = new Intent(this, DetalleActivity.class);
            intent.putExtra(DetalleActivity.EXTRA_ID, receta.id);
            startActivity(intent);
        });
        rvRecetas.setLayoutManager(new LinearLayoutManager(this));
        rvRecetas.setAdapter(adapter);

        findViewById(R.id.fabNueva).setOnClickListener(v ->
                startActivity(new Intent(this, FormularioActivity.class)));
        findViewById(R.id.btnReintentar).setOnClickListener(v -> reintentar());

        configurarBusqueda();
        configurarFiltro();
        cargarCategorias();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se recarga al volver del detalle o del formulario
        cargarRecetas();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    // ---------- Búsqueda y filtro ----------

    private void configurarBusqueda() {
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // Espera un momento después de escribir para no llamar a la API en cada letra
                if (busquedaPendiente != null) {
                    handler.removeCallbacks(busquedaPendiente);
                }
                busquedaPendiente = () -> cargarRecetas();
                handler.postDelayed(busquedaPendiente, RETRASO_BUSQUEDA_MS);
            }
        });
    }

    private void configurarFiltro() {
        mostrarOpcionesFiltro();
        spCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Integer nuevo = position == 0 ? null : categorias.get(position - 1).id;
                if (Objects.equals(nuevo, filtroCategoriaId)) return;
                filtroCategoriaId = nuevo;
                cargarRecetas();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void mostrarOpcionesFiltro() {
        List<String> nombres = new ArrayList<>();
        nombres.add("Todas las categorías");
        for (Categoria c : categorias) {
            nombres.add(c.nombre);
        }
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nombres);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adaptador);
    }

    // ---------- Carga de datos ----------

    private void cargarCategorias() {
        repository.obtenerCategorias(new RepoCallback<List<Categoria>>() {
            @Override
            public void onSuccess(List<Categoria> datos) {
                if (isFinishing() || datos == null) return;
                categorias.clear();
                categorias.addAll(datos);
                mostrarOpcionesFiltro();
            }

            @Override
            public void onError(ApiError error) {
                // El error de conexión se muestra al cargar la lista, aquí no se repite
            }
        });
    }

    private void cargarRecetas() {
        final int solicitud = ++contadorSolicitudes;
        final String busqueda = etBuscar.getText().toString().trim();

        mostrarCargando();

        repository.listar(busqueda.isEmpty() ? null : busqueda, filtroCategoriaId, 1, TAMANO_PAGINA,
                new RepoCallback<PagedResult<Receta>>() {
                    @Override
                    public void onSuccess(PagedResult<Receta> pagina) {
                        // Se ignoran respuestas viejas si el usuario ya cambió la búsqueda
                        if (solicitud != contadorSolicitudes || isFinishing()) return;
                        boolean hayFiltros = !busqueda.isEmpty() || filtroCategoriaId != null;
                        mostrarLista(pagina, hayFiltros);
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (solicitud != contadorSolicitudes || isFinishing()) return;
                        mostrarError(error.mensaje);
                    }
                });
    }

    private void reintentar() {
        if (categorias.isEmpty()) {
            cargarCategorias();
        }
        cargarRecetas();
    }

    // ---------- Estados de la pantalla ----------

    private void mostrarCargando() {
        progress.setVisibility(View.VISIBLE);
        rvRecetas.setVisibility(View.GONE);
        tvVacio.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void mostrarLista(PagedResult<Receta> pagina, boolean hayFiltros) {
        progress.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);

        List<Receta> items = (pagina != null && pagina.items != null)
                ? pagina.items : new ArrayList<>();
        adapter.setRecetas(items);

        if (items.isEmpty()) {
            tvVacio.setText(hayFiltros
                    ? "No se encontraron recetas con esa búsqueda o filtro."
                    : "Aún no hay recetas. Toca + para crear la primera.");
            tvVacio.setVisibility(View.VISIBLE);
            rvRecetas.setVisibility(View.GONE);
            tvConteo.setText("0 recetas");
        } else {
            tvVacio.setVisibility(View.GONE);
            rvRecetas.setVisibility(View.VISIBLE);
            tvConteo.setText("Mostrando " + items.size() + " de " + pagina.totalCount + " recetas");
        }
    }

    private void mostrarError(String mensaje) {
        progress.setVisibility(View.GONE);
        rvRecetas.setVisibility(View.GONE);
        tvVacio.setVisibility(View.GONE);
        tvConteo.setText("");
        tvError.setText(mensaje);
        layoutError.setVisibility(View.VISIBLE);
    }
}