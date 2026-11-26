package edu.udesc.demandatech.data;

public interface Resultado<T> {

    void sucesso(T dados);

    void erro(String mensagem);
}
