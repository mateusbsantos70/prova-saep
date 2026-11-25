# Sistema para Pet Shop (Java)

Cadastro de clientes, pets, serviços, produtos e agendamentos de banho/tosa.

## Tecnologias
- Java 17+
- Servidor HTTP embutido do JDK (`com.sun.net.httpserver`) – sem frameworks
- JDBC + SQLite (`sqlite-jdbc`) – o banco `database.db` é criado na 1ª execução a partir do `schema.sql`
- Maven

## Como rodar

Pré-requisitos: **JDK 17 ou superior** e **Maven 3.8+**.

```bash
mvn compile exec:java
```

Acesse: http://localhost:8080 (para mudar a porta: `PORT=9000 mvn compile exec:java`)

Para recriar o banco do zero, apague o arquivo `database.db` e rode novamente.

## Estrutura
```
src/main/java/app/
  Main.java      rotas e servidor web
  Db.java        conexão JDBC com o SQLite
  Html.java      geração das páginas
  Entidade.java  modelo de tabela/campos
  Config.java    entidades e regras DESTE sistema
src/main/resources/schema.sql   tabelas e dados de exemplo
pom.xml
```

Para adicionar um campo ou tabela: ajuste o `schema.sql` e o `Config.java`.
