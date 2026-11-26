package edu.udesc.demandatech.ui.cadastros;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.CadastroRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.Usuario;
import edu.udesc.demandatech.ui.Telas;

public class FuncionarioActivity extends AppCompatActivity {
    private static final String MATRICULA = "matricula";
    private CadastroRepository repositorio;
    private Usuario logado;
    private String matricula;
    private Perfil perfil = Perfil.USUARIO;
    private TextInputLayout campoNome;
    private TextInputLayout campoEmail;
    private TextInputLayout campoDepartamento;
    private TextInputLayout campoSenha;
    private TextInputEditText etNome;
    private TextInputEditText etEmail;
    private TextInputEditText etSenha;
    private MaterialAutoCompleteTextView acPerfil;
    private MaterialAutoCompleteTextView acDepartamento;
    private MaterialButton btnSalvar;

    public static Intent novo(Context context) {
        return new Intent(context, FuncionarioActivity.class);
    }

    public static Intent editar(Context context, String matricula) {
        return novo(context).putExtra(MATRICULA, matricula);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Telas.abrir(this, R.layout.activity_funcionario);
        repositorio = Repositorios.cadastros(this);
        logado = new Sessao(this).getUsuario();
        matricula = getIntent().getStringExtra(MATRICULA);

        campoNome = findViewById(R.id.campoNome);
        campoEmail = findViewById(R.id.campoEmail);
        campoDepartamento = findViewById(R.id.campoDepartamento);
        campoSenha = findViewById(R.id.campoSenha);
        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        acPerfil = findViewById(R.id.acPerfil);
        acDepartamento = findViewById(R.id.acDepartamento);
        btnSalvar = findViewById(R.id.btnSalvar);

        MaterialToolbar barra = findViewById(R.id.barra);
        barra.setNavigationOnClickListener(v -> finish());
        btnSalvar.setOnClickListener(v -> salvar());
        findViewById(R.id.btnRemover).setOnClickListener(v -> confirmarRemocao());

        configurarPerfil();
        configurarDepartamento();
        if (emEdicao()) {
            barra.setTitle("Editar funcionário");
            btnSalvar.setText("Salvar alterações");
            campoSenha.setHint("Nova senha");
            campoSenha.setHelperText("Deixe em branco para manter a senha atual.");
            carregarFuncionario();
        }
    }

    private boolean emEdicao() {
        return matricula != null;
    }

    private void configurarPerfil() {
        List<String> perfis = new ArrayList<>();
        for (Perfil opcao : Perfil.values()) {
            perfis.add(opcao.getRotulo());
        }
        acPerfil.setAdapter(opcoes(perfis));
        acPerfil.setText(perfil.getRotulo(), false);
        acPerfil.setOnItemClickListener((lista, view, posicao, id) ->
                perfil = Perfil.porRotulo((String) lista.getItemAtPosition(posicao)));
        if (!logado.isAdministrador()) {
            findViewById(R.id.campoPerfil).setVisibility(View.GONE);
        }
    }

    private void configurarDepartamento() {
        if (!logado.isAdministrador()) {
            acDepartamento.setText(logado.getDepartamento(), false);
            campoDepartamento.setEnabled(false);
            return;
        }
        repositorio.listarDepartamentos(new Resultado<List<Departamento>>() {
            @Override
            public void sucesso(List<Departamento> departamentos) {
                acDepartamento.setAdapter(opcoes(departamentos));
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
            }
        });
    }

    private void carregarFuncionario() {
        repositorio.buscarFuncionario(matricula, new Resultado<Usuario>() {
            @Override
            public void sucesso(Usuario funcionario) {
                mostrar(funcionario);
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
                finish();
            }
        });
    }

    private void mostrar(Usuario funcionario) {
        TextInputEditText etMatricula = findViewById(R.id.etMatricula);
        etMatricula.setText(funcionario.getMatricula());
        findViewById(R.id.campoMatricula).setVisibility(View.VISIBLE);

        etNome.setText(funcionario.getNome());
        etEmail.setText(funcionario.getEmail());
        acDepartamento.setText(funcionario.getDepartamento(), false);
        perfil = funcionario.getPerfil();
        acPerfil.setText(perfil.getRotulo(), false);

        boolean ehOProprio = funcionario.getMatricula().equals(logado.getMatricula());
        findViewById(R.id.btnRemover).setVisibility(ehOProprio ? View.GONE : View.VISIBLE);
    }

    private void salvar() {
        String nome = String.valueOf(etNome.getText()).trim();
        String email = String.valueOf(etEmail.getText()).trim();
        String departamento = acDepartamento.getText().toString();
        String senha = String.valueOf(etSenha.getText());

        boolean emailValido = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean valido = exigir(campoNome, !nome.isEmpty(), "Informe o nome completo");
        valido &= exigir(campoEmail, emailValido,
                "Informe um e-mail válido, como nome@empresa.com.br");
        valido &= exigir(campoDepartamento, !departamento.isEmpty(), "Escolha o departamento");
        valido &= exigir(campoSenha, emEdicao() || !senha.isEmpty(), "Informe a senha");
        if (!valido) {
            return;
        }

        btnSalvar.setEnabled(false);
        repositorio.salvarFuncionario(matricula, nome, email, perfil, departamento, senha,
                new Resultado<Usuario>() {
                    @Override
                    public void sucesso(Usuario funcionario) {
                        avisar(emEdicao()
                                ? "Funcionário atualizado."
                                : "Funcionário cadastrado com a matrícula "
                                        + funcionario.getMatricula() + ".");
                        concluir();
                    }

                    @Override
                    public void erro(String mensagem) {
                        btnSalvar.setEnabled(true);
                        avisar(mensagem);
                    }
                });
    }

    private void confirmarRemocao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remover funcionário")
                .setMessage("O cadastro será apagado. Deseja continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover", (dialogo, botao) -> remover())
                .show();
    }

    private void remover() {
        repositorio.removerFuncionario(matricula, new Resultado<Void>() {
            @Override
            public void sucesso(Void dados) {
                avisar("Funcionário removido.");
                concluir();
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
            }
        });
    }

    private void concluir() {
        setResult(RESULT_OK);
        finish();
    }

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
