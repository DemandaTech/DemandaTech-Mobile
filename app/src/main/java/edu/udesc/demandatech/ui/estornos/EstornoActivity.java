package edu.udesc.demandatech.ui.estornos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.EstornoRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.ui.Telas;

// Uma tela só para registrar um estorno novo e para consultar ou editar um existente.
public class EstornoActivity extends AppCompatActivity {

    private static final String ID_ESTORNO = "id_estorno";
    private static final String ID_DEMANDA = "id_demanda";

    private EstornoRepository repositorio;
    private int idEstorno;

    private MaterialToolbar barra;
    private TextInputLayout campoJustificativa;
    private TextInputEditText etJustificativa;
    private MaterialButton btnSalvar;
    private View btnRemover;

    public static Intent novo(Context context, int idDemanda) {
        return new Intent(context, EstornoActivity.class).putExtra(ID_DEMANDA, idDemanda);
    }

    public static Intent existente(Context context, int idEstorno) {
        return new Intent(context, EstornoActivity.class).putExtra(ID_ESTORNO, idEstorno);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Telas.abrir(this, R.layout.activity_estorno);
        repositorio = Repositorios.estornos(this);
        idEstorno = getIntent().getIntExtra(ID_ESTORNO, 0);

        barra = findViewById(R.id.barra);
        campoJustificativa = findViewById(R.id.campoJustificativa);
        etJustificativa = findViewById(R.id.etJustificativa);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnRemover = findViewById(R.id.btnRemover);

        barra.setNavigationOnClickListener(v -> finish());
        btnSalvar.setOnClickListener(v -> salvar());
        btnRemover.setOnClickListener(v -> confirmarRemocao());

        if (ehNovo()) {
            prepararNovo();
        } else {
            repositorio.buscar(idEstorno, new Resultado<Estorno>() {
                @Override
                public void sucesso(Estorno estorno) {
                    mostrar(estorno);
                }

                @Override
                public void erro(String mensagem) {
                    avisar(mensagem);
                    finish();
                }
            });
        }
    }

    private boolean ehNovo() {
        return idEstorno == 0;
    }

    private void prepararNovo() {
        int idDemanda = getIntent().getIntExtra(ID_DEMANDA, 0);
        Repositorios.demandas(this).buscar(idDemanda, new Resultado<Demanda>() {
            @Override
            public void sucesso(Demanda demanda) {
                barra.setTitle("Novo estorno");
                mostrarDemanda(demanda);
                findViewById(R.id.conteudo).setVisibility(View.VISIBLE);
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
                finish();
            }
        });
    }

    private void mostrar(Estorno estorno) {
        barra.setTitle("Estorno " + estorno.getCodigo());
        mostrarDemanda(estorno.getDemanda());
        etJustificativa.setText(estorno.getJustificativa());
        texto(R.id.tvRegistro, "Registrado em " + estorno.getData() + " por "
                + estorno.getDemanda().getCriador().getNome());

        // Quem não criou a demanda apenas consulta o estorno.
        boolean podeEditar = estorno.podeEditar(new Sessao(this).getUsuario());
        etJustificativa.setEnabled(podeEditar);
        btnSalvar.setText("Salvar alterações");
        btnSalvar.setVisibility(podeEditar ? View.VISIBLE : View.GONE);
        btnRemover.setVisibility(podeEditar ? View.VISIBLE : View.GONE);
        findViewById(R.id.conteudo).setVisibility(View.VISIBLE);
    }

    private void mostrarDemanda(Demanda demanda) {
        texto(R.id.tvDemanda, "#" + demanda.getId() + " · " + demanda.getTitulo());
        texto(R.id.tvResponsavel, "Responsável: " + demanda.getResponsavel().getNome());
    }

    private void salvar() {
        String justificativa = String.valueOf(etJustificativa.getText()).trim();
        if (justificativa.isEmpty()) {
            campoJustificativa.setError("Informe a justificativa");
            return;
        }
        campoJustificativa.setError(null);
        btnSalvar.setEnabled(false);

        Resultado<Estorno> aoSalvar = new Resultado<Estorno>() {
            @Override
            public void sucesso(Estorno estorno) {
                avisar("Estorno " + estorno.getCodigo() + (ehNovo() ? " registrado." : " atualizado."));
                concluir();
            }

            @Override
            public void erro(String mensagem) {
                btnSalvar.setEnabled(true);
                avisar(mensagem);
            }
        };

        if (ehNovo()) {
            int idDemanda = getIntent().getIntExtra(ID_DEMANDA, 0);
            repositorio.registrar(idDemanda, justificativa, aoSalvar);
        } else {
            repositorio.editar(idEstorno, justificativa, aoSalvar);
        }
    }

    private void confirmarRemocao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remover estorno")
                .setMessage("O registro deste estorno será apagado. Deseja continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover", (dialogo, botao) -> remover())
                .show();
    }

    private void remover() {
        repositorio.remover(idEstorno, new Resultado<Void>() {
            @Override
            public void sucesso(Void dados) {
                avisar("Estorno removido.");
                concluir();
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
            }
        });
    }

    // Fecha avisando a tela anterior de que a lista mudou.
    private void concluir() {
        setResult(RESULT_OK);
        finish();
    }

    private void texto(int id, String valor) {
        TextView view = findViewById(id);
        view.setText(valor);
    }

    private void avisar(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }
}
