package app;

import static app.Entidade.campo;
import static app.Entidade.fk;
import static app.Entidade.select;

import app.Entidade.Alerta;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Configuração do sistema: entidades (tabelas), campos e painel inicial. */
public final class Config {

    private Config() {}

    public static final String NOME = "📦 Controle de Estoque";

    public static final Map<String, Entidade> ENTIDADES = new LinkedHashMap<>();

    static {
        registrar(new Entidade("categorias", "Categorias",
                campo("nome", "Nome", "text")));

        registrar(new Entidade("produtos", "Produtos",
                campo("nome", "Produto", "text"),
                fk("categoria_id", "Categoria", "categorias", "nome"),
                campo("quantidade", "Quantidade em estoque", "number"),
                campo("estoque_minimo", "Estoque mínimo", "number"),
                campo("preco", "Preço (R$)", "number")));

        registrar(new Entidade("movimentacoes", "Movimentações",
                fk("produto_id", "Produto", "produtos", "nome"),
                select("tipo", "Tipo", "Entrada", "Saída"),
                campo("quantidade", "Quantidade", "number"))
                .extras("criado_em")
                .somenteCriar() // movimentações não podem ser editadas nem excluídas
                .validador(Config::validarMovimentacao)
                .aposInserir(Config::atualizarEstoque));
    }

    /** Blocos de consulta exibidos no painel inicial. */
    public static final List<Alerta> ALERTAS = List.of(
            new Alerta("⚠️ Produtos com estoque baixo", """
                    SELECT nome AS produto, quantidade, estoque_minimo
                    FROM produtos
                    WHERE quantidade <= estoque_minimo
                    ORDER BY quantidade"""));

    /** Impede saída maior que o estoque disponível. */
    private static String validarMovimentacao(Connection c, Map<String, String> v) throws SQLException {
        int qtd;
        try {
            qtd = (int) Double.parseDouble(v.get("quantidade"));
        } catch (NumberFormatException e) {
            return "Quantidade inválida.";
        }
        if (qtd <= 0) return "A quantidade deve ser maior que zero.";

        if ("Saída".equals(v.get("tipo"))) {
            var r = Db.consultar(c, "SELECT quantidade FROM produtos WHERE id = ?", v.get("produto_id"));
            int atual = (int) Double.parseDouble(r.get(0).get("quantidade"));
            if (qtd > atual) return "Estoque insuficiente (disponível: " + atual + ").";
        }
        return null;
    }

    /** Soma ou subtrai do estoque do produto após registrar a movimentação. */
    private static void atualizarEstoque(Connection c, Map<String, String> v) throws SQLException {
        int qtd = (int) Double.parseDouble(v.get("quantidade"));
        int delta = "Entrada".equals(v.get("tipo")) ? qtd : -qtd;
        Db.executar(c, "UPDATE produtos SET quantidade = quantidade + ? WHERE id = ?",
                delta, v.get("produto_id"));
    }

    private static Entidade registrar(Entidade e) {
        ENTIDADES.put(e.nome, e);
        return e;
    }
}
