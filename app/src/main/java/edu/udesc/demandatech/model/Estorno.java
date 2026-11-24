package edu.udesc.demandatech.model;

import java.util.Locale;

public class Estorno {

    private final int id;
    private String justificativa;
    private final String data;
    private final Demanda demanda;

    public Estorno(int id, String justificativa, String data, Demanda demanda) {
        this.id = id;
        this.justificativa = justificativa;
        this.data = data;
        this.demanda = demanda;
    }

    // O administrador e quem criou a demanda editam ou removem o estorno.
    public boolean podeEditar(Usuario usuario) {
        return usuario.isAdministrador() || demanda.foiCriadaPor(usuario);
    }

    // Código exibido nas telas, como EST-006.
    public String getCodigo() {
        return String.format(Locale.ROOT, "EST-%03d", id);
    }

    public int getId() {
        return id;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public String getData() {
        return data;
    }

    public Demanda getDemanda() {
        return demanda;
    }
}
