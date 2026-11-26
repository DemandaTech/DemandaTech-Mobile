package edu.udesc.demandatech.model;

public class ResumoDemandas {
    public final int total;
    public final int abertas;
    public final int aguardando;
    public final int resolvidas;

    public ResumoDemandas(int total, int abertas, int aguardando, int resolvidas) {
        this.total = total;
        this.abertas = abertas;
        this.aguardando = aguardando;
        this.resolvidas = resolvidas;
    }
}
