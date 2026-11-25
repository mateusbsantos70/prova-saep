CREATE TABLE categorias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL UNIQUE
);

CREATE TABLE produtos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    categoria_id INTEGER NOT NULL REFERENCES categorias(id),
    quantidade INTEGER NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    estoque_minimo INTEGER NOT NULL DEFAULT 0,
    preco REAL NOT NULL
);

CREATE TABLE movimentacoes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    produto_id INTEGER NOT NULL REFERENCES produtos(id),
    tipo TEXT NOT NULL CHECK (tipo IN ('Entrada', 'Saída')),
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    criado_em TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- Dados de exemplo
INSERT INTO categorias (nome) VALUES ('Alimentos'), ('Acessórios');
INSERT INTO produtos (nome, categoria_id, quantidade, estoque_minimo, preco) VALUES
    ('Ração Premium 10kg', 1, 3, 5, 189.90),
    ('Coleira M', 2, 25, 10, 34.90);
