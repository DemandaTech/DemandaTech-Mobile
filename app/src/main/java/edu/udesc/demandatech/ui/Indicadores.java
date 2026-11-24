package edu.udesc.demandatech.ui;

import android.view.View;
import android.widget.TextView;

import edu.udesc.demandatech.R;

// Preenche os cartões de indicador (layout view_indicador).
public final class Indicadores {

    private Indicadores() {
    }

    public static void rotular(View raiz, int indicador, String rotulo) {
        TextView tvRotulo = raiz.findViewById(indicador).findViewById(R.id.tvRotulo);
        tvRotulo.setText(rotulo);
    }

    public static void valorar(View raiz, int indicador, String valor) {
        TextView tvValor = raiz.findViewById(indicador).findViewById(R.id.tvValor);
        tvValor.setText(valor);
    }
}
