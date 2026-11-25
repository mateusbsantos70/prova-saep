CREATE TABLE tutores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    telefone TEXT NOT NULL,
    email TEXT
);

CREATE TABLE animais (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    especie TEXT NOT NULL,
    raca TEXT,
    nascimento TEXT,
    tutor_id INTEGER NOT NULL REFERENCES tutores(id)
);

CREATE TABLE veterinarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    crmv TEXT NOT NULL UNIQUE
);

CREATE TABLE consultas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    animal_id INTEGER NOT NULL REFERENCES animais(id),
    veterinario_id INTEGER NOT NULL REFERENCES veterinarios(id),
    data TEXT NOT NULL,
    motivo TEXT NOT NULL,
    diagnostico TEXT,
    valor REAL NOT NULL
);

-- Dados de exemplo
INSERT INTO tutores (nome, telefone, email) VALUES ('João Pereira', '(31) 98888-1111', 'joao@email.com');
INSERT INTO animais (nome, especie, raca, nascimento, tutor_id) VALUES ('Mel', 'Gato', 'Siamês', '2021-05-10', 1);
INSERT INTO veterinarios (nome, crmv) VALUES ('Dra. Ana Lima', 'CRMV-MG 12345');
INSERT INTO consultas (animal_id, veterinario_id, data, motivo, diagnostico, valor)
VALUES (1, 1, '2026-11-20', 'Vacinação anual', 'Vacina V4 aplicada.', 120);
