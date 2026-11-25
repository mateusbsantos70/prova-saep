package app;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Conexão com o SQLite (via JDBC) e utilitários de consulta. */
public final class Db {

    private static final String ARQUIVO = "database.db";

    // 3-sistema-estoque
    private static final String ARQUIVO_SQLITE = "estoque.db";
    private static final String URL = "jdbc:sqlite:" + ARQUIVO;

    private Db() {}

    public static Connection abrir() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    /** Cria o banco a partir do schema.sql na primeira execução. */
    public static void iniciar() throws Exception {
        if (Files.exists(Path.of(ARQUIVO))) return;

        String sql;
        try (InputStream in = Db.class.getResourceAsStream("/schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql não encontrado");
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        StringBuilder limpo = new StringBuilder();
        for (String linha : sql.split("\n")) {
            if (!linha.trim().startsWith("--")) limpo.append(linha).append('\n');
        }

        try (Connection c = abrir(); Statement st = c.createStatement()) {
            for (String comando : limpo.toString().split(";")) {
                if (!comando.isBlank()) st.execute(comando);
            }
        }
        System.out.println("Banco de dados criado: " + ARQUIVO);
    }

    /** Executa um SELECT e devolve as linhas como mapas coluna -> texto. */
    public static List<Map<String, String>> consultar(Connection c, String sql, Object... params)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            vincular(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                List<Map<String, String>> linhas = new ArrayList<>();
                while (rs.next()) {
                    Map<String, String> linha = new LinkedHashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        linha.put(md.getColumnLabel(i), rs.getString(i));
                    }
                    linhas.add(linha);
                }
                return linhas;
            }
        }
    }

    /** Executa INSERT, UPDATE ou DELETE. */
    public static int executar(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            vincular(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void vincular(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) ps.setNull(i + 1, Types.NULL);
            else ps.setObject(i + 1, params[i]);
        }
    }
}
