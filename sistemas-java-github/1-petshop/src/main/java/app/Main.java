package app;

import app.Entidade.Alerta;
import app.Entidade.Campo;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Servidor web + rotas do CRUD genérico. */
public class Main {

    public static void main(String[] args) throws Exception {
        Db.iniciar();
        int porta = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);
        servidor.createContext("/", Main::tratar);
        servidor.start();
        System.out.println(Config.NOME + " rodando em http://localhost:" + porta);
        Thread.currentThread().join(); // mantém o programa ativo
    }

    // ---------- Roteamento ----------
    private static void tratar(HttpExchange ex) throws IOException {
        try {
            rotear(ex);
        } catch (Exception e) {
            e.printStackTrace();
            enviar(ex, 500, Html.pagina("<div class=\"card\">Erro interno: " + Html.esc(e.getMessage()) + "</div>", null, null));
        } finally {
            ex.close();
        }
    }

    private static void rotear(HttpExchange ex) throws Exception {
        String[] p = Arrays.stream(ex.getRequestURI().getPath().split("/"))
                .filter(s -> !s.isEmpty()).toArray(String[]::new);
        boolean post = "POST".equalsIgnoreCase(ex.getRequestMethod());

        if (p.length == 0) { index(ex); return; }

        Entidade e = Config.ENTIDADES.get(p[0]);
        if (e == null) { naoEncontrado(ex); return; }

        if (p.length == 1) { lista(ex, e, post); return; }

        if (p.length == 3 && !e.somenteCriar) {
            long id;
            try { id = Long.parseLong(p[1]); } catch (NumberFormatException n) { naoEncontrado(ex); return; }
            if (p[2].equals("editar")) { editar(ex, e, id, post); return; }
            if (p[2].equals("excluir") && post) { excluir(ex, e, id); return; }
        }
        naoEncontrado(ex);
    }

    // ---------- Telas ----------
    private static void index(HttpExchange ex) throws Exception {
        try (Connection c = Db.abrir()) {
            Map<String, Integer> totais = new LinkedHashMap<>();
            for (Entidade e : Config.ENTIDADES.values()) {
                totais.put(e.nome, Integer.parseInt(
                        Db.consultar(c, "SELECT COUNT(*) AS n FROM " + e.nome).get(0).get("n")));
            }
            Map<Alerta, List<Map<String, String>>> alertas = new LinkedHashMap<>();
            for (Alerta a : Config.ALERTAS) alertas.put(a, Db.consultar(c, a.sql()));
            enviar(ex, 200, Html.pagina(Html.index(totais, alertas), null, null));
        }
    }

    private static void lista(HttpExchange ex, Entidade e, boolean post) throws Exception {
        Map<String, String> valores = new HashMap<>();
        Map<String, String> q = parse(ex.getRequestURI().getRawQuery());
        String msg = q.get("msg"), tipo = q.get("tipo");

        try (Connection c = Db.abrir()) {
            if (post) {
                valores = lerForm(ex, e);
                String erro = validar(c, e, valores);
                if (erro == null) {
                    inserir(c, e, valores);
                    redirecionar(ex, "/" + e.nome, "Registro salvo com sucesso!", "ok");
                    return;
                }
                msg = erro;
                tipo = "erro";
            }

            StringBuilder sql = new StringBuilder("SELECT t.*");
            for (Campo cp : e.campos) {
                if (cp.isFk()) {
                    sql.append(", (SELECT ").append(cp.fkColuna()).append(" FROM ").append(cp.fkTabela())
                       .append(" WHERE id = t.").append(cp.nome()).append(") AS ").append(cp.nome()).append("__rot");
                }
            }
            sql.append(" FROM ").append(e.nome).append(" t ORDER BY t.id DESC");

            var registros = Db.consultar(c, sql.toString());
            enviar(ex, 200, Html.pagina(Html.lista(e, registros, valores, opcoes(c, e)), msg, tipo));
        }
    }

    private static void editar(HttpExchange ex, Entidade e, long id, boolean post) throws Exception {
        try (Connection c = Db.abrir()) {
            var achados = Db.consultar(c, "SELECT * FROM " + e.nome + " WHERE id = ?", id);
            if (achados.isEmpty()) { naoEncontrado(ex); return; }

            Map<String, String> valores = achados.get(0);
            String msg = null;
            if (post) {
                valores = lerForm(ex, e);
                String erro = validar(c, e, valores);
                if (erro == null) {
                    List<Object> params = new ArrayList<>();
                    StringBuilder sets = new StringBuilder();
                    for (Campo cp : e.campos) {
                        if (sets.length() > 0) sets.append(", ");
                        sets.append(cp.nome()).append(" = ?");
                        params.add(valores.get(cp.nome()));
                    }
                    params.add(id);
                    Db.executar(c, "UPDATE " + e.nome + " SET " + sets + " WHERE id = ?", params.toArray());
                    redirecionar(ex, "/" + e.nome, "Registro atualizado!", "ok");
                    return;
                }
                msg = erro;
            }
            enviar(ex, 200, Html.pagina(Html.editar(e, valores, opcoes(c, e), String.valueOf(id)), msg, "erro"));
        }
    }

    private static void excluir(HttpExchange ex, Entidade e, long id) throws Exception {
        try (Connection c = Db.abrir()) {
            Db.executar(c, "DELETE FROM " + e.nome + " WHERE id = ?", id);
            redirecionar(ex, "/" + e.nome, "Registro excluído.", "ok");
        } catch (SQLException erro) {
            if (String.valueOf(erro.getMessage()).contains("FOREIGN KEY")) {
                redirecionar(ex, "/" + e.nome,
                        "Não é possível excluir: existem registros vinculados a este.", "erro");
            } else {
                throw erro;
            }
        }
    }

    // ---------- Regras ----------
    private static String validar(Connection c, Entidade e, Map<String, String> v) throws SQLException {
        for (Campo cp : e.campos) {
            if (v.get(cp.nome()) == null && !cp.ehOpcional()) {
                return "O campo '" + cp.rotulo() + "' é obrigatório.";
            }
        }
        return e.validador != null ? e.validador.validar(c, v) : null;
    }

    private static void inserir(Connection c, Entidade e, Map<String, String> v) throws SQLException {
        c.setAutoCommit(false);
        try {
            String colunas = String.join(", ", v.keySet());
            String marcas = String.join(", ", Collections.nCopies(v.size(), "?"));
            Db.executar(c, "INSERT INTO " + e.nome + " (" + colunas + ") VALUES (" + marcas + ")",
                    v.values().toArray());
            if (e.aposInserir != null) e.aposInserir.executar(c, v);
            c.commit();
        } catch (SQLException erro) {
            c.rollback();
            throw erro;
        }
    }

    private static Map<String, List<String[]>> opcoes(Connection c, Entidade e) throws SQLException {
        Map<String, List<String[]>> mapa = new HashMap<>();
        for (Campo cp : e.campos) {
            List<String[]> lista = new ArrayList<>();
            if (cp.isFk()) {
                for (var r : Db.consultar(c, "SELECT id, " + cp.fkColuna() + " AS rotulo FROM "
                        + cp.fkTabela() + " ORDER BY " + cp.fkColuna())) {
                    lista.add(new String[]{r.get("id"), r.get("rotulo")});
                }
            } else if (cp.isSelect()) {
                for (String o : cp.opcoes()) lista.add(new String[]{o, o});
            }
            mapa.put(cp.nome(), lista);
        }
        return mapa;
    }

    // ---------- HTTP ----------
    private static Map<String, String> lerForm(HttpExchange ex, Entidade e) throws IOException {
        Map<String, String> bruto = parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        Map<String, String> valores = new LinkedHashMap<>();
        for (Campo cp : e.campos) {
            String v = bruto.get(cp.nome());
            valores.put(cp.nome(), v == null || v.isBlank() ? null : v.trim());
        }
        return valores;
    }

    private static Map<String, String> parse(String dados) {
        Map<String, String> mapa = new HashMap<>();
        if (dados == null || dados.isEmpty()) return mapa;
        for (String par : dados.split("&")) {
            String[] kv = par.split("=", 2);
            mapa.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return mapa;
    }

    private static void redirecionar(HttpExchange ex, String destino, String msg, String tipo) throws IOException {
        ex.getResponseHeaders().set("Location", destino + "?msg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8)
                + "&tipo=" + tipo);
        ex.sendResponseHeaders(303, -1);
    }

    private static void naoEncontrado(HttpExchange ex) throws IOException {
        enviar(ex, 404, Html.pagina("<div class=\"card\"><h2>Página não encontrada</h2></div>", null, null));
    }

    private static void enviar(HttpExchange ex, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
    }
}
