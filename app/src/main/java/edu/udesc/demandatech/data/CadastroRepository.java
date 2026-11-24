package edu.udesc.demandatech.data;

import java.util.List;

import edu.udesc.demandatech.model.Cadastro;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.ItemCadastro;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;

// Funcionários, departamentos e tipos de demanda.
// Nos métodos "salvar", matrícula nula ou id zero significam um registro novo.
public interface CadastroRepository {

    void listar(Cadastro cadastro, String busca, int pagina,
                Resultado<Pagina<ItemCadastro>> resultado);

    void buscarFuncionario(String matricula, Resultado<Usuario> resultado);

    // Na edição, senha vazia mantém a senha atual.
    void salvarFuncionario(String matricula, String nome, String email, Perfil perfil,
                           String departamento, String senha, Resultado<Usuario> resultado);

    void removerFuncionario(String matricula, Resultado<Void> resultado);

    void listarDepartamentos(Resultado<List<Departamento>> resultado);

    void buscarDepartamento(long id, Resultado<Departamento> resultado);

    void salvarDepartamento(long id, String descricao, Usuario gerente,
                            Resultado<Departamento> resultado);

    void removerDepartamento(long id, Resultado<Void> resultado);

    void buscarTipo(long id, Resultado<TipoDemanda> resultado);

    void salvarTipo(long id, String descricao, Departamento departamento,
                    Resultado<TipoDemanda> resultado);

    void removerTipo(long id, Resultado<Void> resultado);
}
