package edu.udesc.demandatech.ui.relatorios;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.model.ItemRelatorio;

// Monta as linhas com barra proporcional usadas na Visão geral e nos Relatórios.
final class Barras {

    private Barras() {
    }

    // Mostra até "limite" itens; a barra de cada um é proporcional ao maior valor.
    static void preencher(LinearLayout destino, List<ItemRelatorio> itens, int limite) {
        destino.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(destino.getContext());
        if (itens.isEmpty()) {
            adicionarLinha(inflater, destino, new ItemRelatorio("Sem dados para mostrar.", 0, null), 0);
            return;
        }

        int maior = 1;
        for (ItemRelatorio item : itens) {
            maior = Math.max(maior, item.getValor());
        }
        for (int posicao = 0; posicao < Math.min(limite, itens.size()); posicao++) {
            adicionarLinha(inflater, destino, itens.get(posicao), maior);
        }
    }

    // Com "maior" igual a zero a linha vira só um aviso, sem valor nem barra.
    private static void adicionarLinha(LayoutInflater inflater, LinearLayout destino,
                                       ItemRelatorio item, int maior) {
        View linha = inflater.inflate(R.layout.item_barra, destino, false);
        TextView tvNome = linha.findViewById(R.id.tvNome);
        TextView tvValor = linha.findViewById(R.id.tvValor);
        TextView tvDetalhe = linha.findViewById(R.id.tvDetalhe);
        LinearProgressIndicator barra = linha.findViewById(R.id.barra);

        tvNome.setText(item.getNome());
        if (maior == 0) {
            tvValor.setVisibility(View.GONE);
            barra.setVisibility(View.GONE);
        } else {
            tvValor.setText(String.valueOf(item.getValor()));
            barra.setMax(maior);
            barra.setProgress(item.getValor());
        }
        if (item.getDetalhe() != null) {
            tvDetalhe.setText(item.getDetalhe());
            tvDetalhe.setVisibility(View.VISIBLE);
        }
        destino.addView(linha);
    }
}
