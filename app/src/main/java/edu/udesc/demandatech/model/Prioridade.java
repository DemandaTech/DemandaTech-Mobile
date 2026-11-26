package edu.udesc.demandatech.model;

public enum Prioridade {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    URGENTE("Urgente");

    private final String rotulo;

    Prioridade(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static Prioridade porRotulo(String rotulo) {
        for (Prioridade prioridade : values()) {
            if (prioridade.rotulo.equals(rotulo)) {
                return prioridade;
            }
        }
        return null;
    }
}
