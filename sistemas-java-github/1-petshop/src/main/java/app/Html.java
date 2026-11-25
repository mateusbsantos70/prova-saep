package app;

import app.Entidade.Alerta;
import app.Entidade.Campo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Geração das páginas HTML (sem frameworks de template). */
final class Html {

    private Html() {}

    private static final String CSS = """
        :root{--cor:#2563eb;--bg:#f4f6fa;--card:#fff;--txt:#1f2937;--borda:#e5e7eb}
        *{box-sizing:border-box}
        body{margin:0;font-family:system-ui,Arial,sans-serif;background:var(--bg);color:var(--txt)}
        header{background:var(--cor);color:#fff;padding:12px 24px;display:flex;flex-wrap:wrap;gap:20px;align-items:center}
        header a{color:#fff;text-decoration:none}
        .marca{font-weight:700;font-size:1.2rem}
        nav{display:flex;flex-wrap:wrap;gap:14px}
        nav a{opacity:.9}nav a:hover{opacity:1;text-decoration:underline}
        main{max-width:1000px;margin:24px auto;padding:0 16px}
        .card{background:var(--card);border:1px solid var(--borda);border-radius:10px;padding:18px;margin-bottom:20px}
        .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:14px}
        .grid a{text-decoration:none;color:inherit}
        .num{font-size:2rem;font-weight:700;color:var(--cor)}
        table{width:100%;border-collapse:collapse}
        th,td{text-align:left;padding:8px;border-bottom:1px solid var(--borda);font-size:.92rem}
        th{background:#f9fafb}
        .form{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:12px}
        label{display:flex;flex-direction:column;font-size:.85rem;gap:4px;font-weight:600}
        input,select,textarea{padding:8px;border:1px solid #cbd5e1;border-radius:6px;font:inherit}
        button,.btn{background:var(--cor);color:#fff;border:0;padding:9px 16px;border-radius:6px;cursor:pointer;text-decoration:none;font:inherit;display:inline-block}
        .btn-sec{background:#64748b}.btn-del{background:#dc2626;padding:5px 10px}
        .btn-sm{padding:5px 10px;font-size:.85rem}
        .acoes{display:flex;gap:6px}.acoes form{margin:0}
        .flash{padding:10px 14px;border-radius:6px;margin-bottom:14px}
        .flash.ok{background:#dcfce7;color:#166534}.flash.erro{background:#fee2e2;color:#991b1b}
        .vazio{color:#6b7280}
        """;

