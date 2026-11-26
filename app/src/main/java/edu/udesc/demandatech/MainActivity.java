package edu.udesc.demandatech;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import edu.udesc.demandatech.data.AuthRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Autenticacao;
import edu.udesc.demandatech.ui.CriarContaActivity;
import edu.udesc.demandatech.ui.PrincipalActivity;
import edu.udesc.demandatech.ui.Telas;

public class MainActivity extends AppCompatActivity {

    private Sessao sessao;
    private AuthRepository auth;

    private EditText etMatricula;
    private EditText etSenha;
    private CheckBox cbManterConectado;
    private TextView tvErro;
    private MaterialButton btnEntrar;

    private final ActivityResultLauncher<Intent> criarConta = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), retorno -> {
                if (retorno.getResultCode() == RESULT_OK && retorno.getData() != null) {
                    etMatricula.setText(
                            retorno.getData().getStringExtra(CriarContaActivity.MATRICULA));
                    etSenha.requestFocus();
                }
            });

    private static final String REDE_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK";

    private final ActivityResultLauncher<String> pedirRedeLocal = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedida -> {
                if (!concedida) {
                    tvErro.setText("Sem acesso à rede local o app não consegue falar com o servidor.");
                    tvErro.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessao = new Sessao(this);
        auth = Repositorios.auth();

        if (sessao.estaLogado()) {
            if (sessao.manterConectado()) {
                abrirPrincipal();
                return;
            }
            sessao.encerrar();
        }

        Telas.abrir(this, R.layout.login);
        etMatricula = findViewById(R.id.etMatricula);
        etSenha = findViewById(R.id.etSenha);
        cbManterConectado = findViewById(R.id.cbManterConectado);
        tvErro = findViewById(R.id.tvErro);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> entrar());
        findViewById(R.id.tvCriarConta).setOnClickListener(v ->
                criarConta.launch(new Intent(this, CriarContaActivity.class)));
        findViewById(R.id.tvEsqueciSenha).setOnClickListener(v -> Toast.makeText(this,
                "Procure o administrador para redefinir sua senha.", Toast.LENGTH_LONG).show());

        if (Build.VERSION.SDK_INT >= 37
                && ContextCompat.checkSelfPermission(this, REDE_LOCAL) != PackageManager.PERMISSION_GRANTED) {
            pedirRedeLocal.launch(REDE_LOCAL);
        }
    }

    private void entrar() {
        String matricula = etMatricula.getText().toString().trim();
        String senha = etSenha.getText().toString();
        if (!camposValidos(matricula, senha)) {
            return;
        }

        tvErro.setVisibility(View.GONE);
        btnEntrar.setEnabled(false);
        auth.entrar(matricula, senha, new Resultado<Autenticacao>() {
            @Override
            public void sucesso(Autenticacao dados) {
                sessao.salvar(dados.getUsuario(), dados.getToken(), cbManterConectado.isChecked());
                abrirPrincipal();
            }

            @Override
            public void erro(String mensagem) {
                btnEntrar.setEnabled(true);
                tvErro.setText(mensagem);
                tvErro.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean camposValidos(String matricula, String senha) {
        if (matricula.isEmpty()) {
            etMatricula.setError("Informe sua matrícula");
            etMatricula.requestFocus();
            return false;
        }
        if (senha.isEmpty()) {
            etSenha.setError("Informe sua senha");
            etSenha.requestFocus();
            return false;
        }
        return true;
    }

    private void abrirPrincipal() {
        startActivity(new Intent(this, PrincipalActivity.class));
        finish();
    }
}
