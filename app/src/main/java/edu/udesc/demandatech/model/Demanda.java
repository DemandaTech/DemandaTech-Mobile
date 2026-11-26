package edu.udesc.demandatech.model;

import java.util.List;

public class Demanda {

    private final int id;
    private String titulo;
    private String descricao;
    private Status status;
    private Prioridade prioridade;
    private TipoDemanda tipo;
    private final Usuario criador;
    private final Usuario responsavel;
    private final String abertura;
    private final List<String> envolvidos;
    private String justificativa;

    public Demanda(int id, String titulo, String descricao, Status status, Prioridade prioridade,
                   TipoDemanda tipo, Usuario criador, Usuario responsavel, String abertura,
                   List<String> envolvidos) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.prioridade = prioridade;
        this.tipo = tipo;
        this.criador = criador;
        this.responsavel = responsavel;
        this.abertura = abertura;
        this.envolvidos = envolvidos;
    }

    public boolean foiCriadaPor(Usuario usuario) {
        return pertenceA(criador, usuario);
    }

    public boolean podeEditar(Usuario usuario) {
        return foiCriadaPor(usuario);
    }

    public boolean podeExcluir(Usuario usuario) {
        return foiCriadaPor(usuario);
    }

    public boolean podeRegistrarEstorno(Usuario usuario) {
        return foiCriadaPor(usuario) && status == Status.RESOLVIDA;
    }

    public boolean podeAlterarStatus(Usuario usuario) {
        return pertenceA(responsavel, usuario) && !status.isFinal();
    }

    public boolean eResponsavel(Usuario usuario) {
        return pertenceA(responsavel, usuario);
    }

    private boolean pertenceA(Usuario dono, Usuario usuario) {
        return dono != null && dono.getMatricula().equals(usuario.getMatricula());
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public TipoDemanda getTipo() {
        return tipo;
    }

    public void setTipo(TipoDemanda tipo) {
        this.tipo = tipo;
    }

    public Usuario getCriador() {
        return criador;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public String getAbertura() {
        return abertura;
    }

    public List<String> getEnvolvidos() {
        return envolvidos;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }
}
