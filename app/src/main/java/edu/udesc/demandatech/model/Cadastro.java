package edu.udesc.demandatech.model;

public enum Cadastro {
    FUNCIONARIOS("Funcionários", "Novo funcionário", "Buscar por matrícula ou nome"),
    DEPARTAMENTOS("Departamentos", "Novo departamento", "Buscar por ID ou descrição"),
    TIPOS("Tipos", "Novo tipo", "Buscar por ID ou descrição");

    private final String rotulo;
    private final String acaoNovo;
    private final String dicaBusca;

    Cadastro(String rotulo, String acaoNovo, String dicaBusca) {
        this.rotulo = rotulo;
        this.acaoNovo = acaoNovo;
        this.dicaBusca = dicaBusca;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getAcaoNovo() {
        return acaoNovo;
    }

    public String getDicaBusca() {
        return dicaBusca;
    }
}
