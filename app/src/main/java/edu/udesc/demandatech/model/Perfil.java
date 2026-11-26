package edu.udesc.demandatech.model;

public enum Perfil {
    ADMINISTRADOR("Administrador"),
    GERENTE("Gerente"),
    FUNCIONARIO("Funcionário"),
    USUARIO("Usuário");

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static Perfil porRotulo(String rotulo) {
        for (Perfil perfil : values()) {
            if (perfil.rotulo.equals(rotulo)) {
                return perfil;
            }
        }
        return null;
    }

    public boolean isGestor() {
        return this == ADMINISTRADOR || this == GERENTE;
    }
}
