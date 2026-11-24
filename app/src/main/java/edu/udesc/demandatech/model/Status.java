package edu.udesc.demandatech.model;

// Mesmos valores do enum Status do backend.
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

    // Resolvida e Cancelada encerram a demanda.
    public boolean isFinal() {
        return this == RESOLVIDA || this == CANCELADA;
    }

    // O status só avança: Aberta, depois Aguardando, depois Resolvida ou Cancelada.
    public boolean podeAvancarPara(Status novo) {
        return !isFinal() && novo.ordinal() > ordinal();
    }
}
