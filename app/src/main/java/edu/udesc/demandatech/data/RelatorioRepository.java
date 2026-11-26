package edu.udesc.demandatech.data;

import edu.udesc.demandatech.model.Relatorios;
import edu.udesc.demandatech.model.VisaoGeral;

public interface RelatorioRepository {

    void visaoGeral(Resultado<VisaoGeral> resultado);

    void relatorios(Resultado<Relatorios> resultado);
}
