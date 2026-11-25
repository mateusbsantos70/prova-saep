package app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Descreve uma tabela do sistema: seus campos, regras de validação e ganchos. */
public class Entidade {

    public interface Validador {
        String validar(Connection c, Map<String, String> valores) throws SQLException;
    }

    public interface AposInserir {
        void executar(Connection c, Map<String, String> valores) throws SQLException;
    }

    /** tipo: text, tel, email, date, datetime-local, number, textarea, select ou fk. */
    public record Campo(String nome, String rotulo, String tipo, List<String> opcoes,
                        String fkTabela, String fkColuna, boolean ehOpcional) {

        public Campo opcional() {
            return new Campo(nome, rotulo, tipo, opcoes, fkTabela, fkColuna, true);
        }

        public boolean isFk() { return "fk".equals(tipo); }
        public boolean isSelect() { return "select".equals(tipo); }
    }

    /** Bloco de consulta exibido no painel inicial. */
    public record Alerta(String titulo, String sql) {}

    // ----- fábricas de campos -----
    public static Campo campo(String nome, String rotulo, String tipo) {
        return new Campo(nome, rotulo, tipo, List.of(), null, null, false);
    }

    public static Campo select(String nome, String rotulo, String... opcoes) {
        return new Campo(nome, rotulo, "select", List.of(opcoes), null, null, false);
    }

    public static Campo fk(String nome, String rotulo, String tabela, String colunaExibida) {
        return new Campo(nome, rotulo, "fk", List.of(), tabela, colunaExibida, false);
    }

    // ----- dados da entidade -----
    public final String nome;
    public final String titulo;
    public final List<Campo> campos;
    public final List<String> extras = new ArrayList<>();
    public boolean somenteCriar = false;
    public Validador validador;
    public AposInserir aposInserir;

    public Entidade(String nome, String titulo, Campo... campos) {
        this.nome = nome;
        this.titulo = titulo;
        this.campos = List.of(campos);
    }

    public Entidade extras(String... colunas) { extras.addAll(List.of(colunas)); return this; }
    public Entidade somenteCriar() { this.somenteCriar = true; return this; }
    public Entidade validador(Validador v) { this.validador = v; return this; }
    public Entidade aposInserir(AposInserir a) { this.aposInserir = a; return this; }
}
