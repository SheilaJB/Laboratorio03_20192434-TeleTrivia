package com.example.teletrivia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EstadisticasActivity extends AppCompatActivity {

    private TextView textViewCorrectas;
    private TextView textViewIncorrectas;
    private TextView textViewNoRespondidas;
    private Button buttonVolverAJugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadisticas);

        // Inicializar los componentes
        textViewCorrectas = findViewById(R.id.textViewCorrectas);
        textViewIncorrectas = findViewById(R.id.textViewIncorrectas);
        textViewNoRespondidas = findViewById(R.id.textViewNoRespondidas);
        buttonVolverAJugar = findViewById(R.id.buttonVolverAJugar);

        // Obtener datos de la actividad anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int correctas = extras.getInt("correctas");
            int incorrectas = extras.getInt("incorrectas");
            int noRespondidas = extras.getInt("noRespondidas");

            textViewCorrectas.setText("Correctas: " + correctas);
            textViewIncorrectas.setText("Incorrectas: " + incorrectas);
            textViewNoRespondidas.setText("No respondidas: " + noRespondidas);
        }


        // Configurar botón para volver a jugar
        buttonVolverAJugar.setOnClickListener(v -> {
            Intent mainIntent = new Intent(EstadisticasActivity.this, MainActivity.class);
            // Limpiar el stack de actividades y empezar de nuevo
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}