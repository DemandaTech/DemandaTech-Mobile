package edu.udesc.demandatech.ui.demandas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.DemandaRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Prioridade;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;
import edu.udesc.demandatech.ui.Telas;

// Formulário usado para criar uma demanda e para editar uma existente.
public class FormularioDemandaActivity extends AppCompatActivity {

    private static final String ID = "id";
    private static final int NOVA = 0;

    private DemandaRepository repositorio;
    private int idEdicao;
    private List<TipoDemanda> tipos = new ArrayList<>();
    private final List<Usuario> envolvidos = new ArrayList<>();

    private TextInputLayout campoTitulo;
    private TextInputLayout campoDescricao;
    private TextInputLayout campoPrioridade;
    private TextInputLayout campoTipo;
    private TextInputEditText etTitulo;
    private TextInputEditText etDescricao;
    private MaterialAutoCompleteTextView acPrioridade;
    private MaterialAutoCompleteTextView acTipo;
    private MaterialAutoCompleteTextView acEnvolvido;
    private ChipGroup grupoEnvolvidos;
    private MaterialButton btnSalvar;

    public static Intent nova(Context context) {
        return new Intent(context, FormularioDemandaActivity.class);
    }

    public static Intent editar(Context context, int id) {
        return nova(context).putExtra(ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Telas.abrir(this, R.layout.activity_formulario_demanda);
        repositorio = Repositorios.demandas(this);
        idEdicao = getIntent().getIntExtra(ID, NOVA);

        campoTitulo = findViewById(R.id.campoTitulo);
        campoDescricao = findViewById(R.id.campoDescricao);
        campoPrioridade = findViewById(R.id.campoPrioridade);
        campoTipo = findViewById(R.id.campoTipo);
        etTitulo = findViewById(R.id.etTitulo);
        etDescricao = findViewById(R.id.etDescricao);
        acPrioridade = findViewById(R.id.acPrioridade);
        acTipo = findViewById(R.id.acTipo);
        acEnvolvido = findViewById(R.id.acEnvolvido);
        grupoEnvolvidos = findViewById(R.id.grupoEnvolvidos);
        btnSalvar = findViewById(R.id.btnSalvar);

        MaterialToolbar barra = findViewById(R.id.barra);
        barra.setNavigationOnClickListener(v -> finish());
        btnSalvar.setOnClickListener(v -> salvar());

        carregarPrioridades();
        carregarTipos();

        if (emEdicao()) {
            barra.setTitle("Editar demanda");
            btnSalvar.setText("Salvar alterações");
            btnSalvar.setIcon(null);
            // A edição não altera os envolvidos, assim como o backend.
            findViewById(R.id.secaoEnvolvidos).setVisibility(View.GONE);
            carregarDemanda();
        } else {
            carregarFuncionarios();
        }
    }

    private boolean emEdicao() {
        return idEdicao != NOVA;
    }

    private void carregarPrioridades() {
        List<String> rotulos = new ArrayList<>();
        for (Prioridade prioridade : Prioridade.values()) {
            rotulos.add(prioridade.getRotulo());
        }
        acPrioridade.setAdapter(opcoes(rotulos));
    }

    private void carregarTipos() {
        repositorio.listarTipos(new Resultado<List<TipoDemanda>>() {
            @Override
            public void sucesso(List<TipoDemanda> dados) {
                tipos = dados;
                acTipo.setAdapter(opcoes(dados));
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
            }
        });
    }

    private void carregarFuncionarios() {
        repositorio.listarFuncionarios(new Resultado<List<Usuario>>() {
            @Override
            public void sucesso(List<Usuario> funcionarios) {
                acEnvolvido.setAdapter(opcoes(funcionarios));
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
            }
        });

        acEnvolvido.setOnItemClickListener((lista, view, posicao, id) -> {
            acEnvolvido.setText("");
            adicionarEnvolvido((Usuario) lista.getItemAtPosition(posicao));
        });
    }

    private void carregarDemanda() {
        repositorio.buscar(idEdicao, new Resultado<Demanda>() {
            @Override
            public void sucesso(Demanda demanda) {
                etTitulo.setText(demanda.getTitulo());
                etDescricao.setText(demanda.getDescricao());
                acPrioridade.setText(demanda.getPrioridade().getRotulo(), false);
                acTipo.setText(demanda.getTipo().getDescricao(), false);
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
                finish();
            }
        });
    }

    private void adicionarEnvolvido(Usuario funcionario) {
        for (Usuario envolvido : envolvidos) {
            if (envolvido.getMatricula().equals(funcionario.getMatricula())) {
                return;
            }
        }
        envolvidos.add(funcionario);

        Chip chip = new Chip(this);
        chip.setText(funcionario.getNome());
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            envolvidos.remove(funcionario);
            grupoEnvolvidos.removeView(chip);
        });
        grupoEnvolvidos.addView(chip);
    }

    private void salvar() {
        String titulo = String.valueOf(etTitulo.getText()).trim();
        String descricao = String.valueOf(etDescricao.getText()).trim();
        Prioridade prioridade = Prioridade.porRotulo(acPrioridade.getText().toString());
        TipoDemanda tipo = tipoSelecionado();

        boolean valido = exigir(campoTitulo, !titulo.isEmpty(), "Informe o título");
        valido &= exigir(campoDescricao, !descricao.isEmpty(), "Descreva a necessidade");
        valido &= exigir(campoPrioridade, prioridade != null, "Escolha a prioridade");
        valido &= exigir(campoTipo, tipo != null, "Escolha o tipo");
        if (!valido) {
            return;
        }

        btnSalvar.setEnabled(false);
        Resultado<Demanda> aoSalvar = new Resultado<Demanda>() {
            @Override
            public void sucesso(Demanda salva) {
                avisar("Demanda #" + salva.getId() + (emEdicao() ? " atualizada." : " criada."));
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void erro(String mensagem) {
                btnSalvar.setEnabled(true);
                avisar(mensagem);
            }
        };

        if (emEdicao()) {
            repositorio.editar(idEdicao, titulo, descricao, prioridade, tipo, aoSalvar);
        } else {
            repositorio.criar(titulo, descricao, prioridade, tipo, envolvidos, aoSalvar);
        }
    }

    private TipoDemanda tipoSelecionado() {
        String descricao = acTipo.getText().toString();
        for (TipoDemanda tipo : tipos) {
            if (tipo.getDescricao().equals(descricao)) {
                return tipo;
            }
        }
        return null;
    }

    // Mostra ou limpa a mensagem de erro do campo e devolve se ele está válido.
    private boolean exigir(TextInputLayout campo, boolean valido, String mensagem) {
        campo.setError(valido ? null : mensagem);
        return valido;
    }

    private <T> ArrayAdapter<T> opcoes(List<T> itens) {
        return new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itens);
    }

    private void avisar(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }
}
