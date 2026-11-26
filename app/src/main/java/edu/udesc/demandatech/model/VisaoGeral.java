package edu.udesc.demandatech.model;

import java.util.List;

public class VisaoGeral {
    public final int total;
    public final int abertas;
    public final int aguardando;
    public final double taxaResolucao;
    public final List<Demanda> recentes;
    public final List<ItemRelatorio> ranking;

    public VisaoGeral(int total, int abertas, int aguardando, double taxaResolucao,
                      List<Demanda> recentes, List<ItemRelatorio> ranking) {
        this.total = total;
        this.abertas = abertas;
        this.aguardando = aguardando;
        this.taxaResolucao = taxaResolucao;
        this.recentes = recentes;
        this.ranking = ranking;
    }
}
