package edu.udesc.demandatech.model;

import java.util.List;

public class Relatorios {

    public final int resolvidas;
    public final int estornos;
    public final int emAndamento;
    public final List<ItemRelatorio> porDepartamento;
    public final List<ItemRelatorio> estornosPorResponsavel;
    public final List<ItemRelatorio> maisProdutivos;

    public Relatorios(int resolvidas, int estornos, int emAndamento,
                      List<ItemRelatorio> porDepartamento,
                      List<ItemRelatorio> estornosPorResponsavel,
                      List<ItemRelatorio> maisProdutivos) {
        this.resolvidas = resolvidas;
        this.estornos = estornos;
        this.emAndamento = emAndamento;
        this.porDepartamento = porDepartamento;
        this.estornosPorResponsavel = estornosPorResponsavel;
        this.maisProdutivos = maisProdutivos;
    }
}
