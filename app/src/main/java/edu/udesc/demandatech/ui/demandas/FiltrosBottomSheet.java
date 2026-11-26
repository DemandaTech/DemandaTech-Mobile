package edu.udesc.demandatech.ui.demandas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Objects;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.FiltroDemanda;
import edu.udesc.demandatech.model.Prioridade;
import edu.udesc.demandatech.model.Status;
import edu.udesc.demandatech.model.TipoDemanda;

public class FiltrosBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULTADO = "filtros_aplicados";
    public static final String FILTRO = "filtro";
    private FiltroDemanda atual;
    private ChipGroup grupoStatus;
    private ChipGroup grupoPrioridade;
    private ChipGroup grupoTipo;

    public static FiltrosBottomSheet novo(FiltroDemanda filtro) {
        Bundle argumentos = new Bundle();
        argumentos.putSerializable(FILTRO, filtro);
        FiltrosBottomSheet painel = new FiltrosBottomSheet();
        painel.setArguments(argumentos);
        return painel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.sheet_filtros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estado) {
        atual = (FiltroDemanda) requireArguments().getSerializable(FILTRO);
        grupoStatus = view.findViewById(R.id.grupoStatus);
        grupoPrioridade = view.findViewById(R.id.grupoPrioridade);
        grupoTipo = view.findViewById(R.id.grupoTipo);

        adicionarOpcao(grupoStatus, "Todos", null, atual.status);
        for (Status status : Status.values()) {
            adicionarOpcao(grupoStatus, status.getRotulo(), status, atual.status);
        }

        adicionarOpcao(grupoPrioridade, "Todas", null, atual.prioridade);
        for (Prioridade prioridade : Prioridade.values()) {
            adicionarOpcao(grupoPrioridade, prioridade.getRotulo(), prioridade, atual.prioridade);
        }

        adicionarOpcao(grupoTipo, "Todos", null, atual.idTipo);
        carregarTipos();

        view.findViewById(R.id.btnAplicar).setOnClickListener(v -> aplicar());
        view.findViewById(R.id.btnLimpar).setOnClickListener(v -> devolver(new FiltroDemanda()));
    }

    private void carregarTipos() {
        Repositorios.demandas(requireContext()).listarTipos(new Resultado<List<TipoDemanda>>() {
            @Override
            public void sucesso(List<TipoDemanda> tipos) {
                if (getView() == null) {
                    return;
                }
                for (TipoDemanda tipo : tipos) {
                    adicionarOpcao(grupoTipo, tipo.getDescricao(), tipo.getId(), atual.idTipo);
                }
            }

            @Override
            public void erro(String mensagem) { }
        });
    }

    private void aplicar() {
        FiltroDemanda novo = new FiltroDemanda();
        novo.status = (Status) selecionado(grupoStatus);
        novo.prioridade = (Prioridade) selecionado(grupoPrioridade);
        novo.idTipo = (Long) selecionado(grupoTipo);
        devolver(novo);
    }

    private void devolver(FiltroDemanda novo) {
        novo.texto = atual.texto;
        Bundle dados = new Bundle();
        dados.putSerializable(FILTRO, novo);
        getParentFragmentManager().setFragmentResult(RESULTADO, dados);
        dismiss();
    }

    private void adicionarOpcao(ChipGroup grupo, String rotulo, Object valor, Object marcado) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_filtro, grupo, false);
        chip.setId(View.generateViewId());
        chip.setText(rotulo);
        chip.setTag(valor);
        grupo.addView(chip);
        chip.setChecked(Objects.equals(valor, marcado));
    }

    private Object selecionado(ChipGroup grupo) {
        View chip = grupo.findViewById(grupo.getCheckedChipId());
        return chip == null ? null : chip.getTag();
    }
}
