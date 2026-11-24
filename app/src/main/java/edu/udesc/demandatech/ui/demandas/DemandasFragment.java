package edu.udesc.demandatech.ui.demandas;

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

import com.google.android.material.button.MaterialButton;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.DemandaRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.FiltroDemanda;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.ResumoDemandas;
import edu.udesc.demandatech.model.Usuario;
import edu.udesc.demandatech.ui.Paginador;

// Lista de demandas com busca, filtros e paginação pelo scroll.
// Busca e filtros são apenas enviados ao repositório: quem filtra é o backend.
public class DemandasFragment extends Fragment implements Paginador.Tela<Demanda> {

    private static final String FILTRO = "filtro";

    private DemandaRepository repositorio;
    private Paginador<Demanda> paginador;
    private FiltroDemanda filtro = new FiltroDemanda();
    private boolean mostraResumo;

    private View cabecalho;
    private TextView tvTotal;
    private TextView tvEstado;
    private MaterialButton btnFiltros;

    // Detalhe e formulário avisam com RESULT_OK quando algo mudou.
    private final ActivityResultLauncher<Intent> telaComRetorno = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), retorno -> {
                if (retorno.getResultCode() == Activity.RESULT_OK) {
                    recarregar();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_demandas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estado) {
        if (estado != null) {
            filtro = (FiltroDemanda) estado.getSerializable(FILTRO);
        }
        repositorio = Repositorios.demandas(requireContext());

        RecyclerView lista = view.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        cabecalho = getLayoutInflater().inflate(R.layout.cabecalho_demandas, lista, false);
        tvTotal = cabecalho.findViewById(R.id.tvTotal);
        tvEstado = cabecalho.findViewById(R.id.tvEstado);
        btnFiltros = cabecalho.findViewById(R.id.btnFiltros);

        DemandaAdapter adapter = new DemandaAdapter(cabecalho, this::abrirDetalhe);
        paginador = new Paginador<>(lista, adapter,
                (pagina, resultado) -> repositorio.listar(filtro, pagina, resultado), this);

        configurarResumo();
        configurarBusca(cabecalho.findViewById(R.id.etBusca));
        configurarFiltros();
        tvEstado.setOnClickListener(v -> paginador.carregarProxima());
        view.findViewById(R.id.btnNova).setOnClickListener(v ->
                telaComRetorno.launch(FormularioDemandaActivity.nova(requireContext())));

        recarregar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        paginador.encerrar();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putSerializable(FILTRO, filtro);
    }

    // Perfis sem a aba Visão geral veem um resumo no topo da lista.
    private void configurarResumo() {
        Usuario usuario = new Sessao(requireContext()).getUsuario();
        mostraResumo = !usuario.getPerfil().isGestor();
        if (!mostraResumo) {
            return;
        }
        cabecalho.findViewById(R.id.resumo).setVisibility(View.VISIBLE);
        TextView tvSaudacao = cabecalho.findViewById(R.id.tvSaudacao);
        tvSaudacao.setText("Olá, " + usuario.getPrimeiroNome());

        rotular(R.id.indicadorTotal, "Meus chamados");
        rotular(R.id.indicadorAbertas, "Abertos");
        rotular(R.id.indicadorAguardando, "Aguardando");
        rotular(R.id.indicadorResolvidas, "Resolvidos");
    }

    private void configurarBusca(EditText etBusca) {
        etBusca.setOnEditorActionListener((campo, acao, evento) -> {
            filtro.texto = campo.getText().toString().trim();
            esconderTeclado(campo);
            recarregar();
            return true;
        });
    }

    private void configurarFiltros() {
        btnFiltros.setOnClickListener(v ->
                FiltrosBottomSheet.novo(filtro).show(getChildFragmentManager(), "filtros"));

        getChildFragmentManager().setFragmentResultListener(FiltrosBottomSheet.RESULTADO,
                getViewLifecycleOwner(), (chave, dados) -> {
                    filtro = (FiltroDemanda) dados.getSerializable(FiltrosBottomSheet.FILTRO);
                    recarregar();
                });
    }

    // Volta para a primeira página, por exemplo ao mudar a busca ou os filtros.
    private void recarregar() {
        int ativos = filtro.quantidadeAtivos();
        btnFiltros.setText(ativos == 0 ? "Filtros" : "Filtros (" + ativos + ")");

        paginador.recarregar();
        if (mostraResumo) {
            carregarResumo();
        }
    }

    @Override
    public void aoComecar() {
        tvEstado.setVisibility(View.GONE);
    }

    @Override
    public void aoCarregar(Pagina<Demanda> dados) {
        int total = dados.getTotal();
        tvTotal.setText(total == 1 ? "1 demanda" : total + " demandas");
        if (total == 0) {
            tvEstado.setText("Nenhuma demanda encontrada.");
            tvEstado.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void aoFalhar(String mensagem) {
        tvEstado.setText(mensagem + "\nToque para tentar novamente.");
        tvEstado.setVisibility(View.VISIBLE);
    }

    private void carregarResumo() {
        repositorio.resumo(new Resultado<ResumoDemandas>() {
            @Override
            public void sucesso(ResumoDemandas dados) {
                if (getView() == null) {
                    return;
                }
                valorar(R.id.indicadorTotal, dados.total);
                valorar(R.id.indicadorAbertas, dados.abertas);
                valorar(R.id.indicadorAguardando, dados.aguardando);
                valorar(R.id.indicadorResolvidas, dados.resolvidas);
            }

            @Override
            public void erro(String mensagem) {
                // O resumo é complementar: a lista já informa a falha.
            }
        });
    }

    private void abrirDetalhe(Demanda demanda) {
        telaComRetorno.launch(DetalheDemandaActivity.intent(requireContext(), demanda.getId()));
    }

    private void rotular(int indicador, String rotulo) {
        TextView tvRotulo = cabecalho.findViewById(indicador).findViewById(R.id.tvRotulo);
        tvRotulo.setText(rotulo);
    }

    private void valorar(int indicador, int valor) {
        TextView tvValor = cabecalho.findViewById(indicador).findViewById(R.id.tvValor);
        tvValor.setText(String.valueOf(valor));
    }

    private void esconderTeclado(View campo) {
        InputMethodManager teclado = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        teclado.hideSoftInputFromWindow(campo.getWindowToken(), 0);
    }
}
