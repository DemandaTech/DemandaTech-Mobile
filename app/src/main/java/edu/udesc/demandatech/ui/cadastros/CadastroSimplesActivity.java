package edu.udesc.demandatech.ui.cadastros;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.CadastroRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.Cadastro;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.TipoDemanda;
import edu.udesc.demandatech.model.Usuario;
import edu.udesc.demandatech.ui.Telas;

// Formulário de departamento e de tipo de demanda. Os dois têm uma descrição e uma seleção:
// o gerente (opcional) no departamento e o departamento responsável (obrigatório) no tipo.
public class CadastroSimplesActivity extends AppCompatActivity {

    private static final String CADASTRO = "cadastro";
    private static final String ID = "id";
    private static final long NOVO = 0;

    private CadastroRepository repositorio;
    private boolean ehDepartamento;
    private long id;
    // Gerente ou departamento escolhido na seleção.
    private Object opcao;

    private TextInputLayout campoDescricao;
    private TextInputLayout campoOpcao;
    private TextInputEditText etDescricao;
    private MaterialAutoCompleteTextView acOpcao;
    private View btnSalvar;

    public static Intent novo(Context context, Cadastro cadastro) {
        return new Intent(context, CadastroSimplesActivity.class)
                .putExtra(CADASTRO, cadastro.name());
    }

    public static Intent editar(Context context, Cadastro cadastro, long id) {
        return novo(context, cadastro).putExtra(ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Telas.abrir(this, R.layout.activity_cadastro_simples);
        repositorio = Repositorios.cadastros(this);
        ehDepartamento = Cadastro.DEPARTAMENTOS.name().equals(getIntent().getStringExtra(CADASTRO));
        id = getIntent().getLongExtra(ID, NOVO);

        campoDescricao = findViewById(R.id.campoDescricao);
        campoOpcao = findViewById(R.id.campoOpcao);
        etDescricao = findViewById(R.id.etDescricao);
        acOpcao = findViewById(R.id.acOpcao);
        btnSalvar = findViewById(R.id.btnSalvar);
        View btnRemover = findViewById(R.id.btnRemover);

        String nome = ehDepartamento ? "departamento" : "tipo de demanda";
        MaterialToolbar barra = findViewById(R.id.barra);
        barra.setTitle((emEdicao() ? "Editar " : "Novo ") + nome);
        barra.setNavigationOnClickListener(v -> finish());
        campoOpcao.setHint(ehDepartamento ? "Gerente" : "Departamento responsável *");

        acOpcao.setOnItemClickListener((lista, view, posicao, linha) ->
                opcao = lista.getItemAtPosition(posicao));
        btnSalvar.setOnClickListener(v -> salvar());
        btnRemover.setOnClickListener(v -> confirmarRemocao());
        btnRemover.setVisibility(emEdicao() ? View.VISIBLE : View.GONE);

        carregarOpcoes();
        if (emEdicao()) {
            carregarRegistro();
        }
    }

    private boolean emEdicao() {
        return id != NOVO;
    }

    private void carregarOpcoes() {
        if (ehDepartamento) {
            Repositorios.demandas(this).listarFuncionarios(new Opcoes<Usuario>());
        } else {
            repositorio.listarDepartamentos(new Opcoes<Departamento>());
        }
    }

    private void carregarRegistro() {
        if (ehDepartamento) {
            repositorio.buscarDepartamento(id, new Resultado<Departamento>() {
                @Override
                public void sucesso(Departamento departamento) {
                    mostrar(departamento.getDescricao(), departamento.getGerente());
                }

                @Override
                public void erro(String mensagem) {
                    fecharComErro(mensagem);
                }
            });
        } else {
            repositorio.buscarTipo(id, new Resultado<TipoDemanda>() {
                @Override
                public void sucesso(TipoDemanda tipo) {
                    mostrar(tipo.getDescricao(), tipo.getDepartamento());
                }

                @Override
                public void erro(String mensagem) {
                    fecharComErro(mensagem);
                }
            });
        }
    }

    private void mostrar(String descricao, Object selecionado) {
        etDescricao.setText(descricao);
        opcao = selecionado;
        if (selecionado != null) {
            acOpcao.setText(selecionado.toString(), false);
        }
    }

    private void salvar() {
        String descricao = String.valueOf(etDescricao.getText()).trim();
        boolean valido = exigir(campoDescricao, !descricao.isEmpty(), "Informe a descrição");
        valido &= exigir(campoOpcao, ehDepartamento || opcao != null, "Escolha o departamento");
        if (!valido) {
            return;
        }

        btnSalvar.setEnabled(false);
        if (ehDepartamento) {
            repositorio.salvarDepartamento(id, descricao, (Usuario) opcao, new Conclusao<>());
        } else {
            repositorio.salvarTipo(id, descricao, (Departamento) opcao, new Conclusao<>());
        }
    }

    private void confirmarRemocao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remover registro")
                .setMessage("O cadastro será apagado. Deseja continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover", (dialogo, botao) -> remover())
                .show();
    }

    private void remover() {
        if (ehDepartamento) {
            repositorio.removerDepartamento(id, new Conclusao<>());
        } else {
            repositorio.removerTipo(id, new Conclusao<>());
        }
    }

    private void fecharComErro(String mensagem) {
        avisar(mensagem);
        finish();
    }

    // Mostra ou limpa a mensagem de erro do campo e devolve se ele está válido.
    private boolean exigir(TextInputLayout campo, boolean valido, String mensagem) {
        campo.setError(valido ? null : mensagem);
        return valido;
    }

    private void avisar(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    // Preenche a seleção com os funcionários ou departamentos recebidos.
    private class Opcoes<T> implements Resultado<List<T>> {

        @Override
        public void sucesso(List<T> itens) {
            acOpcao.setAdapter(new ArrayAdapter<>(CadastroSimplesActivity.this,
                    android.R.layout.simple_list_item_1, itens));
        }

        @Override
        public void erro(String mensagem) {
            avisar(mensagem);
        }
    }

    // Depois de salvar ou remover: fecha avisando a lista de que algo mudou.
    private class Conclusao<T> implements Resultado<T> {

        @Override
        public void sucesso(T dados) {
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void erro(String mensagem) {
            btnSalvar.setEnabled(true);
            avisar(mensagem);
        }
    }
}
