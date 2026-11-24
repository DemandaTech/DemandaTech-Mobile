package edu.udesc.demandatech.data.demo;

import java.util.ArrayList;
import java.util.List;

import edu.udesc.demandatech.data.AuthRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.Autenticacao;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.Usuario;

public class AuthRepositoryDemo implements AuthRepository {

    @Override
    public void entrar(String matricula, String senha, Resultado<Autenticacao> resultado) {
        Usuario usuario = DadosDemo.porMatricula(matricula);
        String senhaCorreta = DadosDemo.SENHAS.get(matricula);
        if (usuario != null && senha.equals(senhaCorreta)) {
            resultado.sucesso(new Autenticacao(usuario, "token-demo"));
        } else {
            resultado.erro("Matrícula ou senha inválida.");
        }
    }

    @Override
    public void criarConta(String nome, String email, String departamento, String senha,
                           Resultado<Usuario> resultado) {
        for (Usuario existente : DadosDemo.USUARIOS) {
            if (existente.getEmail().equalsIgnoreCase(email)) {
                resultado.erro("Já existe uma conta com este e-mail.");
                return;
            }
        }
        Usuario novo = new Usuario(DadosDemo.novaMatricula(), nome, email, Perfil.USUARIO,
                departamento);
        DadosDemo.USUARIOS.add(novo);
        DadosDemo.SENHAS.put(novo.getMatricula(), senha);
        resultado.sucesso(novo);
    }

    @Override
    public void listarDepartamentos(Resultado<List<Departamento>> resultado) {
        resultado.sucesso(new ArrayList<>(DadosDemo.DEPARTAMENTOS));
    }
}
