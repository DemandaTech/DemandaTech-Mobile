package edu.udesc.demandatech.data.api;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.udesc.demandatech.data.EstornoRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.model.Pagina;

public class EstornoRepositoryApi implements EstornoRepository {

    // Na tela o estorno aparece como EST-006; o backend busca pelo número puro.
    private static final Pattern CODIGO = Pattern.compile("(?i)^EST-?0*(\\d+)$");

    private final ApiCliente api;

    public EstornoRepositoryApi(Sessao sessao) {
        this.api = new ApiCliente(sessao);
    }

    @Override
    public void listar(String busca, int pagina, Resultado<Pagina<Estorno>> resultado) {
        Uri.Builder url = ApiCliente.caminho("/estornos")
                .appendQueryParameter("page", String.valueOf(pagina - 1))
                .appendQueryParameter("size", String.valueOf(Pagina.TAMANHO));
        String texto = busca == null ? "" : busca.trim().replace("#", "");
        Matcher codigo = CODIGO.matcher(texto);
        if (codigo.matches()) texto = codigo.group(1);
        if (!texto.isEmpty()) url.appendQueryParameter("search", texto);
        api.get(url.build().toString(),
                resposta -> Conversor.pagina(resposta, pagina, Conversor::estorno), resultado);
    }

    @Override
    public void listarDaDemanda(int idDemanda, Resultado<List<Estorno>> resultado) {
        String url = ApiCliente.caminho("/estornos")
                .appendQueryParameter("idDemanda", String.valueOf(idDemanda))
                .appendQueryParameter("size", DemandaRepositoryApi.TODOS).build().toString();
        api.get(url, resposta -> Conversor.conteudo(resposta, Conversor::estorno), resultado);
    }

    @Override
    public void buscar(int id, Resultado<Estorno> resultado) {
        api.get("/estornos/" + id, resposta -> Conversor.estorno(new JSONObject(resposta)), resultado);
    }

    @Override
    public void registrar(int idDemanda, String justificativa, Resultado<Estorno> resultado) {
        try {
            JSONObject corpo = new JSONObject()
                    .put("justificativa", justificativa)
                    .put("idDemanda", idDemanda);
            api.post("/estornos", corpo, resposta -> Conversor.estorno(new JSONObject(resposta)),
                    resultado);
        } catch (JSONException e) {
            resultado.erro("Não foi possível montar a requisição.");
        }
    }

    @Override
    public void editar(int id, String justificativa, Resultado<Estorno> resultado) {
        try {
            JSONObject corpo = new JSONObject().put("justificativa", justificativa);
            api.put("/estornos/" + id, corpo, resposta -> Conversor.estorno(new JSONObject(resposta)),
                    resultado);
        } catch (JSONException e) {
            resultado.erro("Não foi possível montar a requisição.");
        }
    }

    @Override
    public void remover(int id, Resultado<Void> resultado) {
        api.delete("/estornos/" + id, resultado);
    }
}
