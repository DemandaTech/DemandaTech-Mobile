package edu.udesc.demandatech.ui.demandas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.DemandaRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.Estorno;
import edu.udesc.demandatech.model.Status;
import edu.udesc.demandatech.model.Usuario;
import edu.udesc.demandatech.ui.Etiquetas;
import edu.udesc.demandatech.ui.Telas;
import edu.udesc.demandatech.ui.estornos.EstornoActivity;

public class DetalheDemandaActivity extends AppCompatActivity {

    private static final String ID = "id";

    private DemandaRepository repositorio;
    private Usuario usuario;
    private Demanda demanda;

    public static Intent intent(Context context, int id) {
        return new Intent(context, DetalheDemandaActivity.class).putExtra(ID, id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Telas.abrir(this, R.layout.activity_detalhe_demanda);
        repositorio = Repositorios.demandas(this);
        usuario = new Sessao(this).getUsuario();

        MaterialToolbar barra = findViewById(R.id.barra);
        barra.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btnStatus).setOnClickListener(v -> abrirStatus());
        findViewById(R.id.btnExcluir).setOnClickListener(v -> confirmarExclusao());
        findViewById(R.id.btnEstorno).setOnClickListener(v ->
                startActivity(EstornoActivity.novo(this, demanda.getId())));
        findViewById(R.id.btnEditar).setOnClickListener(v -> {
            setResult(RESULT_OK);
            startActivity(FormularioDemandaActivity.editar(this, demanda.getId()));
        });
    }

    // Recarrega também ao voltar da edição ou do registro de estorno.
    @Override
    protected void onResume() {
        super.onResume();
        int id = getIntent().getIntExtra(ID, 0);
        repositorio.buscar(id, new Atualizacao());
        carregarEstornos(id);
    }

    private void carregarEstornos(int id) {
        Repositorios.estornos(this).listarDaDemanda(id, new Resultado<List<Estorno>>() {
            @Override
            public void sucesso(List<Estorno> estornos) {
                List<String> linhas = new ArrayList<>();
                for (Estorno estorno : estornos) {
                    linhas.add(estorno.getCodigo() + " · " + estorno.getData() + "\n"
                            + estorno.getJustificativa());
                }
                texto(R.id.tvEstornos, linhas.isEmpty()
                        ? "Nenhum estorno vinculado a esta demanda."
                        : TextUtils.join("\n\n", linhas));
            }

            @Override
            public void erro(String mensagem) {
                texto(R.id.tvEstornos, mensagem);
            }
        });
    }

    private void mostrar(Demanda dados) {
        demanda = dados;
        texto(R.id.tvTitulo, "#" + dados.getId() + " · " + dados.getTitulo());
        texto(R.id.tvAbertura, "Aberta em " + dados.getAbertura());
        texto(R.id.tvDescricao, dados.getDescricao());
        texto(R.id.tvResponsavel, dados.getResponsavel().getNome());
        texto(R.id.tvCriadoPor, dados.getCriador().getNome());
        texto(R.id.tvDepartamento, dados.getCriador().getDepartamento());
        texto(R.id.tvEnvolvidos, dados.getEnvolvidos().isEmpty()
                ? "Nenhum funcionário envolvido."
                : TextUtils.join("\n", dados.getEnvolvidos()));

        Etiquetas.status(findViewById(R.id.tvStatus), dados.getStatus());
        Etiquetas.prioridade(findViewById(R.id.tvPrioridade), dados.getPrioridade());
        Etiquetas.neutra(findViewById(R.id.tvTipo), dados.getTipo().getDescricao());

        texto(R.id.tvJustificativa, dados.getJustificativa());
        visivel(R.id.cartaoJustificativa, !TextUtils.isEmpty(dados.getJustificativa()));

        // O backend valida a permissão; aqui só mostramos as ações de quem pode usá-las.
        visivel(R.id.btnStatus, dados.podeAlterarStatus(usuario));
        visivel(R.id.btnEditar, dados.podeEditar(usuario));
        visivel(R.id.btnExcluir, dados.podeExcluir(usuario));
        visivel(R.id.btnEstorno, dados.podeRegistrarEstorno(usuario));
        findViewById(R.id.conteudo).setVisibility(View.VISIBLE);
    }

    // Mostra só os status seguintes ao atual, porque o status não regride.
    private void abrirStatus() {
        View formulario = getLayoutInflater().inflate(R.layout.dialog_responder, null);
        ChipGroup grupoStatus = formulario.findViewById(R.id.grupoStatus);
        TextInputLayout campoJustificativa = formulario.findViewById(R.id.campoJustificativa);
        TextInputEditText etJustificativa = formulario.findViewById(R.id.etJustificativa);

        for (Status status : Status.values()) {
            if (!demanda.getStatus().podeAvancarPara(status)) {
                continue;
            }
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_filtro, grupoStatus, false);
            chip.setId(View.generateViewId());
            chip.setText(status.getRotulo());
            chip.setTag(status);
            grupoStatus.addView(chip);
        }

        // A justificativa é opcional e só aparece ao resolver a demanda.
        grupoStatus.setOnCheckedStateChangeListener((grupo, marcados) ->
                campoJustificativa.setVisibility(
                        statusMarcado(grupo) == Status.RESOLVIDA ? View.VISIBLE : View.GONE));
        ((Chip) grupoStatus.getChildAt(0)).setChecked(true);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Alterar status")
                .setView(formulario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialogo, botao) -> {
                    Status novo = statusMarcado(grupoStatus);
                    String justificativa = novo == Status.RESOLVIDA
                            ? String.valueOf(etJustificativa.getText()).trim()
                            : null;
                    setResult(RESULT_OK);
                    repositorio.alterarStatus(demanda.getId(), novo, justificativa, new Atualizacao());
                })
                .show();
    }

    private Status statusMarcado(ChipGroup grupo) {
        View chip = grupo.findViewById(grupo.getCheckedChipId());
        return chip == null ? null : (Status) chip.getTag();
    }

    private void confirmarExclusao() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir demanda")
                .setMessage("A demanda #" + demanda.getId() + " será removida. Deseja continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialogo, botao) -> excluir())
                .show();
    }

    private void excluir() {
        repositorio.excluir(demanda.getId(), new Resultado<Void>() {
            @Override
            public void sucesso(Void dados) {
                avisar("Demanda excluída.");
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void erro(String mensagem) {
                avisar(mensagem);
            }
        });
    }

    private void texto(int id, String valor) {
        TextView view = findViewById(id);
        view.setText(valor);
    }

    private void visivel(int id, boolean visivel) {
        findViewById(id).setVisibility(visivel ? View.VISIBLE : View.GONE);
    }

    private void avisar(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    // Atualiza a tela sempre que a API devolve a demanda.
    private class Atualizacao implements Resultado<Demanda> {

        @Override
        public void sucesso(Demanda dados) {
            mostrar(dados);
        }

        @Override
        public void erro(String mensagem) {
            avisar(mensagem);
            if (demanda == null) {
                finish();
            }
        }
    }
}
