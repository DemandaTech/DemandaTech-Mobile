package edu.udesc.demandatech.data.demo;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.udesc.demandatech.data.CadastroRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Cadastro;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.ItemCadastro;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;

// Simula a API de cadastros em memória.
public class CadastroRepositoryDemo implements CadastroRepository {

    private final Sessao sessao;
    private final Handler principal = new Handler(Looper.getMainLooper());

    public CadastroRepositoryDemo(Sessao sessao) {
        this.sessao = sessao;
    }

    @Override
    public void listar(Cadastro cadastro, String busca, int pagina,
                       Resultado<Pagina<ItemCadastro>> resultado) {
        String texto = busca.toLowerCase(Locale.ROOT);
        List<ItemCadastro> filtrados = new ArrayList<>();
        for (ItemCadastro item : registros(cadastro)) {
            String alvo = item.getTitulo() + " " + item.getDetalhe();
            if (alvo.toLowerCase(Locale.ROOT).contains(texto)) {
                filtrados.add(item);
            }
        }
        int inicio = Math.min((pagina - 1) * Pagina.TAMANHO, filtrados.size());
        int fim = Math.min(inicio + Pagina.TAMANHO, filtrados.size());
        List<ItemCadastro> itens = new ArrayList<>(filtrados.subList(inicio, fim));
        entregar(resultado, new Pagina<>(itens, pagina, filtrados.size()));
    }

    @Override
    public void buscarFuncionario(String matricula, Resultado<Usuario> resultado) {
        Usuario funcionario = DadosDemo.porMatricula(matricula);
        if (funcionario == null) {
            falhar(resultado, "Funcionário não encontrado.");
        } else {
            entregar(resultado, funcionario);
        }
    }

    @Override
    public void salvarFuncionario(String matricula, String nome, String email, Perfil perfil,
                                  String departamento, String senha,
                                  Resultado<Usuario> resultado) {
        Usuario funcionario = matricula == null ? null : DadosDemo.porMatricula(matricula);
        if (funcionario == null) {
            funcionario = new Usuario(DadosDemo.novaMatricula(), nome, email, perfil,
                    departamento);
            DadosDemo.USUARIOS.add(funcionario);
        }
        funcionario.setNome(nome);
        funcionario.setEmail(email);
        funcionario.setDepartamento(departamento);
        funcionario.setPerfil(perfil);
        if (!senha.isEmpty()) {
            DadosDemo.SENHAS.put(funcionario.getMatricula(), senha);
        }
        entregar(resultado, funcionario);
    }

    @Override
    public void removerFuncionario(String matricula, Resultado<Void> resultado) {
        Usuario funcionario = DadosDemo.porMatricula(matricula);
        if (funcionario == null || emUso(funcionario)) {
            falhar(resultado, "Funcionário vinculado a demandas ou a um departamento. "
                    + "Remova os vínculos antes.");
            return;
        }
        DadosDemo.USUARIOS.remove(funcionario);
        entregar(resultado, null);
    }

    @Override
    public void listarDepartamentos(Resultado<List<Departamento>> resultado) {
        entregar(resultado, new ArrayList<>(DadosDemo.DEPARTAMENTOS));
    }

    @Override
    public void buscarDepartamento(long id, Resultado<Departamento> resultado) {
        Departamento departamento = departamentoPorId(id);
        if (departamento == null) {
            falhar(resultado, "Departamento não encontrado.");
        } else {
            entregar(resultado, departamento);
        }
    }

    @Override
    public void salvarDepartamento(long id, String descricao, Usuario gerente,
                                   Resultado<Departamento> resultado) {
        Departamento departamento = departamentoPorId(id);
        if (departamento == null) {
            departamento = new Departamento(DadosDemo.novoIdDepartamento(), descricao, gerente);
            DadosDemo.DEPARTAMENTOS.add(departamento);
        } else {
            renomearNosFuncionarios(departamento.getDescricao(), descricao);
            departamento.setDescricao(descricao);
            departamento.setGerente(gerente);
        }
        entregar(resultado, departamento);
    }

