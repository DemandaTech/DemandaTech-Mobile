package edu.udesc.demandatech.data;

import java.util.List;

import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.model.Pagina;

public interface EstornoRepository {

    void listar(String busca, int pagina, Resultado<Pagina<Estorno>> resultado);

    void listarDaDemanda(int idDemanda, Resultado<List<Estorno>> resultado);

    void buscar(int id, Resultado<Estorno> resultado);

    void registrar(int idDemanda, String justificativa, Resultado<Estorno> resultado);

    void editar(int id, String justificativa, Resultado<Estorno> resultado);

    void remover(int id, Resultado<Void> resultado);
}
