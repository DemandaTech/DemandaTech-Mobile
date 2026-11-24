package edu.udesc.demandatech.data.demo;

import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.udesc.demandatech.data.DemandaRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.FiltroDemanda;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.Prioridade;
import edu.udesc.demandatech.model.ResumoDemandas;
import edu.udesc.demandatech.model.Status;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;

// Simula a API em memória, inclusive as regras de visibilidade por perfil.
public class DemandaRepositoryDemo implements DemandaRepository {

    private final Sessao sessao;
    private final Handler principal = new Handler(Looper.getMainLooper());

    public DemandaRepositoryDemo(Sessao sessao) {
        this.sessao = sessao;
    }

    @Override
    public void listar(FiltroDemanda filtro, int pagina, Resultado<Pagina<Demanda>> resultado) {
        List<Demanda> filtradas = new ArrayList<>();
        for (Demanda demanda : visiveis()) {
            if (atende(demanda, filtro)) {
                filtradas.add(demanda);
            }
        }
        int inicio = Math.min((pagina - 1) * Pagina.TAMANHO, filtradas.size());
        int fim = Math.min(inicio + Pagina.TAMANHO, filtradas.size());
        List<Demanda> itens = new ArrayList<>(filtradas.subList(inicio, fim));
        entregar(resultado, new Pagina<>(itens, pagina, filtradas.size()));
    }

    @Override
    public void resumo(Resultado<ResumoDemandas> resultado) {
        int total = 0;
        int abertas = 0;
        int aguardando = 0;
        int resolvidas = 0;
        for (Demanda demanda : visiveis()) {
            total++;
            if (demanda.getStatus() == Status.ABERTA) abertas++;
            if (demanda.getStatus() == Status.AGUARDANDO) aguardando++;
            if (demanda.getStatus() == Status.RESOLVIDA) resolvidas++;
        }
        entregar(resultado, new ResumoDemandas(total, abertas, aguardando, resolvidas));
    }

    @Override
    public void buscar(int id, Resultado<Demanda> resultado) {
        Demanda demanda = DadosDemo.demandaPorId(id);
        if (demanda == null) {
            falhar(resultado, "Demanda não encontrada.");
        } else {
            entregar(resultado, demanda);
        }
    }

    @Override
    public void criar(String titulo, String descricao, Prioridade prioridade, TipoDemanda tipo,
                      List<Usuario> envolvidos, Resultado<Demanda> resultado) {
        List<Usuario> tecnicos = DadosDemo.tecnicos();
        if (tecnicos.isEmpty()) {
            falhar(resultado, "Não há técnico disponível para atender a demanda.");
            return;
        }
        int id = DadosDemo.DEMANDAS.isEmpty() ? 1 : DadosDemo.DEMANDAS.get(0).getId() + 1;
        Usuario tecnico = tecnicos.get(id % tecnicos.size());
        String agora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(new Date());
        List<String> nomes = new ArrayList<>();
        for (Usuario envolvido : envolvidos) {
            nomes.add(envolvido.getNome());
        }

        Demanda nova = new Demanda(id, titulo, descricao, Status.ABERTA, prioridade, tipo,
                DadosDemo.porMatricula(sessao.getUsuario().getMatricula()), tecnico, agora, nomes);
        DadosDemo.DEMANDAS.add(0, nova);
        entregar(resultado, nova);
    }

    @Override
    public void editar(int id, String titulo, String descricao, Prioridade prioridade,
                       TipoDemanda tipo, Resultado<Demanda> resultado) {
        Demanda demanda = DadosDemo.demandaPorId(id);
        if (demanda == null) {
            falhar(resultado, "Demanda não encontrada.");
            return;
        }
        demanda.setTitulo(titulo);
        demanda.setDescricao(descricao);
        demanda.setPrioridade(prioridade);
        demanda.setTipo(tipo);
        entregar(resultado, demanda);
    }

    @Override
    public void alterarStatus(int id, Status status, String justificativa,
                              Resultado<Demanda> resultado) {
        Demanda demanda = DadosDemo.demandaPorId(id);
        if (demanda == null) {
            falhar(resultado, "Demanda não encontrada.");
            return;
        }
        if (!demanda.getStatus().podeAvancarPara(status)) {
            falhar(resultado, "O status de uma demanda não pode regredir.");
            return;
        }
        demanda.setStatus(status);
        demanda.setJustificativa(status == Status.RESOLVIDA ? justificativa : null);
        entregar(resultado, demanda);
    }

    @Override
    public void excluir(int id, Resultado<Void> resultado) {
        DadosDemo.DEMANDAS.remove(DadosDemo.demandaPorId(id));
        DadosDemo.ESTORNOS.removeIf(estorno -> estorno.getDemanda().getId() == id);
        entregar(resultado, null);
    }

    @Override
    public void listarTipos(Resultado<List<TipoDemanda>> resultado) {
        entregar(resultado, new ArrayList<>(DadosDemo.TIPOS));
    }

    @Override
    public void listarFuncionarios(Resultado<List<Usuario>> resultado) {
        entregar(resultado, new ArrayList<>(DadosDemo.USUARIOS));
    }

    private List<Demanda> visiveis() {
        Usuario usuario = sessao.getUsuario();
        List<Demanda> visiveis = new ArrayList<>();
        for (Demanda demanda : DadosDemo.DEMANDAS) {
            if (DadosDemo.podeVer(usuario, demanda)) {
                visiveis.add(demanda);
            }
        }
        return visiveis;
    }

    private boolean atende(Demanda demanda, FiltroDemanda filtro) {
        String texto = filtro.texto.toLowerCase(Locale.ROOT);
        String alvo = ("#" + demanda.getId() + " " + demanda.getTitulo()).toLowerCase(Locale.ROOT);
        return alvo.contains(texto)
                && (filtro.status == null || filtro.status == demanda.getStatus())
                && (filtro.prioridade == null || filtro.prioridade == demanda.getPrioridade())
                && (filtro.idTipo == null || filtro.idTipo == demanda.getTipo().getId());
    }

    private <T> void entregar(Resultado<T> resultado, T dados) {
        principal.post(() -> resultado.sucesso(dados));
    }

    private <T> void falhar(Resultado<T> resultado, String mensagem) {
        principal.post(() -> resultado.erro(mensagem));
    }
}
