package edu.udesc.demandatech.data;

import android.content.Context;

import edu.udesc.demandatech.data.api.AuthRepositoryApi;
import edu.udesc.demandatech.data.api.CadastroRepositoryApi;
import edu.udesc.demandatech.data.api.DemandaRepositoryApi;
import edu.udesc.demandatech.data.api.EstornoRepositoryApi;
import edu.udesc.demandatech.data.api.RelatorioRepositoryApi;

// Ponto único de troca: as implementações em data/demo continuam disponíveis para usar sem backend.
public final class Repositorios {

    private Repositorios() {
    }

    public static AuthRepository auth() {
        return new AuthRepositoryApi();
    }

    public static DemandaRepository demandas(Context context) {
        return new DemandaRepositoryApi(new Sessao(context));
    }

    public static CadastroRepository cadastros(Context context) {
        return new CadastroRepositoryApi(new Sessao(context));
    }

    public static EstornoRepository estornos(Context context) {
        return new EstornoRepositoryApi(new Sessao(context));
    }

    public static RelatorioRepository relatorios(Context context) {
        return new RelatorioRepositoryApi(new Sessao(context));
    }
}
