package edu.udesc.demandatech.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.Pagina;

public class Paginador<T> {

    public interface Fonte<T> {
        void buscar(int pagina, Resultado<Pagina<T>> resultado);
    }

    public interface Tela<T> {
        void aoComecar();

        void aoCarregar(Pagina<T> pagina);

        void aoFalhar(String mensagem);
    }

    private static final int ANTECEDENCIA = 3;

    private final RecyclerView lista;
    private final ListaPaginadaAdapter<T> adapter;
    private final LinearLayoutManager organizador;
    private final Fonte<T> fonte;
    private final Tela<T> tela;

    private int pagina;
    private boolean temMais;
    private boolean carregando;
    private int pedidoAtual;

    public Paginador(RecyclerView lista, ListaPaginadaAdapter<T> adapter, Fonte<T> fonte,
                     Tela<T> tela) {
        this.lista = lista;
        this.adapter = adapter;
        this.fonte = fonte;
        this.tela = tela;

        organizador = (LinearLayoutManager) lista.getLayoutManager();
        lista.setAdapter(adapter);
        lista.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    carregarSeChegouAoFim();
                }
            }
        });
    }

    public void recarregar() {
        pagina = 0;
        temMais = true;
        adapter.limpar();
        carregarProxima();
    }

    public void carregarProxima() {
        carregando = true;
        int pedido = ++pedidoAtual;
        int proxima = pagina + 1;
        tela.aoComecar();
        adapter.mostrarCarregando(true);

        fonte.buscar(proxima, new Resultado<Pagina<T>>() {
            @Override
            public void sucesso(Pagina<T> dados) {
                if (pedido != pedidoAtual) {
                    return;
                }
                pagina = proxima;
                temMais = dados.temProxima();
                terminar();
                adapter.adicionar(dados.getItens());
                tela.aoCarregar(dados);
                carregarSeChegouAoFim();
            }

            @Override
            public void erro(String mensagem) {
                if (pedido == pedidoAtual) {
                    terminar();
                    tela.aoFalhar(mensagem);
                }
            }
        });
    }

    public void encerrar() {
        pedidoAtual++;
    }

    private void terminar() {
        carregando = false;
        adapter.mostrarCarregando(false);
    }

    private void carregarSeChegouAoFim() {
        if (carregando || !temMais) {
            return;
        }
        lista.post(() -> {
            int ultimoVisivel = organizador.findLastVisibleItemPosition();
            if (!carregando && ultimoVisivel >= adapter.getItemCount() - ANTECEDENCIA) {
                carregarProxima();
            }
        });
    }
}
