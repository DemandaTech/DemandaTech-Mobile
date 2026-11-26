package edu.udesc.demandatech.ui.estornos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.EstornoRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.ui.Paginador;

public class EstornosFragment extends Fragment implements Paginador.Tela<Estorno> {

    private static final String BUSCA = "busca";

    private EstornoRepository repositorio;
    private Paginador<Estorno> paginador;
    private String busca = "";

    private TextView tvTotal;
    private TextView tvEstado;

    private final ActivityResultLauncher<Intent> telaComRetorno = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), retorno -> {
                if (retorno.getResultCode() == Activity.RESULT_OK) {
                    paginador.recarregar();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_estornos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estado) {
        if (estado != null) {
            busca = estado.getString(BUSCA, "");
        }
        repositorio = Repositorios.estornos(requireContext());

        RecyclerView lista = view.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        View cabecalho = getLayoutInflater().inflate(R.layout.cabecalho_estornos, lista, false);
        tvTotal = cabecalho.findViewById(R.id.tvTotal);
        tvEstado = cabecalho.findViewById(R.id.tvEstado);

        EstornoAdapter adapter = new EstornoAdapter(cabecalho, this::abrirEstorno);
        paginador = new Paginador<>(lista, adapter,
                (pagina, resultado) -> repositorio.listar(busca, pagina, resultado), this);

        configurarBusca(cabecalho.findViewById(R.id.etBusca));
        tvEstado.setOnClickListener(v -> paginador.carregarProxima());
        paginador.recarregar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        paginador.encerrar();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putString(BUSCA, busca);
    }

    @Override
    public void aoComecar() {
        tvEstado.setVisibility(View.GONE);
    }

    @Override
    public void aoCarregar(Pagina<Estorno> dados) {
        int total = dados.getTotal();
        tvTotal.setText(total == 1 ? "1 estorno registrado" : total + " estornos registrados");
        if (total == 0) {
            tvEstado.setText("Nenhum estorno encontrado.");
            tvEstado.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void aoFalhar(String mensagem) {
        tvEstado.setText(mensagem + "\nToque para tentar novamente.");
        tvEstado.setVisibility(View.VISIBLE);
    }

    private void configurarBusca(EditText etBusca) {
        etBusca.setOnEditorActionListener((campo, acao, evento) -> {
            busca = campo.getText().toString().trim();
            InputMethodManager teclado = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            teclado.hideSoftInputFromWindow(campo.getWindowToken(), 0);
            paginador.recarregar();
            return true;
        });
    }

    private void abrirEstorno(Estorno estorno) {
        telaComRetorno.launch(EstornoActivity.existente(requireContext(), estorno.getId()));
    }
}
