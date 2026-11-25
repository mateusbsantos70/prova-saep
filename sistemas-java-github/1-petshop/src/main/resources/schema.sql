CREATE TABLE clientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    telefone TEXT NOT NULL,
    email TEXT
);

CREATE TABLE pets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    especie TEXT NOT NULL,
    raca TEXT,
    cliente_id INTEGER NOT NULL REFERENCES clientes(id)
);

CREATE TABLE servicos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    preco REAL NOT NULL
);

CREATE TABLE produtos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    preco REAL NOT NULL,
    estoque INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE agendamentos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pet_id INTEGER NOT NULL REFERENCES pets(id),
    servico_id INTEGER NOT NULL REFERENCES servicos(id),
    data_hora TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'Agendado'
);

-- Dados de exemplo
INSERT INTO clientes (nome, telefone, email) VALUES ('Maria Souza', '(31) 99999-0000', 'maria@email.com');
INSERT INTO pets (nome, especie, raca, cliente_id) VALUES ('Thor', 'Cão', 'Golden Retriever', 1);
INSERT INTO servicos (nome, preco) VALUES ('Banho', 50), ('Tosa', 70), ('Banho e Tosa', 110);
INSERT INTO produtos (nome, preco, estoque) VALUES ('Ração Premium 10kg', 189.90, 12), ('Coleira M', 34.90, 25);
INSERT INTO agendamentos (pet_id, servico_id, data_hora, status) VALUES (1, 3, '2026-12-01T10:00', 'Agendado');
