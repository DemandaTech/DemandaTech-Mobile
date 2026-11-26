package edu.udesc.demandatech.model;

public class Autenticacao {

    private final Usuario usuario;
    private final String token;

    public Autenticacao(Usuario usuario, String token) {
        this.usuario = usuario;
        this.token = token;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getToken() {
        return token;
    }
}
