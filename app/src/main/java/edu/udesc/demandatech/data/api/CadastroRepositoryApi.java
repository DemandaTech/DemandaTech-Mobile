package edu.udesc.demandatech.data.api;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import edu.udesc.demandatech.data.CadastroRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Cadastro;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.ItemCadastro;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;

public class CadastroRepositoryApi implements CadastroRepository {

    private final ApiCliente api;

    public CadastroRepositoryApi(Sessao sessao) {
        this.api = new ApiCliente(sessao);
    }

    @Override
    public void listar(Cadastro cadastro, String busca, int pagina,
                       Resultado<Pagina<ItemCadastro>> resultado) {
        String caminho;
        Conversor.Item<ItemCadastro> item;
        if (cadastro == Cadastro.DEPARTAMENTOS) {
            caminho = "/cadastros/departamentos";
            item = Conversor::departamento;
        } else if (cadastro == Cadastro.TIPOS) {
            caminho = "/cadastros/tipos-demanda";
            item = Conversor::tipo;
        } else {
            caminho = "/cadastros/funcionarios";
            item = Conversor::funcionario;
        }
        Uri.Builder url = ApiCliente.caminho(caminho)
                .appendQueryParameter("page", String.valueOf(pagina - 1))
                .appendQueryParameter("size", String.valueOf(Pagina.TAMANHO));
        String texto = busca == null ? "" : busca.trim();
        if (!texto.isEmpty()) url.appendQueryParameter("search", texto);
        api.get(url.build().toString(), resposta -> Conversor.pagina(resposta, pagina, item), resultado);
    }

    @Override
    public void buscarFuncionario(String matricula, Resultado<Usuario> resultado) {
        api.get("/cadastros/funcionarios/" + matricula,
                resposta -> Conversor.funcionario(new JSONObject(resposta)), resultado);
    }

    // As telas trabalham com o nome do departamento; o backend precisa do id.
    @Override
    public void salvarFuncionario(String matricula, String nome, String email, Perfil perfil,
                                  String departamento, String senha, Resultado<Usuario> resultado) {
        listarDepartamentos(new Resultado<List<Departamento>>() {
            @Override
            public void sucesso(List<Departamento> departamentos) {
                Long idDepartamento = null;
                for (Departamento d : departamentos) {
                    if (d.getDescricao().equalsIgnoreCase(departamento)) {
                        idDepartamento = d.getId();
                    }
                }
                try {
                    JSONObject corpo = new JSONObject()
                            .put("nome", nome)
                            .put("email", email);
                    if (senha != null && !senha.isEmpty()) corpo.put("senha", senha);
                    if (perfil != null) corpo.put("perfil", perfil.name());
                    if (idDepartamento != null) {
                        corpo.put("departamento", new JSONObject().put("idDepartamento", idDepartamento));
                    }
                    if (matricula == null) {
                        api.post("/cadastros/funcionarios", corpo,
                                resposta -> Conversor.funcionario(new JSONObject(resposta)), resultado);
                    } else {
                        api.put("/cadastros/funcionarios/" + matricula, corpo,
                                resposta -> Conversor.funcionario(new JSONObject(resposta)), resultado);
                    }
                } catch (JSONException e) {
                    resultado.erro("Não foi possível montar a requisição.");
                }
            }

            @Override
            public void erro(String mensagem) {
                resultado.erro(mensagem);
            }
        });
    }

    @Override
    public void removerFuncionario(String matricula, Resultado<Void> resultado) {
        api.delete("/cadastros/funcionarios/" + matricula, resultado);
    }

    @Override
    public void listarDepartamentos(Resultado<List<Departamento>> resultado) {
        api.get("/publico/departamentos",
                resposta -> Conversor.lista(new JSONArray(resposta), Conversor::departamentoResumo),
                resultado);
    }

    @Override
    public void buscarDepartamento(long id, Resultado<Departamento> resultado) {
        api.get("/cadastros/departamentos/" + id,
                resposta -> Conversor.departamento(new JSONObject(resposta)), resultado);
    }

    @Override
    public void salvarDepartamento(long id, String descricao, Usuario gerente,
                                   Resultado<Departamento> resultado) {
        try {
            JSONObject corpo = new JSONObject().put("descricao", descricao);
            if (gerente != null) {
                corpo.put("gerente", new JSONObject().put("matricula", Long.parseLong(gerente.getMatricula())));
            }
            if (id == 0) {
                api.post("/cadastros/departamentos", corpo,
                        resposta -> Conversor.departamento(new JSONObject(resposta)), resultado);
            } else {
                api.put("/cadastros/departamentos/" + id, corpo,
                        resposta -> Conversor.departamento(new JSONObject(resposta)), resultado);
            }
        } catch (JSONException | NumberFormatException e) {
            resultado.erro("Não foi possível montar a requisição.");
        }
    }

    @Override
    public void removerDepartamento(long id, Resultado<Void> resultado) {
        api.delete("/cadastros/departamentos/" + id, resultado);
    }

    @Override
    public void buscarTipo(long id, Resultado<TipoDemanda> resultado) {
        api.get("/cadastros/tipos-demanda/" + id,
                resposta -> Conversor.tipo(new JSONObject(resposta)), resultado);
    }

    @Override
    public void salvarTipo(long id, String descricao, Departamento departamento,
                           Resultado<TipoDemanda> resultado) {
        try {
            JSONObject corpo = new JSONObject().put("descricao", descricao);
            if (departamento != null) {
                corpo.put("departamento", new JSONObject().put("idDepartamento", departamento.getId()));
            }
            if (id == 0) {
                api.post("/cadastros/tipos-demanda", corpo,
                        resposta -> Conversor.tipo(new JSONObject(resposta)), resultado);
            } else {
                api.put("/cadastros/tipos-demanda/" + id, corpo,
                        resposta -> Conversor.tipo(new JSONObject(resposta)), resultado);
            }
        } catch (JSONException e) {
            resultado.erro("Não foi possível montar a requisição.");
        }
    }

    @Override
    public void removerTipo(long id, Resultado<Void> resultado) {
        api.delete("/cadastros/tipos-demanda/" + id, resultado);
    }
}
