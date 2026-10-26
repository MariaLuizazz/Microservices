-- Usuarios iniciais (sem eles ninguem teria ROLE_MANUTENCAO para cadastrar o primeiro usuario)
INSERT INTO usuarios (username, senha) VALUES ('admin', 'admin');
INSERT INTO usuarios (username, senha) VALUES ('manut', 'manut');
INSERT INTO usuarios (username, senha) VALUES ('joao', '123');

INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
INSERT INTO authorities (username, authority) VALUES ('manut', 'ROLE_MANUTENCAO');
INSERT INTO authorities (username, authority) VALUES ('joao', 'ROLE_USER');
