package edu.udesc.demandatech.data;

// Retorno assíncrono dos repositórios, sempre entregue na thread principal.
public interface Resultado<T> {

    void sucesso(T dados);

    void erro(String mensagem);
}
