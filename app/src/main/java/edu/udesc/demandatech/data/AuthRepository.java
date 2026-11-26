package edu.udesc.demandatech.data;

import java.util.List;

import edu.udesc.demandatech.model.Autenticacao;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Usuario;

public interface AuthRepository {

    void entrar(String matricula, String senha, Resultado<Autenticacao> resultado);

    void criarConta(String nome, String email, String departamento, String senha,
                    Resultado<Usuario> resultado);

    void listarDepartamentos(Resultado<List<Departamento>> resultado);
}
