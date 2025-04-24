package com.example.teletrivia;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TriviaActivity extends AppCompatActivity {

    private TextView textViewCategoria, textViewPreguntaNumero, textViewPregunta, textViewTimer;
    private RadioGroup radioGroupRespuestas;
    private RadioButton[] radioButtons;
    private Button buttonSiguiente;

    private List<Pregunta> listaPreguntas;
    private int preguntaActual = 0;
    private int correctas = 0;
    private CountDownTimer countDownTimer;
    private long tiempoRestante;
    private int tiempoPorPregunta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trivia);


        textViewCategoria = findViewById(R.id.textViewCategoria);
        textViewPreguntaNumero = findViewById(R.id.textViewPreguntaNumero);
        textViewPregunta = findViewById(R.id.textViewPregunta);
        textViewTimer = findViewById(R.id.textViewTimer);
        radioGroupRespuestas = findViewById(R.id.radioGroupRespuestas);
        buttonSiguiente = findViewById(R.id.buttonSiguiente);

        radioButtons = new RadioButton[]{
                findViewById(R.id.radioButtonRespuesta1),
                findViewById(R.id.radioButtonRespuesta2),
                findViewById(R.id.radioButtonRespuesta3),
                findViewById(R.id.radioButtonRespuesta4)

        };

        // Parámetros desde MainActivity
        int cantidad = getIntent().getIntExtra("cantidad", 5);
        String categoriaNombre = getIntent().getStringExtra("categoria");
        String dificultadOriginal = getIntent().getStringExtra("dificultad");
        String dificultad = mapearDificultad(dificultadOriginal);

        Log.d("TriviaDebug", "Cantidad: " + cantidad);
        Log.d("TriviaDebug", "Categoría nombre: " + categoriaNombre);
        Log.d("TriviaDebug", "Dificultad original: " + dificultadOriginal);
        Log.d("TriviaDebug", "Dificultad mapeada: " + dificultad);

        int categoriaId = convertirCategoria(categoriaNombre);
        Log.d("TriviaDebug", "Categoría ID: " + categoriaId);
        tiempoPorPregunta = getTiempoPorDificultad(dificultad);
        textViewCategoria.setText(categoriaNombre);

        obtenerPreguntas(cantidad, categoriaId, dificultad);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private String mapearDificultad(String dificultad) {
        if (dificultad == null) return "easy";

        switch (dificultad.toLowerCase()) {
            case "fácil": return "easy";
            case "medio": return "medium";
            case "difícil": return "hard";
            default: return "easy";
        }
    }

    private void obtenerPreguntas(int cantidad, int categoria, String dificultad) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TriviaApiServicio api = retrofit.create(TriviaApiServicio.class);
        Call<Repuesta> call = api.getQuestions(cantidad, categoria, dificultad, "multiple");

        // LOOOOOOGGGG
        Log.d("TriviaDebug", "Llamando a la API con: cantidad=" + cantidad +
                ", categoria=" + categoria + ", dificultad=" + dificultad);

        call.enqueue(new Callback<Repuesta>() {
            @Override
            public void onResponse(Call<Repuesta> call, Response<Repuesta> response) {
                if (response.isSuccessful()) {
                    Log.d("TriviaDebug", "Respuesta exitosa de la API");
                    Repuesta respuesta = response.body();

                    if (respuesta != null) {
                        Log.d("TriviaDebug", "Código de respuesta: " + respuesta.getResponseCode());
                        listaPreguntas = respuesta.getResults();

                        if (listaPreguntas != null && !listaPreguntas.isEmpty()) {
                            Log.d("TriviaDebug", "Preguntas recibidas: " + listaPreguntas.size());
                            iniciarTrivia();
                        } else {
                            Log.w("TriviaDebug", "La lista de preguntas está vacía");
                            String errorMsg = "No se encontraron preguntas para esta configuración";
                            mostrarError(errorMsg);
                        }
                    } else {
                        Log.e("TriviaDebug", "El cuerpo de respuesta es nulo");
                        mostrarError("Error en el formato de respuesta");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin detalle";
                        Log.e("TriviaDebug", "Respuesta no exitosa: " + response.code() + " - " + errorBody);
                        mostrarError("Error " + response.code() + ": " + errorBody);
                    } catch (Exception e) {
                        Log.e("TriviaDebug", "Error al leer respuesta: " + e.getMessage());
                        mostrarError("Error de comunicación: " + response.code());
                    }
                }
            }
            @Override
            public void onFailure(Call<Repuesta> call, Throwable t) {
                mostrarError("Fallo de conexión");
            }
        });
    }

    private void iniciarTrivia() {
        iniciarContador();
        mostrarPregunta();

        buttonSiguiente.setOnClickListener(v -> {
            if (verificarRespuesta()) correctas++;
            if (++preguntaActual < listaPreguntas.size()) {
                mostrarPregunta();
            } else {
                countDownTimer.cancel();
                irAEstadisticas();
            }
        });
    }

    private void mostrarPregunta() {
        Pregunta pregunta = listaPreguntas.get(preguntaActual);
        textViewPreguntaNumero.setText("Pregunta " + (preguntaActual + 1) + "/" + listaPreguntas.size());
        textViewPregunta.setText(Html.fromHtml(pregunta.getQuestion(), Html.FROM_HTML_MODE_LEGACY));
        radioGroupRespuestas.clearCheck();

        // Mezclar respuestas
        List<String> opciones = new ArrayList<>(pregunta.getIncorrectAnswers());
        opciones.add(pregunta.getCorrectAnswer());
        Collections.shuffle(opciones);

        for (int i = 0; i < radioButtons.length; i++) {
            radioButtons[i].setText(Html.fromHtml(opciones.get(i), Html.FROM_HTML_MODE_LEGACY));
        }
    }

    private boolean verificarRespuesta() {
        int seleccionId = radioGroupRespuestas.getCheckedRadioButtonId();
        if (seleccionId == -1) return false;

        RadioButton seleccionada = findViewById(seleccionId);
        String respuesta = Html.fromHtml(listaPreguntas.get(preguntaActual).getCorrectAnswer(), Html.FROM_HTML_MODE_LEGACY).toString();
        return seleccionada.getText().toString().equals(respuesta);
    }

    private void iniciarContador() {
        tiempoRestante = listaPreguntas.size() * tiempoPorPregunta * 1000L;

        countDownTimer = new CountDownTimer(tiempoRestante, 1000) {
            public void onTick(long millisUntilFinished) {
                int segundos = (int) (millisUntilFinished / 1000);
                textViewTimer.setText(String.format("%02d:%02d", segundos / 60, segundos % 60));
            }

            public void onFinish() {
                irAEstadisticas();
            }
        }.start();
    }

    private void irAEstadisticas() {
        Intent intent = new Intent(this, EstadisticasActivity.class);
        intent.putExtra("correctas", correctas);
        intent.putExtra("total", listaPreguntas.size());
        startActivity(intent);
        finish();
    }

    private int convertirCategoria(String categoria) {
        Log.d("TriviaDebug", "Convirtiendo categoría: " + categoria);
        switch (categoria) {
            case "Cultura General": return 9;
            case "Libros": return 10;
            case "Películas": return 11;
            case "Música": return 12;
            case "Computación": return 18;
            case "Deportes": return 21;
            case "Matemática": return 19;
            case "Historia": return 23;
            default: return 9;
        }
    }

    private int getTiempoPorDificultad(String dificultad) {
        if (dificultad == null) return 5;

        switch (dificultad.toLowerCase()) {
            case "fácil":
            case "easy":
                return 5;
            case "medio":
            case "medium":
                return 7;
            case "difícil":
            case "hard":
                return 10;
            default:
                return 5;
        }
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        finish();
    }




}