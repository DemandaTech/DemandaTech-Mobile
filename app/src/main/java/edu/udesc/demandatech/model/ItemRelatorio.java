package edu.udesc.demandatech.model;

public class ItemRelatorio {
    private final String nome;
    private final int valor;
    private final String detalhe;

    public ItemRelatorio(String nome, int valor, String detalhe) {
        this.nome = nome;
        this.valor = valor;
        this.detalhe = detalhe;
    }

    public String getNome() {
        return nome;
    }

    public int getValor() {
        return valor;
    }

    public String getDetalhe() {
        return detalhe;
    }
}
