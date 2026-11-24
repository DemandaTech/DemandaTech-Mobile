package edu.udesc.demandatech.data.demo;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import edu.udesc.demandatech.data.RelatorioRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.model.ItemRelatorio;
import edu.udesc.demandatech.model.Relatorios;
import edu.udesc.demandatech.model.Status;
import edu.udesc.demandatech.model.Usuario;
import edu.udesc.demandatech.model.VisaoGeral;

// Calcula os relatórios a partir do armazenamento em memória.
public class RelatorioRepositoryDemo implements RelatorioRepository {

    private static final Locale BRASIL = new Locale("pt", "BR");
    private static final int RECENTES = 4;
    private static final int TAMANHO_RANKING = 10;

    private final Sessao sessao;
    private final Handler principal = new Handler(Looper.getMainLooper());

    public RelatorioRepositoryDemo(Sessao sessao) {
        this.sessao = sessao;
    }

    @Override
    public void visaoGeral(Resultado<VisaoGeral> resultado) {
        Usuario logado = sessao.getUsuario();
        List<Demanda> visiveis = new ArrayList<>();
        for (Demanda demanda : DadosDemo.DEMANDAS) {
            if (DadosDemo.podeVer(logado, demanda)) {
                visiveis.add(demanda);
            }
        }

        int total = visiveis.size();
        int resolvidas = contar(visiveis, Status.RESOLVIDA);
        double taxa = total == 0 ? 0 : resolvidas * 100.0 / total;
        List<Demanda> recentes = new ArrayList<>(visiveis.subList(0, Math.min(RECENTES, total)));
        List<ItemRelatorio> ranking = logado.isAdministrador()
                ? filaDosTecnicos()
                : demandasDoDepartamento(logado.getDepartamento());

        VisaoGeral visao = new VisaoGeral(total, contar(visiveis, Status.ABERTA),
                contar(visiveis, Status.AGUARDANDO), taxa, recentes, ranking);
        principal.post(() -> resultado.sucesso(visao));
    }

    @Override
    public void relatorios(Resultado<Relatorios> resultado) {
        List<Demanda> demandas = DadosDemo.DEMANDAS;
        int emAndamento = contar(demandas, Status.ABERTA) + contar(demandas, Status.AGUARDANDO);
        Relatorios relatorios = new Relatorios(contar(demandas, Status.RESOLVIDA),
                DadosDemo.ESTORNOS.size(), emAndamento, porDepartamento(),
                estornosPorResponsavel(), maisProdutivos());
        principal.post(() -> resultado.sucesso(relatorios));
    }

    // Administrador: demandas ainda não encerradas por técnico da TI.
    private List<ItemRelatorio> filaDosTecnicos() {
        List<ItemRelatorio> fila = new ArrayList<>();
        for (Usuario tecnico : DadosDemo.tecnicos()) {
            int pendentes = 0;
            for (Demanda demanda : DadosDemo.DEMANDAS) {
                if (demanda.eResponsavel(tecnico) && !demanda.getStatus().isFinal()) {
                    pendentes++;
                }
            }
            fila.add(new ItemRelatorio(tecnico.getNome(), pendentes, null));
        }
        return maiores(fila);
    }

    // Gerente: demandas abertas por cada funcionário do departamento dele.
    private List<ItemRelatorio> demandasDoDepartamento(String departamento) {
        List<ItemRelatorio> porFuncionario = new ArrayList<>();
        for (Usuario funcionario : DadosDemo.USUARIOS) {
            if (!funcionario.getDepartamento().equals(departamento)) {
                continue;
            }
            int criadas = 0;
            for (Demanda demanda : DadosDemo.DEMANDAS) {
                if (demanda.foiCriadaPor(funcionario)) {
                    criadas++;
                }
            }
            porFuncionario.add(new ItemRelatorio(funcionario.getNome(), criadas, null));
        }
        return maiores(porFuncionario);
    }

    // Agrupa pelo departamento de quem abriu a demanda, com a participação no total.
    private List<ItemRelatorio> porDepartamento() {
        int total = DadosDemo.DEMANDAS.size();
        List<ItemRelatorio> itens = new ArrayList<>();
        for (Departamento departamento : DadosDemo.DEPARTAMENTOS) {
            int quantidade = 0;
            for (Demanda demanda : DadosDemo.DEMANDAS) {
                if (demanda.getCriador().getDepartamento().equals(departamento.getDescricao())) {
                    quantidade++;
                }
            }
            double participacao = total == 0 ? 0 : quantidade * 100.0 / total;
            String detalhe = String.format(BRASIL, "%.1f%% das demandas", participacao);
            itens.add(new ItemRelatorio(departamento.getDescricao(), quantidade, detalhe));
        }
        return maiores(itens);
    }

    private List<ItemRelatorio> estornosPorResponsavel() {
        List<ItemRelatorio> itens = new ArrayList<>();
        for (Usuario tecnico : DadosDemo.tecnicos()) {
            int estornos = estornosDe(tecnico);
            if (estornos > 0) {
                itens.add(new ItemRelatorio(tecnico.getNome(), estornos, null));
            }
        }
        return maiores(itens);
    }

    // Classifica os técnicos pelas demandas resolvidas.
    private List<ItemRelatorio> maisProdutivos() {
        List<ItemRelatorio> itens = new ArrayList<>();
        for (Usuario tecnico : DadosDemo.tecnicos()) {
            int resolvidas = 0;
            int pendentes = 0;
            for (Demanda demanda : DadosDemo.DEMANDAS) {
                if (!demanda.eResponsavel(tecnico)) {
                    continue;
                }
                if (demanda.getStatus() == Status.RESOLVIDA) {
                    resolvidas++;
                } else if (!demanda.getStatus().isFinal()) {
                    pendentes++;
                }
            }
            String detalhe = pendentes + " pendências · " + estornosDe(tecnico) + " estornos";
            itens.add(new ItemRelatorio(tecnico.getNome(), resolvidas, detalhe));
        }
        return maiores(itens);
    }

    private int estornosDe(Usuario tecnico) {
        int estornos = 0;
        for (Estorno estorno : DadosDemo.ESTORNOS) {
            if (estorno.getDemanda().eResponsavel(tecnico)) {
                estornos++;
            }
        }
        return estornos;
    }

    private int contar(List<Demanda> demandas, Status status) {
        int quantidade = 0;
        for (Demanda demanda : demandas) {
            if (demanda.getStatus() == status) {
                quantidade++;
            }
        }
        return quantidade;
    }

    // Ordena do maior para o menor valor e limita ao tamanho do ranking.
    private List<ItemRelatorio> maiores(List<ItemRelatorio> itens) {
        Collections.sort(itens, (a, b) -> b.getValor() - a.getValor());
        return new ArrayList<>(itens.subList(0, Math.min(TAMANHO_RANKING, itens.size())));
    }
}
