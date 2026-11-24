package edu.udesc.demandatech.data;

import android.content.Context;
import android.content.SharedPreferences;

import edu.udesc.demandatech.model.Perfil;
import edu.udesc.demandatech.model.Usuario;

// Guarda o usuário logado e o token entre as telas.
public class Sessao {

    private static final String ARQUIVO = "sessao";
    private static final String TOKEN = "token";
    private static final String MANTER = "manter";
    private static final String MATRICULA = "matricula";
    private static final String NOME = "nome";
    private static final String PERFIL = "perfil";
    private static final String DEPARTAMENTO = "departamento";

    private final SharedPreferences prefs;

    public Sessao(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
    }

    public void salvar(Usuario usuario, String token, boolean manterConectado) {
        prefs.edit()
                .putString(TOKEN, token)
                .putBoolean(MANTER, manterConectado)
                .putString(MATRICULA, usuario.getMatricula())
                .putString(NOME, usuario.getNome())
                .putString(PERFIL, usuario.getPerfil().name())
                .putString(DEPARTAMENTO, usuario.getDepartamento())
                .apply();
    }

    public boolean estaLogado() {
        return prefs.contains(TOKEN);
    }

    public boolean manterConectado() {
        return prefs.getBoolean(MANTER, false);
    }

    public String getToken() {
        return prefs.getString(TOKEN, null);
    }

    public Usuario getUsuario() {
        return new Usuario(
                prefs.getString(MATRICULA, ""),
                prefs.getString(NOME, ""),
                Perfil.valueOf(prefs.getString(PERFIL, Perfil.USUARIO.name())),
                prefs.getString(DEPARTAMENTO, ""));
    }

    public void encerrar() {
        prefs.edit().clear().apply();
    }
}
