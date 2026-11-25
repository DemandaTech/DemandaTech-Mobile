package edu.udesc.demandatech.data.api;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import edu.udesc.demandatech.data.DemandaRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.FiltroDemanda;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.Prioridade;
import edu.udesc.demandatech.model.ResumoDemandas;
import edu.udesc.demandatech.model.Status;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;

public class DemandaRepositoryApi implements DemandaRepository {

    static final String TODOS = "1000";

    private final ApiCliente api;

    public DemandaRepositoryApi(Sessao sessao) {
        this.api = new ApiCliente(sessao);
    }

    @Override
    public void listar(FiltroDemanda filtro, int pagina, Resultado<Pagina<Demanda>> resultado) {
        Uri.Builder url = ApiCliente.caminho("/demanda")
                .appendQueryParameter("page", String.valueOf(pagina - 1))
                .appendQueryParameter("size", String.valueOf(Pagina.TAMANHO));
        String texto = filtro.texto == null ? "" : filtro.texto.trim().replace("#", "");
        if (!texto.isEmpty()) url.appendQueryParameter("search", texto);
        if (filtro.status != null) url.appendQueryParameter("status", filtro.status.name());
        if (filtro.prioridade != null) url.appendQueryParameter("prioridade", filtro.prioridade.name());
        if (filtro.idTipo != null) url.appendQueryParameter("idTipo", String.valueOf(filtro.idTipo));
        api.get(url.build().toString(),
                resposta -> Conversor.pagina(resposta, pagina, Conversor::demandaResumo), resultado);
    }

    @Override
    public void resumo(Resultado<ResumoDemandas> resultado) {
        api.get("/demanda/resumo", resposta -> {
            JSONObject json = new JSONObject(resposta);
            return new ResumoDemandas(json.getInt("total"), json.getInt("abertas"),
                    json.getInt("aguardando"), json.getInt("resolvidas"));
        }, resultado);
    }

    @Override
    public void buscar(int id, Resultado<Demanda> resultado) {
        api.get("/demanda/" + id, resposta -> Conversor.demanda(new JSONObject(resposta)), resultado);
    }

    @Override
    public void criar(String titulo, String descricao, Prioridade prioridade, TipoDemanda tipo,
                      List<Usuario> envolvidos, Resultado<Demanda> resultado) {
        try {
            JSONArray lista = new JSONArray();
            for (Usuario envolvido : envolvidos) {
                lista.put(new JSONObject().put("matricula", Long.parseLong(envolvido.getMatricula())));
            }
            JSONObject corpo = new JSONObject()
                    .put("titulo", titulo)
                    .put("descricao", descricao)
                    .put("prioridade", prioridade == null ? JSONObject.NULL : prioridade.name())
                    .put("tipo", tipo == null ? JSONObject.NULL : new JSONObject().put("idTipo", tipo.getId()))
                    .put("usuariosEnvolvidos", lista);
            api.post("/demanda/criar", corpo, resposta -> Conversor.demanda(new JSONObject(resposta)),
                    resultado);
        } catch (JSONException | NumberFormatException e) {
            resultado.erro("Não foi possível montar a requisição.");
        }
    }

    @Override
    public void editar(int id, String titulo, String descricao, Prioridade prioridade,
                       TipoDemanda tipo, Resultado<Demanda> resultado) {
        try {
            JSONObject corpo = new JSONObject()
                    .put("titulo", titulo)
                    .put("descricao", descricao)
                    .put("prioridade", prioridade == null ? JSONObject.NULL : prioridade.name())
                    .put("idTipo", tipo == null ? JSONObject.NULL : tipo.getId());
            api.put("/demanda/" + id, corpo, resposta -> Conversor.demanda(new JSONObject(resposta)),
                    resultado);
        } catch (JSONException e) {
            resultado.erro("Não foi possível montar a requisição.");
        }
    }

    @Override
    public void alterarStatus(int id, Status status, String justificativa,
                              Resultado<Demanda> resultado) {
        try {
            JSONObject corpo = new JSONObject().put("status", status.name());
            if (status == Status.RESOLVIDA && justificativa != null && !justificativa.trim().isEmpty()) {
                corpo.put("justificativa", justificativa.trim());
            }
            api.patch("/demanda/" + id + "/status", corpo,
                    resposta -> Conversor.demanda(new JSONObject(resposta)), resultado);
        } catch (JSONException e) {
            resultado.erro("Não foi possível montar a requisição.");
        }
    }

    @Override
    public void excluir(int id, Resultado<Void> resultado) {
        api.delete("/demanda/" + id, resultado);
    }

    @Override
    public void listarTipos(Resultado<List<TipoDemanda>> resultado) {
        String url = ApiCliente.caminho("/cadastros/tipos-demanda")
                .appendQueryParameter("size", TODOS).build().toString();
        api.get(url, resposta -> Conversor.conteudo(resposta, Conversor::tipo), resultado);
    }

    @Override
    public void listarFuncionarios(Resultado<List<Usuario>> resultado) {
        String url = ApiCliente.caminho("/demanda/criar")
                .appendQueryParameter("size", TODOS).build().toString();
        api.get(url, resposta -> Conversor.conteudo(resposta, Conversor::colaborador), resultado);
    }
}
