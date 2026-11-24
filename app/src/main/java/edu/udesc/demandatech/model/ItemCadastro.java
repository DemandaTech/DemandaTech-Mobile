package edu.udesc.demandatech.model;

// O que cada registro mostra no cartão das listas de Cadastros.
public interface ItemCadastro {

    String getTitulo();

    String getDetalhe();

    // Etiqueta opcional ao lado do título; null quando não há.
    String getMarcador();
}
