package edu.udesc.demandatech.data.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.model.ItemRelatorio;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.Prioridade;
import edu.udesc.demandatech.model.Status;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;

final class Conversor {

    private static final Locale BRASIL = new Locale("pt", "BR");
    private static final Departamento SEM_DEPARTAMENTO = new Departamento(0, "", null);

    interface Item<T> {
        T converter(JSONObject json) throws JSONException;
    }

    private Conversor() {}

    static <T> Pagina<T> pagina(String corpo, int numero, Item<T> item) throws JSONException {
        JSONObject json = new JSONObject(corpo);
        List<T> itens = lista(json.getJSONArray("content"), item);
        int total = json.getJSONObject("page").getInt("totalElements");
        return new Pagina<>(itens, numero, total);
    }

    static <T> List<T> conteudo(String corpo, Item<T> item) throws JSONException {
        return lista(new JSONObject(corpo).getJSONArray("content"), item);
    }

    static <T> List<T> lista(JSONArray array, Item<T> item) throws JSONException {
        List<T> itens = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            itens.add(item.converter(array.getJSONObject(i)));
        }
        return itens;
    }

    static Usuario funcionario(JSONObject json) throws JSONException {
        return new Usuario(json.getString("matricula"), json.getString("nome"),
                json.optString("email", ""), Perfil.valueOf(json.getString("perfil")),
                texto(json, "departamento"));
    }

    static Usuario colaborador(JSONObject json) throws JSONException {
        return new Usuario(json.getString("matricula"), json.getString("nome"), null, "");
    }

    static Departamento departamentoResumo(JSONObject json) throws JSONException {
        return new Departamento(json.getLong("idDepartamento"), json.getString("descricao"), null);
    }

    static Departamento departamento(JSONObject json) throws JSONException {
        Usuario gerente = json.isNull("matriculaGerente") ? null
                : new Usuario(json.getString("matriculaGerente"), json.getString("gerente"), null, "");
        return new Departamento(json.getLong("idDepartamento"), json.getString("descricao"), gerente);
    }

    static TipoDemanda tipo(JSONObject json) throws JSONException {
        Departamento departamento = new Departamento(json.getLong("idDepartamento"),
                json.getString("departamento"), null);
        return new TipoDemanda(json.getLong("idTipo"), json.getString("descricao"), departamento);
    }

    static Demanda demandaResumo(JSONObject json) throws JSONException {
        Usuario responsavel = pessoa(null, texto(json, "responsavel"));
        TipoDemanda tipo = new TipoDemanda(0, texto(json, "tipo"), SEM_DEPARTAMENTO);
        return new Demanda(json.getInt("idDemanda"), json.getString("titulo"), null,
                Status.valueOf(json.getString("status")),
                Prioridade.valueOf(json.getString("prioridade")),
                tipo, null, responsavel, data(texto(json, "data")), new ArrayList<>());
    }

    static Demanda demanda(JSONObject json) throws JSONException {
        String departamento = texto(json, "departamento");
        TipoDemanda tipo = new TipoDemanda(json.getLong("idTipo"), texto(json, "tipoDemanda"),
                new Departamento(0, departamento, null));
        Usuario criador = new Usuario(json.getString("matriculaCriador"), texto(json, "criador"),
                null, texto(json, "departamentoCriador"));
        Usuario responsavel = pessoa(json.isNull("matriculaResponsavel") ? null
                : json.getString("matriculaResponsavel"), texto(json, "responsavel"));

        List<String> envolvidos = new ArrayList<>();
        JSONArray lista = json.optJSONArray("funcionariosEnvolvidos");
        if (lista != null) {
            for (int i = 0; i < lista.length(); i++) {
                envolvidos.add(lista.getJSONObject(i).getString("nome"));
            }
        }

        String abertura = data(texto(json, "dataAbertura"));
        String hora = texto(json, "horaAbertura");
        if (hora.length() >= 5) {
            abertura += " " + hora.substring(0, 5);
        }

        Demanda demanda = new Demanda(json.getInt("idDemanda"), json.getString("titulo"),
                texto(json, "descricao"), Status.valueOf(json.getString("status")),
                Prioridade.valueOf(json.getString("prioridade")), tipo, criador, responsavel,
                abertura, envolvidos);
        demanda.setJustificativa(json.isNull("justificativa") ? null : json.getString("justificativa"));
        return demanda;
    }

    static Estorno estorno(JSONObject json) throws JSONException {
        Usuario criador = pessoa(json.isNull("matriculaCriador") ? null
                : json.getString("matriculaCriador"), texto(json, "criador"));
        Usuario responsavel = pessoa(json.isNull("matriculaResponsavel") ? null
                : json.getString("matriculaResponsavel"), texto(json, "responsavel"));
        Demanda demanda = new Demanda(json.getInt("idDemanda"), texto(json, "tituloDemanda"), null,
                Status.RESOLVIDA, null, null, criador, responsavel, "", new ArrayList<>());
        return new Estorno(json.getInt("idEstorno"), texto(json, "descricao"),
                data(texto(json, "data")), demanda);
    }

    static ItemRelatorio rankingVisaoGeral(JSONObject json) throws JSONException {
        return new ItemRelatorio(json.getString("nome"), json.getInt("qtdDemanda"), null);
    }

    static ItemRelatorio porDepartamento(JSONObject json) throws JSONException {
        String detalhe = String.format(BRASIL, "%.1f%% das demandas", json.getDouble("percentual"));
        return new ItemRelatorio(json.getString("departamento"), json.getInt("qtdDemandas"), detalhe);
    }

    static ItemRelatorio estornosPorResponsavel(JSONObject json) throws JSONException {
        return new ItemRelatorio(json.getString("nome"), json.getInt("qtdEstornos"), null);
    }

    static ItemRelatorio maisProdutivo(JSONObject json) throws JSONException {
        String detalhe = json.getInt("pendencias") + " pendências · " + json.getInt("estornos") + " estornos";
        return new ItemRelatorio(json.getString("nome"), json.getInt("concluidas"), detalhe);
    }

    private static Usuario pessoa(String matricula, String nome) {
        return new Usuario(matricula == null ? "" : matricula, nome, null, "");
    }

    private static String texto(JSONObject json, String campo) {
        return json.isNull(campo) ? "" : json.optString(campo, "");
    }

    private static String data(String iso) {
        if (iso.length() < 10) {
            return iso;
        }
        return iso.substring(8, 10) + "/" + iso.substring(5, 7) + "/" + iso.substring(0, 4);
    }
}
