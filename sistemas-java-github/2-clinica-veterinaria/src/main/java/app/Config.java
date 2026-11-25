package app;

import static app.Entidade.campo;
import static app.Entidade.fk;
import static app.Entidade.select;

import app.Entidade.Alerta;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Configuração do sistema: entidades (tabelas), campos e painel inicial. */
public final class Config {

    private Config() {}

    public static final String NOME = "🩺 Clínica Veterinária";

    public static final Map<String, Entidade> ENTIDADES = new LinkedHashMap<>();

    static {
        registrar(new Entidade("tutores", "Tutores",
                campo("nome", "Nome", "text"),
                campo("telefone", "Telefone", "tel"),
                campo("email", "E-mail", "email").opcional()));

        registrar(new Entidade("animais", "Animais",
                campo("nome", "Nome", "text"),
                select("especie", "Espécie", "Cão", "Gato", "Ave", "Roedor", "Outro"),
                campo("raca", "Raça", "text").opcional(),
                campo("nascimento", "Nascimento", "date").opcional(),
                fk("tutor_id", "Tutor", "tutores", "nome")));

        registrar(new Entidade("veterinarios", "Veterinários",
                campo("nome", "Nome", "text"),
                campo("crmv", "CRMV", "text")));

        registrar(new Entidade("consultas", "Consultas",
                fk("animal_id", "Animal", "animais", "nome"),
                fk("veterinario_id", "Veterinário", "veterinarios", "nome"),
                campo("data", "Data", "date"),
                campo("motivo", "Motivo", "text"),
                campo("diagnostico", "Diagnóstico / tratamento", "textarea").opcional(),
                campo("valor", "Valor (R$)", "number")));
    }

    /** Blocos de consulta exibidos no painel inicial. */
    public static final List<Alerta> ALERTAS = List.of(
            new Alerta("Últimas consultas", """
                    SELECT c.data AS data, a.nome AS animal, v.nome AS veterinario, c.motivo AS motivo
                    FROM consultas c
                    JOIN animais a ON a.id = c.animal_id
                    JOIN veterinarios v ON v.id = c.veterinario_id
                    ORDER BY c.data DESC, c.id DESC LIMIT 10"""));

    private static void registrar(Entidade e) {
        ENTIDADES.put(e.nome, e);
    }
}
