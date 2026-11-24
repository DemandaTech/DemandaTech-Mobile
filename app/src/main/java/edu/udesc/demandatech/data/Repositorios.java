package edu.udesc.demandatech.data;

import android.content.Context;

import edu.udesc.demandatech.data.demo.AuthRepositoryDemo;
import edu.udesc.demandatech.data.demo.CadastroRepositoryDemo;
import edu.udesc.demandatech.data.demo.DemandaRepositoryDemo;
import edu.udesc.demandatech.data.demo.EstornoRepositoryDemo;
import edu.udesc.demandatech.data.demo.RelatorioRepositoryDemo;

// Ponto único de troca: quando a API existir, basta devolver aqui as implementações reais.
public final class Repositorios {

    private Repositorios() {
    }

    public static AuthRepository auth() {
        return new AuthRepositoryDemo();
    }

    public static DemandaRepository demandas(Context context) {
        return new DemandaRepositoryDemo(new Sessao(context));
    }

    public static CadastroRepository cadastros(Context context) {
        return new CadastroRepositoryDemo(new Sessao(context));
    }

    public static EstornoRepository estornos(Context context) {
        return new EstornoRepositoryDemo(new Sessao(context));
    }

    public static RelatorioRepository relatorios(Context context) {
        return new RelatorioRepositoryDemo(new Sessao(context));
    }
}
