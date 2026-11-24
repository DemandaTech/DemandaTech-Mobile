package edu.udesc.demandatech.model;

import java.util.Locale;

public class Usuario implements ItemCadastro {

    private final String matricula;
    private String nome;
    private String email;
    private Perfil perfil;
    private String departamento;

    public Usuario(String matricula, String nome, Perfil perfil, String departamento) {
        this(matricula, nome, "", perfil, departamento);
    }

    public Usuario(String matricula, String nome, String email, Perfil perfil,
                   String departamento) {
        this.matricula = matricula;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.departamento = departamento;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public boolean isAdministrador() {
        return perfil == Perfil.ADMINISTRADOR;
    }

    public String getPrimeiroNome() {
        return nome.trim().split(" ")[0];
    }

    // Primeira letra do primeiro e do último nome, como "LA" para Lucas Arins.
    public String getIniciais() {
        String[] partes = nome.trim().split(" ");
        String iniciais = partes[0].substring(0, 1);
        if (partes.length > 1) {
            iniciais += partes[partes.length - 1].substring(0, 1);
        }
        return iniciais.toUpperCase(Locale.ROOT);
    }

    @Override
    public String getTitulo() {
        return nome;
    }

    @Override
    public String getDetalhe() {
        return matricula + " · " + departamento;
    }

    @Override
    public String getMarcador() {
        return perfil.getRotulo();
    }

    // Texto exibido nas listas de seleção.
    @Override
    public String toString() {
        return nome + " · " + matricula;
    }
}
