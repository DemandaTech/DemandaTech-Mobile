package edu.udesc.demandatech.data.demo;

import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.udesc.demandatech.data.EstornoRepository;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.Usuario;

// Simula a API de estornos em memória.
public class EstornoRepositoryDemo implements EstornoRepository {

    private final Sessao sessao;
    private final Handler principal = new Handler(Looper.getMainLooper());

    public EstornoRepositoryDemo(Sessao sessao) {
        this.sessao = sessao;
    }

    @Override
    public void listar(String busca, int pagina, Resultado<Pagina<Estorno>> resultado) {
        Usuario usuario = sessao.getUsuario();
        List<Estorno> filtrados = new ArrayList<>();
        for (Estorno estorno : DadosDemo.ESTORNOS) {
            if (DadosDemo.podeVer(usuario, estorno.getDemanda()) && atende(estorno, busca)) {
                filtrados.add(estorno);
            }
        }
        int inicio = Math.min((pagina - 1) * Pagina.TAMANHO, filtrados.size());
        int fim = Math.min(inicio + Pagina.TAMANHO, filtrados.size());
        List<Estorno> itens = new ArrayList<>(filtrados.subList(inicio, fim));
        entregar(resultado, new Pagina<>(itens, pagina, filtrados.size()));
    }

    @Override
    public void listarDaDemanda(int idDemanda, Resultado<List<Estorno>> resultado) {
        List<Estorno> daDemanda = new ArrayList<>();
        for (Estorno estorno : DadosDemo.ESTORNOS) {
            if (estorno.getDemanda().getId() == idDemanda) {
                daDemanda.add(estorno);
            }
        }
        entregar(resultado, daDemanda);
    }

    @Override
    public void buscar(int id, Resultado<Estorno> resultado) {
        Estorno estorno = porId(id);
        if (estorno == null) {
            falhar(resultado, "Estorno não encontrado.");
        } else {
            entregar(resultado, estorno);
        }
    }

    @Override
    public void registrar(int idDemanda, String justificativa, Resultado<Estorno> resultado) {
        Demanda demanda = DadosDemo.demandaPorId(idDemanda);
        if (demanda == null) {
            falhar(resultado, "Demanda não encontrada.");
            return;
        }
        if (!demanda.podeRegistrarEstorno(sessao.getUsuario())) {
            falhar(resultado, "O estorno é registrado por quem criou a demanda, depois de resolvida.");
            return;
        }
        int id = DadosDemo.ESTORNOS.isEmpty() ? 1 : DadosDemo.ESTORNOS.get(0).getId() + 1;
        String hoje = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(new Date());
        Estorno novo = new Estorno(id, justificativa, hoje, demanda);
        DadosDemo.ESTORNOS.add(0, novo);
        entregar(resultado, novo);
    }

    @Override
    public void editar(int id, String justificativa, Resultado<Estorno> resultado) {
        Estorno estorno = porId(id);
        if (estorno == null) {
            falhar(resultado, "Estorno não encontrado.");
            return;
        }
        estorno.setJustificativa(justificativa);
        entregar(resultado, estorno);
    }

    @Override
    public void remover(int id, Resultado<Void> resultado) {
        DadosDemo.ESTORNOS.remove(porId(id));
        entregar(resultado, null);
    }

    private boolean atende(Estorno estorno, String busca) {
        Demanda demanda = estorno.getDemanda();
        String alvo = estorno.getCodigo() + " #" + demanda.getId() + " " + demanda.getTitulo();
        return alvo.toLowerCase(Locale.ROOT).contains(busca.toLowerCase(Locale.ROOT));
    }

    private Estorno porId(int id) {
        for (Estorno estorno : DadosDemo.ESTORNOS) {
            if (estorno.getId() == id) {
                return estorno;
            }
        }
        return null;
    }

    private <T> void entregar(Resultado<T> resultado, T dados) {
        principal.post(() -> resultado.sucesso(dados));
    }

    private <T> void falhar(Resultado<T> resultado, String mensagem) {
        principal.post(() -> resultado.erro(mensagem));
    }
}
