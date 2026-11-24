package edu.udesc.demandatech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.AuthRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.Departamento;
import edu.udesc.demandatech.model.Usuario;

// Cadastro feito pela própria pessoa, a partir da tela de login.
public class CriarContaActivity extends AppCompatActivity {

    public static final String MATRICULA = "matricula";

    private AuthRepository auth;

    private TextInputLayout campoNome;
    private TextInputLayout campoEmail;
    private TextInputLayout campoDepartamento;
    private TextInputLayout campoSenha;
    private TextInputLayout campoConfirmacao;
    private TextInputEditText etNome;
    private TextInputEditText etEmail;
    private TextInputEditText etSenha;
    private TextInputEditText etConfirmacao;
    private MaterialAutoCompleteTextView acDepartamento;
    private MaterialButton btnCriar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Telas.abrir(this, R.layout.activity_criar_conta);
        auth = Repositorios.auth();

        campoNome = findViewById(R.id.campoNome);
        campoEmail = findViewById(R.id.campoEmail);
        campoDepartamento = findViewById(R.id.campoDepartamento);
        campoSenha = findViewById(R.id.campoSenha);
        campoConfirmacao = findViewById(R.id.campoConfirmacao);
        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        etConfirmacao = findViewById(R.id.etConfirmacao);
        acDepartamento = findViewById(R.id.acDepartamento);
        btnCriar = findViewById(R.id.btnCriar);

        MaterialToolbar barra = findViewById(R.id.barra);
        barra.setNavigationOnClickListener(v -> finish());
        btnCriar.setOnClickListener(v -> criar());
        carregarDepartamentos();
    }

    private void carregarDepartamentos() {
        auth.listarDepartamentos(new Resultado<List<Departamento>>() {
            @Override
            public void sucesso(List<Departamento> departamentos) {
                acDepartamento.setAdapter(new ArrayAdapter<>(CriarContaActivity.this,
                        android.R.layout.simple_list_item_1, departamentos));
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
            }
        });
    }

    private void criar() {
        String nome = String.valueOf(etNome.getText()).trim();
        String email = String.valueOf(etEmail.getText()).trim();
        String departamento = acDepartamento.getText().toString();
        String senha = String.valueOf(etSenha.getText());
        String confirmacao = String.valueOf(etConfirmacao.getText());

        boolean emailValido = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean valido = exigir(campoNome, !nome.isEmpty(), "Informe o nome completo");
        valido &= exigir(campoEmail, emailValido,
                "Informe um e-mail válido, como nome@empresa.com.br");
        valido &= exigir(campoDepartamento, !departamento.isEmpty(), "Escolha o departamento");
        valido &= exigir(campoSenha, !senha.isEmpty(), "Informe a senha");
        valido &= exigir(campoConfirmacao, senha.equals(confirmacao), "As senhas não conferem");
        if (!valido) {
            return;
        }

        btnCriar.setEnabled(false);
        auth.criarConta(nome, email, departamento, senha, new Resultado<Usuario>() {
            @Override
            public void sucesso(Usuario usuario) {
                mostrarMatricula(usuario.getMatricula());
            }

            @Override
            public void erro(String mensagem) {
                btnCriar.setEnabled(true);
                avisar(mensagem);
            }
        });
    }

    // O login é feito pela matrícula, então ela precisa ficar clara antes de voltar.
    private void mostrarMatricula(String matricula) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Conta criada")
                .setMessage("Sua matrícula é " + matricula + ". Use-a para entrar no sistema.")
                .setCancelable(false)
                .setPositiveButton("Ir para o login", (dialogo, botao) -> {
                    setResult(RESULT_OK, new Intent().putExtra(MATRICULA, matricula));
                    finish();
                })
                .show();
    }

    // Mostra ou limpa a mensagem de erro do campo e devolve se ele está válido.
    private boolean exigir(TextInputLayout campo, boolean valido, String mensagem) {
        campo.setError(valido ? null : mensagem);
        return valido;
    }

    private void avisar(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }
}
