package edu.udesc.demandatech.model;

public enum Status {
    ABERTA("Aberta"),
    AGUARDANDO("Aguardando"),
    RESOLVIDA("Resolvida"),
    CANCELADA("Cancelada");

    private final String rotulo;

    Status(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean isFinal() {
        return this == RESOLVIDA || this == CANCELADA;
    }

    public boolean podeAvancarPara(Status novo) {
        return !isFinal() && novo.ordinal() > ordinal();
    }
}