    @Override
    public void removerDepartamento(long id, Resultado<Void> resultado) {
        Departamento departamento = departamentoPorId(id);
        if (departamento == null || emUso(departamento)) {
            falhar(resultado, "Departamento com funcionários ou tipos de demanda vinculados.");
            return;
        }
        DadosDemo.DEPARTAMENTOS.remove(departamento);
        entregar(resultado, null);
    }

    @Override
    public void buscarTipo(long id, Resultado<TipoDemanda> resultado) {
        TipoDemanda tipo = tipoPorId(id);
        if (tipo == null) {
            falhar(resultado, "Tipo de demanda não encontrado.");
        } else {
            entregar(resultado, tipo);
        }
    }

    @Override
    public void salvarTipo(long id, String descricao, Departamento departamento,
                           Resultado<TipoDemanda> resultado) {
        TipoDemanda tipo = tipoPorId(id);
        if (tipo == null) {
            tipo = new TipoDemanda(DadosDemo.novoIdTipo(), descricao, departamento);
            DadosDemo.TIPOS.add(tipo);
        } else {
            tipo.setDescricao(descricao);
            tipo.setDepartamento(departamento);
        }
        entregar(resultado, tipo);
    }

    @Override
    public void removerTipo(long id, Resultado<Void> resultado) {
        TipoDemanda tipo = tipoPorId(id);
        for (Demanda demanda : DadosDemo.DEMANDAS) {
            if (demanda.getTipo() == tipo) {
                falhar(resultado, "Tipo usado em demandas não pode ser removido.");
                return;
            }
        }
        DadosDemo.TIPOS.remove(tipo);
        entregar(resultado, null);
    }

    // O Gerente só enxerga os funcionários do próprio departamento.
    private List<? extends ItemCadastro> registros(Cadastro cadastro) {
        if (cadastro == Cadastro.DEPARTAMENTOS) {
            return DadosDemo.DEPARTAMENTOS;
        }
        if (cadastro == Cadastro.TIPOS) {
            return DadosDemo.TIPOS;
        }
        Usuario logado = sessao.getUsuario();
        if (logado.isAdministrador()) {
            return DadosDemo.USUARIOS;
        }
        List<Usuario> doDepartamento = new ArrayList<>();
        for (Usuario funcionario : DadosDemo.USUARIOS) {
            if (funcionario.getDepartamento().equals(logado.getDepartamento())) {
                doDepartamento.add(funcionario);
            }
        }
        return doDepartamento;
    }

    private boolean emUso(Usuario funcionario) {
        for (Departamento departamento : DadosDemo.DEPARTAMENTOS) {
            if (departamento.getGerente() == funcionario) {
                return true;
            }
        }
        for (Demanda demanda : DadosDemo.DEMANDAS) {
            if (demanda.foiCriadaPor(funcionario) || demanda.eResponsavel(funcionario)) {
                return true;
            }
        }
        return false;
    }

    private boolean emUso(Departamento departamento) {
        for (Usuario funcionario : DadosDemo.USUARIOS) {
            if (funcionario.getDepartamento().equals(departamento.getDescricao())) {
                return true;
            }
        }
        for (TipoDemanda tipo : DadosDemo.TIPOS) {
            if (tipo.getDepartamento() == departamento) {
                return true;
            }
        }
        return false;
    }

    // Os funcionários guardam o nome do departamento, então acompanham a mudança.
    private void renomearNosFuncionarios(String antigo, String novo) {
        for (Usuario funcionario : DadosDemo.USUARIOS) {
            if (funcionario.getDepartamento().equals(antigo)) {
                funcionario.setDepartamento(novo);
            }
        }
    }

    private Departamento departamentoPorId(long id) {
        for (Departamento departamento : DadosDemo.DEPARTAMENTOS) {
            if (departamento.getId() == id) {
                return departamento;
            }
        }
        return null;
    }

    private TipoDemanda tipoPorId(long id) {
        for (TipoDemanda tipo : DadosDemo.TIPOS) {
            if (tipo.getId() == id) {
                return tipo;
            }
        }
        return null;
    }

    private <T> void entregar(Resultado<T> resultado, T dados) {
        principal.post(() -> resultado.sucesso(dados));
    }

    private <T> void falhar(Resultado<T> resultado, String mensagem) {
        principal.post(() -> resultado.erro(mensagem));
    }
}
