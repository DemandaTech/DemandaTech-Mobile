package edu.udesc.demandatech.ui.cadastros;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.model.ItemCadastro;
import edu.udesc.demandatech.ui.Etiquetas;
import edu.udesc.demandatech.ui.ListaPaginadaAdapter;

public class CadastroAdapter extends ListaPaginadaAdapter<ItemCadastro> {

    public interface AoClicar {
        void abrir(ItemCadastro item);
    }

    private final AoClicar aoClicar;

    public CadastroAdapter(View cabecalho, AoClicar aoClicar) {
        super(cabecalho);
        this.aoClicar = aoClicar;
    }

    @Override
    protected RecyclerView.ViewHolder criarCartao(LayoutInflater inflater, ViewGroup parent) {
        return new Cartao(inflater.inflate(R.layout.item_cadastro, parent, false));
    }

    @Override
    protected void preencher(RecyclerView.ViewHolder holder, ItemCadastro item) {
        Cartao cartao = (Cartao) holder;
        cartao.tvTitulo.setText(item.getTitulo());
        cartao.tvDetalhe.setText(item.getDetalhe());
        cartao.itemView.setOnClickListener(v -> aoClicar.abrir(item));

        boolean temMarcador = item.getMarcador() != null;
        cartao.tvMarcador.setVisibility(temMarcador ? View.VISIBLE : View.GONE);
        if (temMarcador) {
            Etiquetas.neutra(cartao.tvMarcador, item.getMarcador());
        }
    }

    private static class Cartao extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final TextView tvDetalhe;
        final TextView tvMarcador;

        Cartao(View view) {
            super(view);
            tvTitulo = view.findViewById(R.id.tvTitulo);
            tvDetalhe = view.findViewById(R.id.tvDetalhe);
            tvMarcador = view.findViewById(R.id.tvMarcador);
        }
    }
}
