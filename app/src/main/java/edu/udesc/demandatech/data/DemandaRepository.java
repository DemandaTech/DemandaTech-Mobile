package edu.udesc.demandatech.data;

import java.util.List;

import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.FiltroDemanda;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.Prioridade;
import edu.udesc.demandatech.model.ResumoDemandas;
import edu.udesc.demandatech.model.Status;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;

// O backend devolve apenas o que o perfil logado pode ver.
public interface DemandaRepository {

    void listar(FiltroDemanda filtro, int pagina, Resultado<Pagina<Demanda>> resultado);

    void resumo(Resultado<ResumoDemandas> resultado);

    void buscar(int id, Resultado<Demanda> resultado);

    void criar(String titulo, String descricao, Prioridade prioridade, TipoDemanda tipo,
               List<Usuario> envolvidos, Resultado<Demanda> resultado);

    void editar(int id, String titulo, String descricao, Prioridade prioridade, TipoDemanda tipo,
                Resultado<Demanda> resultado);

    // O status só avança. A justificativa é opcional e vale apenas para Resolvida.
    void alterarStatus(int id, Status status, String justificativa, Resultado<Demanda> resultado);

    void excluir(int id, Resultado<Void> resultado);

    void listarTipos(Resultado<List<TipoDemanda>> resultado);

    void listarFuncionarios(Resultado<List<Usuario>> resultado);
}
