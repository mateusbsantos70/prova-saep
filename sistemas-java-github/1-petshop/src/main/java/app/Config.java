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

    public static final String NOME = "🐾 Pet Shop";

    public static final Map<String, Entidade> ENTIDADES = new LinkedHashMap<>();

    static {
        registrar(new Entidade("clientes", "Clientes",
                campo("nome", "Nome", "text"),
                campo("telefone", "Telefone", "tel"),
                campo("email", "E-mail", "email").opcional()));

        registrar(new Entidade("pets", "Pets",
                campo("nome", "Nome do pet", "text"),
                select("especie", "Espécie", "Cão", "Gato", "Ave", "Outro"),
                campo("raca", "Raça", "text").opcional(),
                fk("cliente_id", "Dono", "clientes", "nome")));

        registrar(new Entidade("servicos", "Serviços",
                campo("nome", "Serviço", "text"),
                campo("preco", "Preço (R$)", "number")));

        registrar(new Entidade("produtos", "Produtos",
                campo("nome", "Produto", "text"),
                campo("preco", "Preço (R$)", "number"),
                campo("estoque", "Estoque", "number")));

        registrar(new Entidade("agendamentos", "Agendamentos",
                fk("pet_id", "Pet", "pets", "nome"),
                fk("servico_id", "Serviço", "servicos", "nome"),
                campo("data_hora", "Data e hora", "datetime-local"),
                select("status", "Status", "Agendado", "Concluído", "Cancelado")));
    }

    /** Blocos de consulta exibidos no painel inicial. */
    public static final List<Alerta> ALERTAS = List.of(
            new Alerta("Próximos agendamentos", """
                    SELECT a.data_hora AS data_hora, p.nome AS pet, s.nome AS servico
                    FROM agendamentos a
                    JOIN pets p ON p.id = a.pet_id
                    JOIN servicos s ON s.id = a.servico_id
                    WHERE a.status = 'Agendado'
                    ORDER BY a.data_hora LIMIT 10"""));

    private static void registrar(Entidade e) {
        ENTIDADES.put(e.nome, e);
    }
}