    static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String cabecalho(String coluna) {
        String t = coluna.replace("_", " ");
        return t.isEmpty() ? t : Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    static String pagina(String corpo, String msg, String tipo) {
        StringBuilder nav = new StringBuilder();
        for (Entidade e : Config.ENTIDADES.values()) {
            nav.append("<a href=\"/").append(e.nome).append("\">").append(esc(e.titulo)).append("</a>");
        }
        String flash = (msg == null || msg.isBlank()) ? ""
                : "<div class=\"flash " + ("erro".equals(tipo) ? "erro" : "ok") + "\">" + esc(msg) + "</div>";

        return "<!doctype html><html lang=\"pt-BR\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + esc(Config.NOME) + "</title><style>" + CSS + "</style></head><body>"
                + "<header><a class=\"marca\" href=\"/\">" + esc(Config.NOME) + "</a><nav>" + nav + "</nav></header>"
                + "<main>" + flash + corpo + "</main></body></html>";
    }

    static String index(Map<String, Integer> totais, Map<Alerta, List<Map<String, String>>> alertas) {
        StringBuilder sb = new StringBuilder("<h2>Painel</h2><div class=\"grid\">");
        for (Entidade e : Config.ENTIDADES.values()) {
            sb.append("<a href=\"/").append(e.nome).append("\"><div class=\"card\"><div class=\"num\">")
              .append(totais.get(e.nome)).append("</div>").append(esc(e.titulo)).append("</div></a>");
        }
        sb.append("</div>");

        for (Map.Entry<Alerta, List<Map<String, String>>> a : alertas.entrySet()) {
            sb.append("<div class=\"card\"><h3>").append(esc(a.getKey().titulo())).append("</h3>");
            List<Map<String, String>> linhas = a.getValue();
            if (linhas.isEmpty()) {
                sb.append("<p class=\"vazio\">Nada por aqui.</p>");
            } else {
                sb.append("<table><tr>");
                for (String col : linhas.get(0).keySet()) sb.append("<th>").append(esc(cabecalho(col))).append("</th>");
                sb.append("</tr>");
                for (Map<String, String> l : linhas) {
                    sb.append("<tr>");
                    for (String v : l.values()) sb.append("<td>").append(esc(v)).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
            }
            sb.append("</div>");
        }
        return sb.toString();
    }

    static String formulario(Entidade e, Map<String, String> valores,
                             Map<String, List<String[]>> opcoes, String acao, String botao) {
        StringBuilder sb = new StringBuilder("<form method=\"post\" action=\"" + acao + "\" class=\"form\">");
        for (Campo c : e.campos) {
            String req = c.ehOpcional() ? "" : " required";
            String valor = valores.get(c.nome());
            sb.append("<label>").append(esc(c.rotulo())).append(c.ehOpcional() ? " (opcional)" : "");

            if (c.isFk() || c.isSelect()) {
                sb.append("<select name=\"").append(c.nome()).append("\"").append(req).append(">")
                  .append("<option value=\"\">Selecione...</option>");
                for (String[] o : opcoes.get(c.nome())) {
                    String sel = o[0].equals(valor) ? " selected" : "";
                    sb.append("<option value=\"").append(esc(o[0])).append("\"").append(sel).append(">")
                      .append(esc(o[1])).append("</option>");
                }
                sb.append("</select>");
            } else if ("textarea".equals(c.tipo())) {
                sb.append("<textarea name=\"").append(c.nome()).append("\" rows=\"2\"").append(req).append(">")
                  .append(esc(valor)).append("</textarea>");
            } else {
                if (valor == null && "date".equals(c.tipo()) && !c.ehOpcional()) valor = LocalDate.now().toString();
                String extra = "number".equals(c.tipo()) ? " step=\"any\" min=\"0\"" : "";
                sb.append("<input type=\"").append(c.tipo()).append("\" name=\"").append(c.nome())
                  .append("\" value=\"").append(esc(valor)).append("\"").append(extra).append(req).append(">");
            }
            sb.append("</label>");
        }
        sb.append("<div style=\"align-self:end\"><button type=\"submit\">").append(botao).append("</button></div></form>");
        return sb.toString();
    }

    static String lista(Entidade e, List<Map<String, String>> registros, Map<String, String> valores,
                        Map<String, List<String[]>> opcoes) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>").append(esc(e.titulo)).append("</h2>");
        sb.append("<div class=\"card\"><h3>Novo registro</h3>")
          .append(formulario(e, valores, opcoes, "/" + e.nome, "Salvar")).append("</div>");

        sb.append("<div class=\"card\" style=\"overflow-x:auto\">");
        if (registros.isEmpty()) {
            sb.append("<p class=\"vazio\">Nenhum registro cadastrado ainda.</p></div>");
            return sb.toString();
        }
        sb.append("<table><tr><th>#</th>");
        for (Campo c : e.campos) sb.append("<th>").append(esc(c.rotulo())).append("</th>");
        for (String x : e.extras) sb.append("<th>").append(esc(cabecalho(x))).append("</th>");
        if (!e.somenteCriar) sb.append("<th></th>");
        sb.append("</tr>");

        for (Map<String, String> r : registros) {
            String id = r.get("id");
            sb.append("<tr><td>").append(esc(id)).append("</td>");
            for (Campo c : e.campos) {
                String v = c.isFk() ? r.get(c.nome() + "__rot") : r.get(c.nome());
                sb.append("<td>").append(esc(v)).append("</td>");
            }
            for (String x : e.extras) sb.append("<td>").append(esc(r.get(x))).append("</td>");
            if (!e.somenteCriar) {
                sb.append("<td class=\"acoes\"><a class=\"btn btn-sec btn-sm\" href=\"/").append(e.nome)
                  .append("/").append(id).append("/editar\">Editar</a>")
                  .append("<form method=\"post\" action=\"/").append(e.nome).append("/").append(id)
                  .append("/excluir\" onsubmit=\"return confirm('Excluir este registro?')\">")
                  .append("<button class=\"btn-del\">Excluir</button></form></td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table></div>");
        return sb.toString();
    }

    static String editar(Entidade e, Map<String, String> valores, Map<String, List<String[]>> opcoes, String id) {
        return "<h2>Editar – " + esc(e.titulo) + "</h2><div class=\"card\">"
                + formulario(e, valores, opcoes, "/" + e.nome + "/" + id + "/editar", "Atualizar")
                + "<p><a href=\"/" + e.nome + "\">← Voltar</a></p></div>";
    }
}
