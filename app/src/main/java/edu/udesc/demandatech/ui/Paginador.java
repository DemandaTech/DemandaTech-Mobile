package edu.udesc.demandatech.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.Pagina;

// Paginação pelo scroll: pede a próxima página quando a lista chega perto do fim.
public class Paginador<T> {

    // De onde vêm as páginas, normalmente uma chamada ao repositório.
    public interface Fonte<T> {
        void buscar(int pagina, Resultado<Pagina<T>> resultado);
    }

    // Avisos para a tela atualizar total, estado vazio e mensagem de erro.
    public interface Tela<T> {
        void aoComecar();

        void aoCarregar(Pagina<T> pagina);

        void aoFalhar(String mensagem);
    }

    // Quantos itens antes do fim da lista a próxima página é pedida.
    private static final int ANTECEDENCIA = 3;

    private final RecyclerView lista;
    private final ListaPaginadaAdapter<T> adapter;
    private final LinearLayoutManager organizador;
    private final Fonte<T> fonte;
    private final Tela<T> tela;

    private int pagina;
    private boolean temMais;
    private boolean carregando;
    // Identifica o pedido mais recente, para ignorar respostas de pedidos antigos.
    private int pedidoAtual;

    public Paginador(RecyclerView lista, ListaPaginadaAdapter<T> adapter, Fonte<T> fonte,
                     Tela<T> tela) {
        this.lista = lista;
        this.adapter = adapter;
        this.fonte = fonte;
        this.tela = tela;

        // A tela define o organizador antes, porque ele é exigido ao inflar o cabeçalho.
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

    // Volta para a primeira página, por exemplo ao mudar a busca ou os filtros.
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
                // Se a página não encheu a tela, não haverá scroll: busca a seguinte direto.
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

    // Chamado quando a tela é destruída, para descartar respostas que ainda chegarem.
    public void encerrar() {
        pedidoAtual++;
    }

    private void terminar() {
        carregando = false;
        adapter.mostrarCarregando(false);
    }

    // O pedido sai no próximo ciclo: a lista não aceita mudanças durante o scroll.
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
