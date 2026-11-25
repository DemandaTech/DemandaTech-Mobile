package edu.udesc.demandatech.data.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import edu.udesc.demandatech.data.AuthRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.Autenticacao;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Usuario;

public class AuthRepositoryApi implements AuthRepository {

    private final ApiCliente api = new ApiCliente(null);

    @Override
    public void entrar(String matricula, String senha, Resultado<Autenticacao> resultado) {
        long numero;
        try {
            numero = Long.parseLong(matricula.trim());
        } catch (NumberFormatException e) {
            resultado.erro("Matrícula ou senha inválida.");
            return;
        }
        try {
            JSONObject corpo = new JSONObject().put("matricula", numero).put("senha", senha);
            api.post("/login", corpo, resposta -> {
                JSONObject json = new JSONObject(resposta);
                return new Autenticacao(Conversor.funcionario(json), json.getString("token"));
            }, resultado);
        } catch (JSONException e) {
            resultado.erro("Não foi possível montar a requisição.");
        }
    }

    @Override
    public void criarConta(String nome, String email, String departamento, String senha,
                           Resultado<Usuario> resultado) {
        listarDepartamentos(new Resultado<List<Departamento>>() {
            @Override
            public void sucesso(List<Departamento> departamentos) {
                Departamento escolhido = null;
                for (Departamento d : departamentos) {
                    if (d.getDescricao().equalsIgnoreCase(departamento)) {
                        escolhido = d;
                    }
                }
                if (escolhido == null) {
                    resultado.erro("Selecione um departamento válido.");
                    return;
                }
                try {
                    JSONObject corpo = new JSONObject()
                            .put("nome", nome)
                            .put("email", email)
                            .put("idDepartamento", escolhido.getId())
                            .put("senha", senha);
                    api.post("/registrar", corpo,
                            resposta -> Conversor.funcionario(new JSONObject(resposta)), resultado);
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
    public void listarDepartamentos(Resultado<List<Departamento>> resultado) {
        api.get("/publico/departamentos",
                resposta -> Conversor.lista(new JSONArray(resposta), Conversor::departamentoResumo),
                resultado);
    }
}
