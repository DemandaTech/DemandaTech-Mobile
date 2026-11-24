package edu.udesc.demandatech.data;

import edu.udesc.demandatech.model.Relatorios;
import edu.udesc.demandatech.model.VisaoGeral;

// Números consolidados. O backend calcula conforme o que o perfil logado pode ver.
public interface RelatorioRepository {

    void visaoGeral(Resultado<VisaoGeral> resultado);

    void relatorios(Resultado<Relatorios> resultado);
}
