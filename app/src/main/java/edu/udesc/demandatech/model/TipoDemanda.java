package edu.udesc.demandatech.model;

import java.util.Locale;

public class TipoDemanda implements ItemCadastro {
    private final long id;
    private String descricao;
    private Departamento departamento;

    public TipoDemanda(long id, String descricao, Departamento departamento) {
        this.id = id;
        this.descricao = descricao;
        this.departamento = departamento;
    }

    public long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    @Override
    public String getTitulo() {
        return descricao;
    }

    @Override
    public String getDetalhe() {
        return String.format(Locale.ROOT, "ID %02d · %s", id, departamento.getDescricao());
    }

    @Override
    public String getMarcador() {
        return null;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
