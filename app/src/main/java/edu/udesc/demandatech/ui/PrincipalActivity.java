package edu.udesc.demandatech.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import edu.udesc.demandatech.MainActivity;
import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.Usuario;
import edu.udesc.demandatech.ui.cadastros.CadastrosFragment;
import edu.udesc.demandatech.ui.demandas.DemandasFragment;
import edu.udesc.demandatech.ui.estornos.EstornosFragment;
import edu.udesc.demandatech.ui.relatorios.RelatoriosFragment;
import edu.udesc.demandatech.ui.relatorios.VisaoGeralFragment;

// Tela principal: barra superior, conteúdo da aba e barra inferior montada conforme o perfil.
public class PrincipalActivity extends AppCompatActivity {

    private static final String TITULO = "titulo";

    private Sessao sessao;
    private TextView tvTitulo;
    private BottomNavigationView navegacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessao = new Sessao(this);
        if (!sessao.estaLogado()) {
            voltarParaLogin();
            return;
        }

        Telas.abrir(this, R.layout.activity_principal);
        tvTitulo = findViewById(R.id.tvTitulo);
        navegacao = findViewById(R.id.navegacao);

        Usuario usuario = sessao.getUsuario();
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        tvAvatar.setText(usuario.getIniciais());
        tvAvatar.setOnClickListener(v -> mostrarConta(usuario));

        montarMenu(usuario.getPerfil());
        navegacao.setOnItemSelectedListener(item -> {
            abrirAba(item);
            return true;
        });

        if (savedInstanceState == null) {
            abrirAba(navegacao.getMenu().getItem(0));
        } else {
            tvTitulo.setText(savedInstanceState.getCharSequence(TITULO));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tvTitulo != null) {
            outState.putCharSequence(TITULO, tvTitulo.getText());
        }
    }

    // O backend é quem garante o acesso; aqui só escondemos o que o perfil não usa.
    private void montarMenu(Perfil perfil) {
        Menu menu = navegacao.getMenu();
        if (perfil.isGestor()) {
            menu.add(Menu.NONE, R.id.nav_visao_geral, Menu.NONE, "Visão geral")
                    .setIcon(R.drawable.ic_visao_geral);
        }
        menu.add(Menu.NONE, R.id.nav_demandas, Menu.NONE, "Demandas")
                .setIcon(R.drawable.ic_demandas);
        menu.add(Menu.NONE, R.id.nav_estornos, Menu.NONE, "Estornos")
                .setIcon(R.drawable.ic_estornos);
        if (perfil.isGestor()) {
            menu.add(Menu.NONE, R.id.nav_cadastros, Menu.NONE, "Cadastros")
                    .setIcon(R.drawable.ic_cadastros);
        }
        if (perfil == Perfil.ADMINISTRADOR) {
            menu.add(Menu.NONE, R.id.nav_relatorios, Menu.NONE, "Relatórios")
                    .setIcon(R.drawable.ic_relatorios);
        }
    }

    private void abrirAba(MenuItem item) {
        String titulo = String.valueOf(item.getTitle());
        tvTitulo.setText(titulo);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.conteudo, criarAba(item.getItemId(), titulo))
                .commit();
    }

    // Usado pela Visão geral em "Ver todas as demandas".
    public void abrirDemandas() {
        navegacao.setSelectedItemId(R.id.nav_demandas);
    }

    private Fragment criarAba(int id, String titulo) {
        if (id == R.id.nav_visao_geral) {
            return new VisaoGeralFragment();
        }
        if (id == R.id.nav_relatorios) {
            return new RelatoriosFragment();
        }
        if (id == R.id.nav_demandas) {
            return new DemandasFragment();
        }
        if (id == R.id.nav_estornos) {
            return new EstornosFragment();
        }
        if (id == R.id.nav_cadastros) {
            return new CadastrosFragment();
        }
        return EmConstrucaoFragment.novo(titulo);
    }

    private void mostrarConta(Usuario usuario) {
        View conta = getLayoutInflater().inflate(R.layout.dialog_conta, null);
        definirTexto(conta, R.id.tvAvatarConta, usuario.getIniciais());
        definirTexto(conta, R.id.tvNome, usuario.getNome());
        definirTexto(conta, R.id.tvMatricula, "Matrícula " + usuario.getMatricula());
        definirTexto(conta, R.id.tvDepartamento, usuario.getDepartamento());
        Etiquetas.neutra(conta.findViewById(R.id.tvPerfil), usuario.getPerfil().getRotulo());

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this).setView(conta).show();
        conta.findViewById(R.id.btnFechar).setOnClickListener(v -> dialogo.dismiss());
        conta.findViewById(R.id.btnSair).setOnClickListener(v -> {
            dialogo.dismiss();
            sessao.encerrar();
            voltarParaLogin();
        });
    }

    private void definirTexto(View raiz, int id, String texto) {
        TextView view = raiz.findViewById(id);
        view.setText(texto);
    }

    private void voltarParaLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
