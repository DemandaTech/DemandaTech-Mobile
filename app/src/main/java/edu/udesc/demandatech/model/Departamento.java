package edu.udesc.demandatech.model;

import java.util.Locale;

public class Departamento implements ItemCadastro {

    private final long id;
    private String descricao;
    private Usuario gerente;

    public Departamento(long id, String descricao, Usuario gerente) {
        this.id = id;
        this.descricao = descricao;
        this.gerente = gerente;
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

    public Usuario getGerente() {
        return gerente;
    }

    public void setGerente(Usuario gerente) {
        this.gerente = gerente;
    }

    @Override
    public String getTitulo() {
        return descricao;
    }

    @Override
    public String getDetalhe() {
        String responsavel = gerente == null ? "Sem gerente" : "Gerente: " + gerente.getNome();
        return String.format(Locale.ROOT, "ID %02d · %s", id, responsavel);
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
