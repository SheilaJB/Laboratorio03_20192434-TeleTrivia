package com.example.teletrivia;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Repuesta {
    @SerializedName("response_code")
    private int responseCode;

    @SerializedName("results")
    private List<Pregunta> results;

    public int getResponseCode() {
        return responseCode;
    }

    public List<Pregunta> getResults() {
        return results;
    }
}
