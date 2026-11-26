package edu.udesc.demandatech.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.udesc.demandatech.R;

public abstract class ListaPaginadaAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int CABECALHO = 0;
    private static final int CARTAO = 1;
    private static final int CARREGANDO = 2;
    private final View cabecalho;
    private final List<T> itens = new ArrayList<>();
    private boolean carregando;

    protected ListaPaginadaAdapter(View cabecalho) {
        this.cabecalho = cabecalho;
    }

    protected abstract RecyclerView.ViewHolder criarCartao(LayoutInflater inflater, ViewGroup parent);

    protected abstract void preencher(RecyclerView.ViewHolder cartao, T item);

    public void limpar() {
        int quantidade = itens.size();
        itens.clear();
        notifyItemRangeRemoved(1, quantidade);
    }

    public void adicionar(List<T> novos) {
        int inicio = 1 + itens.size();
        itens.addAll(novos);
        notifyItemRangeInserted(inicio, novos.size());
    }

    public void mostrarCarregando(boolean mostrar) {
        if (carregando == mostrar) {
            return;
        }
        carregando = mostrar;
        int posicao = 1 + itens.size();
        if (mostrar) {
            notifyItemInserted(posicao);
        } else {
            notifyItemRemoved(posicao);
        }
    }

    @Override
    public int getItemCount() {
        return 1 + itens.size() + (carregando ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return CABECALHO;
        }
        return position <= itens.size() ? CARTAO : CARREGANDO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == CARTAO) {
            return criarCartao(inflater, parent);
        }
        if (viewType == CARREGANDO) {
            return new Fixo(inflater.inflate(R.layout.item_carregando, parent, false));
        }
        soltar(cabecalho);
        return new Fixo(cabecalho);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == CARTAO) {
            preencher(holder, itens.get(position - 1));
        }
    }

    private void soltar(View view) {
        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private static class Fixo extends RecyclerView.ViewHolder {

        Fixo(View view) {
            super(view);
        }
    }
}
