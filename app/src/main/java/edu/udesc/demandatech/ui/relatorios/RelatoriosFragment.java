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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.udesc.demandatech.R;
import edu.udesc.demandatech.data.RelatorioRepository;
import edu.udesc.demandatech.data.Repositorios;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.model.ItemRelatorio;
import edu.udesc.demandatech.model.Relatorios;
import edu.udesc.demandatech.ui.Indicadores;

// Relatórios do Administrador: indicadores e três seções que abrem ao tocar no título.
public class RelatoriosFragment extends Fragment {

    private static final int SEM_LIMITE = Integer.MAX_VALUE;

    private RelatorioRepository repositorio;
    private View raiz;
    private TextView tvEstado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle estado) {
        return inflater.inflate(R.layout.fragment_relatorios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estado) {
        repositorio = Repositorios.relatorios(requireContext());
        raiz = view;
        tvEstado = view.findViewById(R.id.tvEstado);
        tvEstado.setOnClickListener(v -> carregar());

        Indicadores.rotular(view, R.id.indicadorResolvidas, "Resolvidas");
        Indicadores.rotular(view, R.id.indicadorEstornos, "Estornos");
        Indicadores.rotular(view, R.id.indicadorAndamento, "Em andamento");

        configurarSecao(R.id.secaoDepartamentos, "Demandas por departamento",
                "Departamento de quem abriu a demanda", true);
        configurarSecao(R.id.secaoEstornos, "Estornos por responsável",
                "Técnicos com estorno registrado", false);
        configurarSecao(R.id.secaoProdutivos, "Top 10 · Mais produtivos",
                "Técnicos por demandas resolvidas", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        carregar();
    }

    private void configurarSecao(int id, String titulo, String subtitulo, boolean aberta) {
        View secao = raiz.findViewById(id);
        TextView tvTitulo = secao.findViewById(R.id.tvTituloSecao);
        TextView tvSubtitulo = secao.findViewById(R.id.tvSubtituloSecao);
        View corpo = secao.findViewById(R.id.corpoSecao);
        View seta = secao.findViewById(R.id.ivSeta);

        tvTitulo.setText(titulo);
        tvSubtitulo.setText(subtitulo);
        abrirOuFechar(corpo, seta, aberta);
        secao.findViewById(R.id.cabecalhoSecao).setOnClickListener(v ->
                abrirOuFechar(corpo, seta, corpo.getVisibility() != View.VISIBLE));
    }

    private void abrirOuFechar(View corpo, View seta, boolean aberta) {
        corpo.setVisibility(aberta ? View.VISIBLE : View.GONE);
        seta.setRotation(aberta ? 180 : 0);
    }

    private void carregar() {
        tvEstado.setVisibility(View.GONE);
        repositorio.relatorios(new Resultado<Relatorios>() {
            @Override
            public void sucesso(Relatorios relatorios) {
                if (getView() != null) {
                    mostrar(relatorios);
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

    private void mostrar(Relatorios relatorios) {
        Indicadores.valorar(raiz, R.id.indicadorResolvidas, String.valueOf(relatorios.resolvidas));
        Indicadores.valorar(raiz, R.id.indicadorEstornos, String.valueOf(relatorios.estornos));
        Indicadores.valorar(raiz, R.id.indicadorAndamento, String.valueOf(relatorios.emAndamento));

        Barras.preencher(corpo(R.id.secaoDepartamentos), relatorios.porDepartamento, SEM_LIMITE);
        Barras.preencher(corpo(R.id.secaoEstornos), relatorios.estornosPorResponsavel, SEM_LIMITE);
        Barras.preencher(corpo(R.id.secaoProdutivos), comPosicao(relatorios.maisProdutivos),
                SEM_LIMITE);
    }

    private LinearLayout corpo(int secao) {
        return raiz.findViewById(secao).findViewById(R.id.corpoSecao);
    }

    // Acrescenta a posição ao nome, como "01 · Ana Souza".
    private List<ItemRelatorio> comPosicao(List<ItemRelatorio> itens) {
        List<ItemRelatorio> numerados = new ArrayList<>();
        for (int indice = 0; indice < itens.size(); indice++) {
            ItemRelatorio item = itens.get(indice);
            String nome = String.format(Locale.ROOT, "%02d · %s", indice + 1, item.getNome());
            numerados.add(new ItemRelatorio(nome, item.getValor(), item.getDetalhe()));
        }
        return numerados;
    }
}
