package edu.udesc.demandatech.data.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;

// Armazenamento em memória usado enquanto a API não está disponível.
// Começa vazio e perde tudo ao fechar o app.
final class DadosDemo {

    static final List<Usuario> USUARIOS = new ArrayList<>();
    static final List<Demanda> DEMANDAS = new ArrayList<>();
    static final List<Estorno> ESTORNOS = new ArrayList<>();
    static final List<Departamento> DEPARTAMENTOS = new ArrayList<>();
    static final List<TipoDemanda> TIPOS = new ArrayList<>();
    // Senha de cada usuário, pela matrícula.
    static final Map<String, String> SENHAS = new HashMap<>();

    private DadosDemo() {
    }

    static Usuario porMatricula(String matricula) {
        for (Usuario usuario : USUARIOS) {
            if (usuario.getMatricula().equals(matricula)) {
                return usuario;
            }
        }
        return null;
    }

    // Técnicos são os usuários com perfil Funcionário.
    static List<Usuario> tecnicos() {
        List<Usuario> tecnicos = new ArrayList<>();
        for (Usuario usuario : USUARIOS) {
            if (usuario.getPerfil() == Perfil.FUNCIONARIO) {
                tecnicos.add(usuario);
            }
        }
        return tecnicos;
    }

    static String novaMatricula() {
        long maior = 0;
        for (Usuario usuario : USUARIOS) {
            maior = Math.max(maior, Long.parseLong(usuario.getMatricula()));
        }
        return String.valueOf(maior + 1);
    }

    static long novoIdDepartamento() {
        long maior = 0;
        for (Departamento departamento : DEPARTAMENTOS) {
            maior = Math.max(maior, departamento.getId());
        }
        return maior + 1;
    }

    static long novoIdTipo() {
        long maior = 0;
        for (TipoDemanda tipo : TIPOS) {
            maior = Math.max(maior, tipo.getId());
        }
        return maior + 1;
    }

    static Demanda demandaPorId(int id) {
        for (Demanda demanda : DEMANDAS) {
            if (demanda.getId() == id) {
                return demanda;
            }
        }
        return null;
    }

    // Regra de visibilidade por perfil, que na versão real é aplicada pelo backend.
    static boolean podeVer(Usuario usuario, Demanda demanda) {
        switch (usuario.getPerfil()) {
            case ADMINISTRADOR:
                return true;
            case GERENTE:
                return demanda.getCriador().getDepartamento().equals(usuario.getDepartamento());
            case FUNCIONARIO:
                return demanda.eResponsavel(usuario) || demanda.foiCriadaPor(usuario);
            default:
                return demanda.foiCriadaPor(usuario)
                        || demanda.getEnvolvidos().contains(usuario.getNome());
        }
    }
}
