package edu.udesc.demandatech.model;

import java.io.Serializable;

public class FiltroDemanda implements Serializable {
    public String texto = "";
    public Status status;
    public Prioridade prioridade;
    public Long idTipo;

    public int quantidadeAtivos() {
        int ativos = 0;
        if (status != null) ativos++;
        if (prioridade != null) ativos++;
        if (idTipo != null) ativos++;
        return ativos;
    }
}
