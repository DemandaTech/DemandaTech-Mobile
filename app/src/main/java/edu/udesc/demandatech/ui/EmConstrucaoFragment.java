package edu.udesc.demandatech.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import edu.udesc.demandatech.R;

public class EmConstrucaoFragment extends Fragment {

    private static final String TITULO = "titulo";

    public static EmConstrucaoFragment novo(String titulo) {
        Bundle argumentos = new Bundle();
        argumentos.putString(TITULO, titulo);
        EmConstrucaoFragment fragment = new EmConstrucaoFragment();
        fragment.setArguments(argumentos);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle estado) {
        View view = inflater.inflate(R.layout.fragment_em_construcao, container, false);
        TextView tvTitulo = view.findViewById(R.id.tvTitulo);
        tvTitulo.setText(requireArguments().getString(TITULO));
        return view;
    }
}
