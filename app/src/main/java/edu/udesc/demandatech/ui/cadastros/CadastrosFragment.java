package edu.udesc.demandatech.ui.cadastros;

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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.CadastroRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Cadastro;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.ItemCadastro;
import edu.udesc.demandatech.model.Pagina;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;
import edu.udesc.demandatech.ui.Paginador;

// Cadastros: uma lista que troca de conteúdo conforme a aba escolhida.
public class CadastrosFragment extends Fragment implements Paginador.Tela<ItemCadastro> {

    private static final String ABA = "aba";
    private static final String BUSCA = "busca";

    private CadastroRepository repositorio;
    private Paginador<ItemCadastro> paginador;
    private Cadastro cadastro = Cadastro.FUNCIONARIOS;
    private String busca = "";

    private EditText etBusca;
    private TextView tvTotal;
    private TextView tvEstado;
    private ExtendedFloatingActionButton btnNovo;

    // Os formulários avisam com RESULT_OK quando algo mudou.
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
        return inflater.inflate(R.layout.fragment_cadastros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estado) {
        if (estado != null) {
            cadastro = Cadastro.valueOf(estado.getString(ABA, Cadastro.FUNCIONARIOS.name()));
            busca = estado.getString(BUSCA, "");
        }
        repositorio = Repositorios.cadastros(requireContext());

        RecyclerView lista = view.findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        View cabecalho = getLayoutInflater().inflate(R.layout.cabecalho_cadastros, lista, false);
        etBusca = cabecalho.findViewById(R.id.etBusca);
        tvTotal = cabecalho.findViewById(R.id.tvTotal);
        tvEstado = cabecalho.findViewById(R.id.tvEstado);
        btnNovo = view.findViewById(R.id.btnNovo);

        CadastroAdapter adapter = new CadastroAdapter(cabecalho, this::abrirRegistro);
        paginador = new Paginador<>(lista, adapter, (pagina, resultado) ->
                repositorio.listar(cadastro, busca, pagina, resultado), this);

        configurarAbas(view.findViewById(R.id.abas));
        configurarBusca();
        tvEstado.setOnClickListener(v -> paginador.carregarProxima());
        btnNovo.setOnClickListener(v -> abrirNovo());
        mostrarCadastro();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        paginador.encerrar();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putString(ABA, cadastro.name());
        estado.putString(BUSCA, busca);
    }

    // O Gerente administra só os funcionários do seu departamento, então não vê as abas.
    private void configurarAbas(TabLayout abas) {
        Usuario usuario = new Sessao(requireContext()).getUsuario();
        if (!usuario.isAdministrador()) {
            abas.setVisibility(View.GONE);
            cadastro = Cadastro.FUNCIONARIOS;
            return;
        }
        for (Cadastro opcao : Cadastro.values()) {
            TabLayout.Tab aba = abas.newTab().setText(opcao.getRotulo()).setTag(opcao);
            abas.addTab(aba, opcao == cadastro);
        }
        abas.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab aba) {
                cadastro = (Cadastro) aba.getTag();
                busca = "";
                etBusca.setText("");
                mostrarCadastro();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab aba) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab aba) {
            }
        });
    }

    private void configurarBusca() {
        etBusca.setOnEditorActionListener((campo, acao, evento) -> {
            busca = campo.getText().toString().trim();
            InputMethodManager teclado = (InputMethodManager)
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            teclado.hideSoftInputFromWindow(campo.getWindowToken(), 0);
            paginador.recarregar();
            return true;
        });
    }

    // Ajusta os textos da tela ao cadastro atual e carrega a lista.
    private void mostrarCadastro() {
        etBusca.setHint(cadastro.getDicaBusca());
        btnNovo.setText(cadastro.getAcaoNovo());
        paginador.recarregar();
    }

    @Override
    public void aoComecar() {
        tvEstado.setVisibility(View.GONE);
    }

    @Override
    public void aoCarregar(Pagina<ItemCadastro> dados) {
        tvTotal.setText("Total: " + dados.getTotal());
        if (dados.getTotal() == 0) {
            tvEstado.setText("Nenhum registro encontrado.");
            tvEstado.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void aoFalhar(String mensagem) {
        tvEstado.setText(mensagem + "\nToque para tentar novamente.");
        tvEstado.setVisibility(View.VISIBLE);
    }

    private void abrirNovo() {
        Context context = requireContext();
        telaComRetorno.launch(cadastro == Cadastro.FUNCIONARIOS
                ? FuncionarioActivity.novo(context)
                : CadastroSimplesActivity.novo(context, cadastro));
    }

    private void abrirRegistro(ItemCadastro item) {
        Context context = requireContext();
        if (item instanceof Usuario) {
            String matricula = ((Usuario) item).getMatricula();
            telaComRetorno.launch(FuncionarioActivity.editar(context, matricula));
        } else if (item instanceof Departamento) {
            long id = ((Departamento) item).getId();
            telaComRetorno.launch(CadastroSimplesActivity.editar(context, Cadastro.DEPARTAMENTOS, id));
        } else {
            long id = ((TipoDemanda) item).getId();
            telaComRetorno.launch(CadastroSimplesActivity.editar(context, Cadastro.TIPOS, id));
        }
    }
}
