package com.example.teletrivia;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Indicar los botones, edittext y demas componentes usados :D
    private Button btnComprobarConexion;
    private Button btnComenzar;
    private EditText editTextCantidad;
    private Spinner spinnerCategoria;
    private Spinner spinnerDificultad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Inicializar los componentes
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);
        editTextCantidad = findViewById(R.id.editTextCantidad);
        btnComprobarConexion = findViewById(R.id.btnComprobarConexion);
        btnComenzar = findViewById(R.id.btnComenzar);

        // Crear las categorias
        String[] categorias = {"Selecionar Categoria","Cultura General", "Libros", "Películas", "Música", "Computación", "Deportes", "Matemática", "Historia"};
        spinnerCategoria.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias));

        // Crear las dificultades
        String[] dificultades = {"Selecionar Dificultad","Fácil", "Medio", "Difícil"};
        spinnerDificultad.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dificultades));

        // Crear el boton de comprobar conexion
        btnComprobarConexion.setOnClickListener(v -> {

            // Validar los campos
            if (validarCampos()) {
                if (validarConexionInternet()) {
                    // Si hay conexión, habilitar el botón "Comenzar"
                    btnComenzar.setEnabled(true);
                    Toast.makeText(this, "Conexión establecida", Toast.LENGTH_SHORT).show();
                } else {
                    // Si no hay conexión, mostrar un mensaje y deshabilitar el botón "Comenzar"
                    btnComenzar.setEnabled(false);
                    Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
                }
            }


        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validarCampos() {

        String categoriaSeleccionada = spinnerCategoria.getSelectedItem().toString();
        String dificultadSeleccionada = spinnerDificultad.getSelectedItem().toString();

        if (categoriaSeleccionada.equals("Selecionar Categoria")) {
            Toast.makeText(this, "Seleccione una categoría válida", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (dificultadSeleccionada.equals("Selecionar Dificultad")) {
            Toast.makeText(this, "Seleccione una dificultad válida", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que los campos esten completos
        if (spinnerCategoria.getSelectedItem() == null || spinnerDificultad.getSelectedItem() == null || editTextCantidad.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Inicializar el edittext de cantidad
        int cantidad = Integer.parseInt(editTextCantidad.getText().toString());

        if (cantidad <= 0) {
            Toast.makeText(this, "La cantidad debe ser positiva", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    private boolean validarConexionInternet(){

        // Inicializar el connectivity manager
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        /*
        Segun se muestra en la teoria, se usa NetworkInfo
        pero debido a que uso una version mas recinete de API es obsoleto
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();

        Promt: estoy empleando la validacion a conexion a intenert pero cuando ingreso NetworkInfo me sale como si estuvia inabilitado en el mismo codigo  NetworkInfo ni = cm.getActiveNetworkInfo(); como podria solucionar el error?
        Respuesta: Puedes usar NetworkCapabilities en lugar de NetworkInfo para verificar la conexión a Internet en versiones más recientes de Android. Aquí tienes un ejemplo de cómo hacerlo:
        */

        Network network = cm.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

    }
}