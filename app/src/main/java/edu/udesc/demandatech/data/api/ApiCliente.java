package edu.udesc.demandatech.data.api;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.udesc.demandatech.BuildConfig;
import edu.udesc.demandatech.data.Resultado;
import edu.udesc.demandatech.data.Sessao;

// Faz as chamadas HTTP fora da thread principal e entrega o resultado nela.
public class ApiCliente {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());
    private static final String TAG = "ApiCliente";
    private static final int TEMPO_LIMITE = 15000;
    private static final String PREFIXO_TOKEN = "Bearer ";

    public interface Conversao<T> {
        T converter(String corpo) throws JSONException;
    }

    private final Sessao sessao;

    public ApiCliente(Sessao sessao) {
        this.sessao = sessao;
    }

    public static Uri.Builder caminho(String caminho) {
        return Uri.parse(caminho).buildUpon();
    }

    public <T> void get(String caminho, Conversao<T> conversao, Resultado<T> resultado) {
        enviar("GET", caminho, null, conversao, resultado);
    }

    public <T> void post(String caminho, JSONObject corpo, Conversao<T> conversao,
                         Resultado<T> resultado) {
        enviar("POST", caminho, corpo, conversao, resultado);
    }

    public <T> void put(String caminho, JSONObject corpo, Conversao<T> conversao,
                        Resultado<T> resultado) {
        enviar("PUT", caminho, corpo, conversao, resultado);
    }

    public <T> void patch(String caminho, JSONObject corpo, Conversao<T> conversao,
                          Resultado<T> resultado) {
        enviar("PATCH", caminho, corpo, conversao, resultado);
    }

    public void delete(String caminho, Resultado<Void> resultado) {
        enviar("DELETE", caminho, null, corpo -> null, resultado);
    }

    private <T> void enviar(String metodo, String caminho, JSONObject corpo,
                            Conversao<T> conversao, Resultado<T> resultado) {
        EXECUTOR.execute(() -> {
            HttpURLConnection conexao = null;
            try {
                conexao = (HttpURLConnection) new URL(BuildConfig.API_URL + caminho).openConnection();
                conexao.setRequestMethod(metodo);
                conexao.setConnectTimeout(TEMPO_LIMITE);
                conexao.setReadTimeout(TEMPO_LIMITE);
                conexao.setUseCaches(false);
                conexao.setRequestProperty("Accept", "application/json");
                String token = sessao == null ? null : sessao.getToken();
                if (token != null) {
                    conexao.setRequestProperty("Authorization", PREFIXO_TOKEN + token);
                }
                if (corpo != null) {
                    conexao.setDoOutput(true);
                    conexao.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    try (OutputStream saida = conexao.getOutputStream()) {
                        saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                int status = conexao.getResponseCode();
                renovarToken(conexao.getHeaderField("Authorization"));
                String resposta = ler(status < 400 ? conexao.getInputStream() : conexao.getErrorStream());

                if (status >= 400) {
                    falhar(resultado, mensagemDeErro(status, resposta));
                    return;
                }
                T dados = conversao.converter(resposta);
                PRINCIPAL.post(() -> resultado.sucesso(dados));
            } catch (IOException e) {
                Log.w(TAG, metodo + " " + caminho, e);
                falhar(resultado, "Não foi possível conectar ao servidor. Verifique se o backend está rodando.");
            } catch (JSONException | RuntimeException e) {
                Log.w(TAG, metodo + " " + caminho, e);
                falhar(resultado, "Resposta inesperada do servidor.");
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        });
    }

    private void renovarToken(String cabecalho) {
        if (sessao != null && cabecalho != null && cabecalho.startsWith(PREFIXO_TOKEN)) {
            sessao.atualizarToken(cabecalho.substring(PREFIXO_TOKEN.length()));
        }
    }

    private static String mensagemDeErro(int status, String resposta) {
        try {
            String erro = new JSONObject(resposta).optString("erro", "");
            if (!erro.isEmpty()) {
                return erro;
            }
        } catch (JSONException ignorado) {
        }
        if (status == 401) {
            return "Sessão expirada. Entre novamente.";
        }
        if (status == 403) {
            return "Você não tem permissão para esta ação.";
        }
        return "Erro no servidor (" + status + ").";
    }

    private static String ler(InputStream entrada) throws IOException {
        if (entrada == null) {
            return "";
        }
        try (InputStream in = entrada; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int lidos;
            while ((lidos = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, lidos);
            }
            return bytes.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static <T> void falhar(Resultado<T> resultado, String mensagem) {
        PRINCIPAL.post(() -> resultado.erro(mensagem));
    }
}
