package edu.udesc.demandatech.model;

import java.util.List;

public class Pagina<T> {

    public static final int TAMANHO = 10;

    private final List<T> itens;
    private final int numero;
    private final int total;

    public Pagina(List<T> itens, int numero, int total) {
        this.itens = itens;
        this.numero = numero;
        this.total = total;
    }

    public List<T> getItens() {
        return itens;
    }

    public int getTotal() {
        return total;
    }

    public boolean temProxima() {
        return numero * TAMANHO < total;
    }
}
