package edu.udesc.demandatech.ui.relatorios;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.RelatorioRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;
import edu.udesc.demandatech.model.Demanda;
import edu.udesc.demandatech.model.VisaoGeral;
import edu.udesc.demandatech.ui.Etiquetas;
import edu.udesc.demandatech.ui.Indicadores;
import edu.udesc.demandatech.ui.PrincipalActivity;
import edu.udesc.demandatech.ui.demandas.DetalheDemandaActivity;
import edu.udesc.demandatech.ui.demandas.FormularioDemandaActivity;

// Visão geral do Administrador (toda a operação) e do Gerente (o departamento dele).
public class VisaoGeralFragment extends Fragment {

    private static final Locale BRASIL = new Locale("pt", "BR");
    // Quantas linhas do ranking aparecem antes de tocar em "Ver todos".
    private static final int RANKING_RESUMIDO = 5;

    private RelatorioRepository repositorio;
    private VisaoGeral dados;
    private boolean rankingCompleto;

    private View raiz;
    private TextView tvEstado;
    private TextView btnVerRanking;
    private LinearLayout listaRecentes;
    private LinearLayout listaRanking;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_visao_geral, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estado) {
        repositorio = Repositorios.relatorios(requireContext());
        raiz = view;
        tvEstado = view.findViewById(R.id.tvEstado);
        btnVerRanking = view.findViewById(R.id.btnVerRanking);
        listaRecentes = view.findViewById(R.id.listaRecentes);
        listaRanking = view.findViewById(R.id.listaRanking);

        Indicadores.rotular(view, R.id.indicadorTotal, "Demandas");
        Indicadores.rotular(view, R.id.indicadorAbertas, "Abertas");
        Indicadores.rotular(view, R.id.indicadorAguardando, "Aguardando");
        Indicadores.rotular(view, R.id.indicadorTaxa, "Taxa de resolução");
        configurarTitulos();

        tvEstado.setOnClickListener(v -> carregar());
        btnVerRanking.setOnClickListener(v -> {
            rankingCompleto = !rankingCompleto;
            mostrarRanking();
        });
        view.findViewById(R.id.btnVerDemandas).setOnClickListener(v ->
                ((PrincipalActivity) requireActivity()).abrirDemandas());
        view.findViewById(R.id.btnNova).setOnClickListener(v ->
                startActivity(FormularioDemandaActivity.nova(requireContext())));
    }

    // Recarrega ao voltar de uma demanda ou do formulário.
    @Override
    public void onResume() {
        super.onResume();
        carregar();
    }

    // O Administrador acompanha os técnicos da TI; o Gerente, o próprio departamento.
    private void configurarTitulos() {
        boolean administrador = new Sessao(requireContext()).getUsuario().isAdministrador();
        TextView tvTitulo = raiz.findViewById(R.id.tvTituloRanking);
        TextView tvSubtitulo = raiz.findViewById(R.id.tvSubtituloRanking);
        tvTitulo.setText(administrador ? "Técnicos com mais demandas na fila" : "Demandas por funcionário");
        tvSubtitulo.setText(administrador
                ? "Abertas ou aguardando, por técnico da TI"
                : "Abertas pelos funcionários do seu departamento");
    }

    private void carregar() {
        tvEstado.setVisibility(View.GONE);
        repositorio.visaoGeral(new Resultado<VisaoGeral>() {
            @Override
            public void sucesso(VisaoGeral visao) {
                if (getView() != null) {
                    mostrar(visao);
                }
            }

            @Override
            public void erro(String mensagem) {
                if (getView() != null) {
                    tvEstado.setText(mensagem + "\nToque para tentar novamente.");
                    tvEstado.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void mostrar(VisaoGeral visao) {
        dados = visao;
        Indicadores.valorar(raiz, R.id.indicadorTotal, String.valueOf(visao.total));
        Indicadores.valorar(raiz, R.id.indicadorAbertas, String.valueOf(visao.abertas));
        Indicadores.valorar(raiz, R.id.indicadorAguardando, String.valueOf(visao.aguardando));
        Indicadores.valorar(raiz, R.id.indicadorTaxa,
                String.format(BRASIL, "%.1f%%", visao.taxaResolucao));
        mostrarRecentes();
        mostrarRanking();
    }

    private void mostrarRecentes() {
        listaRecentes.removeAllViews();
        for (Demanda demanda : dados.recentes) {
            View linha = getLayoutInflater().inflate(R.layout.item_recente, listaRecentes, false);
            TextView tvId = linha.findViewById(R.id.tvId);
            TextView tvTitulo = linha.findViewById(R.id.tvTitulo);
            tvId.setText("#" + demanda.getId());
            tvTitulo.setText(demanda.getTitulo());
            Etiquetas.status(linha.findViewById(R.id.tvStatus), demanda.getStatus());
            linha.setOnClickListener(v -> startActivity(
                    DetalheDemandaActivity.intent(requireContext(), demanda.getId())));
            listaRecentes.addView(linha);
        }
    }

    private void mostrarRanking() {
        int total = dados.ranking.size();
        Barras.preencher(listaRanking, dados.ranking, rankingCompleto ? total : RANKING_RESUMIDO);
        btnVerRanking.setText(rankingCompleto ? "Ver menos" : "Ver todos");
        btnVerRanking.setVisibility(total > RANKING_RESUMIDO ? View.VISIBLE : View.GONE);
    }
}
