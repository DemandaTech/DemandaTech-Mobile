package edu.udesc.demandatech.ui.demandas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.ui.Etiquetas;
import edu.udesc.demandatech.ui.ListaPaginadaAdapter;

public class DemandaAdapter extends ListaPaginadaAdapter<Demanda> {

    public interface AoClicar {
        void abrir(Demanda demanda);
    }

    private final AoClicar aoClicar;

    public DemandaAdapter(View cabecalho, AoClicar aoClicar) {
        super(cabecalho);
        this.aoClicar = aoClicar;
    }

    @Override
    protected RecyclerView.ViewHolder criarCartao(LayoutInflater inflater, ViewGroup parent) {
        return new Cartao(inflater.inflate(R.layout.item_demanda, parent, false));
    }

    @Override
    protected void preencher(RecyclerView.ViewHolder holder, Demanda demanda) {
        Cartao cartao = (Cartao) holder;
        cartao.tvId.setText("#" + demanda.getId());
        cartao.tvData.setText(demanda.getAbertura());
        cartao.tvTitulo.setText(demanda.getTitulo());
        cartao.tvRodape.setText(demanda.getTipo() + " · " + demanda.getResponsavel().getNome());
        Etiquetas.status(cartao.tvStatus, demanda.getStatus());
        Etiquetas.prioridade(cartao.tvPrioridade, demanda.getPrioridade());
        cartao.itemView.setOnClickListener(v -> aoClicar.abrir(demanda));
    }

    private static class Cartao extends RecyclerView.ViewHolder {

        final TextView tvId;
        final TextView tvData;
        final TextView tvTitulo;
        final TextView tvStatus;
        final TextView tvPrioridade;
        final TextView tvRodape;

        Cartao(View view) {
            super(view);
            tvId = view.findViewById(R.id.tvId);
            tvData = view.findViewById(R.id.tvData);
            tvTitulo = view.findViewById(R.id.tvTitulo);
            tvStatus = view.findViewById(R.id.tvStatus);
            tvPrioridade = view.findViewById(R.id.tvPrioridade);
            tvRodape = view.findViewById(R.id.tvRodape);
        }
    }
}
