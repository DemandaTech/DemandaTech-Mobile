package edu.udesc.demandatech.data;

import java.util.List;

import edu.udesc.demandatech.model.Autenticacao;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Usuario;

public interface AuthRepository {

    void entrar(String matricula, String senha, Resultado<Autenticacao> resultado);

    // Cadastro feito pela própria pessoa na tela de login; a conta nasce com perfil Usuário.
    void criarConta(String nome, String email, String departamento, String senha,
                    Resultado<Usuario> resultado);

    // Departamentos para o formulário de criação de conta, sem exigir login.
    void listarDepartamentos(Resultado<List<Departamento>> resultado);
}
