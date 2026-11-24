package edu.udesc.demandatech.ui.estornos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.ui.ListaPaginadaAdapter;

public class EstornoAdapter extends ListaPaginadaAdapter<Estorno> {

    public interface AoClicar {
        void abrir(Estorno estorno);
    }

    private final AoClicar aoClicar;

    public EstornoAdapter(View cabecalho, AoClicar aoClicar) {
        super(cabecalho);
        this.aoClicar = aoClicar;
    }

    @Override
    protected RecyclerView.ViewHolder criarCartao(LayoutInflater inflater, ViewGroup parent) {
        return new Cartao(inflater.inflate(R.layout.item_estorno, parent, false));
    }

    @Override
    protected void preencher(RecyclerView.ViewHolder holder, Estorno estorno) {
        Cartao cartao = (Cartao) holder;
        Demanda demanda = estorno.getDemanda();
        cartao.tvCodigo.setText(estorno.getCodigo());
        cartao.tvData.setText(estorno.getData());
        cartao.tvDemanda.setText("#" + demanda.getId() + " · " + demanda.getTitulo());
        cartao.tvJustificativa.setText(estorno.getJustificativa());
        cartao.tvTecnico.setText("Técnico: " + demanda.getResponsavel().getNome());
        cartao.itemView.setOnClickListener(v -> aoClicar.abrir(estorno));
    }

    private static class Cartao extends RecyclerView.ViewHolder {

        final TextView tvCodigo;
        final TextView tvData;
        final TextView tvDemanda;
        final TextView tvJustificativa;
        final TextView tvTecnico;

        Cartao(View view) {
            super(view);
            tvCodigo = view.findViewById(R.id.tvCodigo);
            tvData = view.findViewById(R.id.tvData);
            tvDemanda = view.findViewById(R.id.tvDemanda);
            tvJustificativa = view.findViewById(R.id.tvJustificativa);
            tvTecnico = view.findViewById(R.id.tvTecnico);
        }
    }
}
