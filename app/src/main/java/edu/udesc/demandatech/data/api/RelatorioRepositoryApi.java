package edu.udesc.demandatech.data.api;

import org.json.JSONObject;

import edu.udesc.demandatech.data.RelatorioRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Relatorios;
import edu.udesc.demandatech.model.VisaoGeral;

public class RelatorioRepositoryApi implements RelatorioRepository {

    private final ApiCliente api;

    public RelatorioRepositoryApi(Sessao sessao) {
        this.api = new ApiCliente(sessao);
    }

    @Override
    public void visaoGeral(Resultado<VisaoGeral> resultado) {
        api.get("/visao-geral", resposta -> {
            JSONObject json = new JSONObject(resposta);
            return new VisaoGeral(json.getInt("totalDemandas"), json.getInt("abertas"),
                    json.getInt("aguardando"), json.getDouble("taxaConclusao"),
                    Conversor.lista(json.getJSONArray("chamadosRecentes"), Conversor::demandaResumo),
                    Conversor.lista(json.getJSONArray("melhoresFuncionarios"), Conversor::rankingVisaoGeral));
        }, resultado);
    }

    @Override
    public void relatorios(Resultado<Relatorios> resultado) {
        api.get("/relatorios", resposta -> {
            JSONObject json = new JSONObject(resposta);
            return new Relatorios(json.getInt("demandasConcluidas"), json.getInt("estornosRegistrados"),
                    json.getInt("emAndamento"),
                    Conversor.lista(json.getJSONArray("demandasPorDepartamento"), Conversor::porDepartamento),
                    Conversor.lista(json.getJSONArray("estornosPorResponsavel"), Conversor::estornosPorResponsavel),
                    Conversor.lista(json.getJSONArray("maisProdutivos"), Conversor::maisProdutivo));
        }, resultado);
    }
}
