package edu.udesc.demandatech.ui;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.model.Prioridade;
import edu.udesc.demandatech.model.Status;

// Pinta as etiquetas coloridas de status, prioridade e tipo.
public final class Etiquetas {

    private Etiquetas() {
    }

    public static void status(TextView etiqueta, Status status) {
        etiqueta.setText(status.getRotulo());
        switch (status) {
            case AGUARDANDO:
                pintar(etiqueta, R.color.amarelo_texto, R.color.amarelo_fundo);
                break;
            case RESOLVIDA:
                pintar(etiqueta, R.color.verde_texto, R.color.verde_fundo);
                break;
            case CANCELADA:
                pintar(etiqueta, R.color.vermelho, R.color.vermelho_fundo);
                break;
            default:
                pintar(etiqueta, R.color.azul_texto, R.color.azul_fundo);
        }
    }

    public static void prioridade(TextView etiqueta, Prioridade prioridade) {
        etiqueta.setText(prioridade.getRotulo());
        switch (prioridade) {
            case URGENTE:
            case ALTA:
                pintar(etiqueta, R.color.vermelho, R.color.vermelho_fundo);
                break;
            case MEDIA:
                pintar(etiqueta, R.color.amarelo_texto, R.color.amarelo_fundo);
                break;
            default:
                pintar(etiqueta, R.color.verde_texto, R.color.verde_fundo);
        }
    }

    public static void neutra(TextView etiqueta, String texto) {
        etiqueta.setText(texto);
        pintar(etiqueta, R.color.azul_texto, R.color.azul_fundo);
    }

    private static void pintar(TextView etiqueta, int corTexto, int corFundo) {
        etiqueta.setTextColor(ContextCompat.getColor(etiqueta.getContext(), corTexto));
        int fundo = ContextCompat.getColor(etiqueta.getContext(), corFundo);
        ViewCompat.setBackgroundTintList(etiqueta, ColorStateList.valueOf(fundo));
    }
}
