-- =====================================================================
-- V3: dados iniciais do Gojo Purin
-- 1 admin, 8 categorias, 55 ingredientes, 25 pratos com ficha tecnica,
-- 4 fornecedores com catalogo e o estoque inicial.
-- =====================================================================

-- ---------- Admin (login: admin@email.com / senha: senha123) ----------
-- A senha nunca e gravada em texto: o valor abaixo e o hash BCrypt de "senha123".
INSERT INTO usuario (nome, email, senha_hash, perfil, telefone, status) VALUES
('Administrador Gojo Purin', 'admin@email.com',
 '$2a$10$Bnsm.RGHgZ7bPMDXjGs4HuWxUt2exGNQp8IL3NUDyZ8Jsq1rcS9YC', 'ADMIN', '11999990000', 'ATIVO');

-- ---------- Categorias ----------
INSERT INTO categoria (nome, descricao, ordem) VALUES
('Prato principal', 'Ramens, curry e pratos completos dos animes',     1),
('Massa',           'Massas autorais da casa',                         2),
('Bento',           'Marmitas japonesas montadas na hora',             3),
('Petisco',         'Porções para dividir (ou não)',                   4),
('Acompanhamento',  'Para completar o pedido',                         5),
('Sobremesa',       'Doces inspirados nos personagens',                6),
('Bebida',          'Sodas e bebidas geladas',                         7),
('Bebida especial', 'Mocktails autorais',                              8);

-- ---------- Ingredientes ----------
-- custo_unitario e o custo por unidade padrao (por grama, por ml ou por unidade).
INSERT INTO ingrediente (sku, nome, unidade_padrao, estoque_minimo, custo_unitario) VALUES
('ING-001', 'Macarrão para ramen',           'G',  3000, 0.0180),
('ING-002', 'Caldo tonkotsu',                'ML', 8000, 0.0080),
('ING-003', 'Barriga suína (chashu)',        'G',  2000, 0.0320),
('ING-004', 'Ovo',                           'UN',   60, 0.8000),
('ING-005', 'Cebolinha',                     'G',   300, 0.0200),
('ING-006', 'Narutomaki',                    'G',   300, 0.0600),
('ING-007', 'Arroz japonês',                 'G',  5000, 0.0120),
('ING-008', 'Roux de curry japonês',         'G',   800, 0.0500),
('ING-009', 'Batata',                        'G',  2000, 0.0060),
('ING-010', 'Cenoura',                       'G',  1500, 0.0050),
('ING-011', 'Cebola',                        'G',  2000, 0.0050),
('ING-012', 'Carne bovina em tiras',         'G',  3000, 0.0450),
('ING-013', 'Peito de frango',               'G',  4000, 0.0200),
('ING-014', 'Ketchup',                       'G',   500, 0.0150),
('ING-015', 'Manteiga',                      'G',   800, 0.0450),
('ING-016', 'Alho negro',                    'G',   300, 0.1800),
('ING-017', 'Molho shoyu',                   'ML', 1000, 0.0180),
('ING-018', 'Molho teriyaki',                'ML',  800, 0.0300),
('ING-019', 'Brócolis',                      'G',  1000, 0.0150),
('ING-020', 'Polvo cozido',                  'G',  1000, 0.1200),
('ING-021', 'Farinha de trigo',              'G',  3000, 0.0060),
('ING-022', 'Molho okonomiyaki',             'ML',  600, 0.0400),
('ING-023', 'Maionese japonesa',             'G',   500, 0.0350),
('ING-024', 'Katsuobushi (flocos de bonito)', 'G',  100, 0.3000),
('ING-025', 'Massa de gyoza',                'UN',  120, 0.3500),
('ING-026', 'Carne suína moída',             'G',  2000, 0.0250),
('ING-027', 'Repolho',                       'G',  2000, 0.0050),
('ING-028', 'Alga nori (folha)',             'UN',   30, 0.9000),
('ING-029', 'Atum em conserva',              'G',   500, 0.0600),
('ING-030', 'Batata palito congelada',       'G',  4000, 0.0160),
('ING-031', 'Páprica defumada',              'G',   100, 0.0800),
('ING-032', 'Molho picante da casa',         'ML',  500, 0.0350),
('ING-033', 'Farinha panko',                 'G',   800, 0.0300),
('ING-034', 'Molho agridoce',                'ML',  800, 0.0250),
('ING-035', 'Alho',                          'G',   300, 0.0300),
('ING-036', 'Óleo vegetal',                  'ML', 3000, 0.0100),
('ING-037', 'Açúcar',                        'G',  2000, 0.0050),
('ING-038', 'Cacau em pó',                   'G',   400, 0.0600),
('ING-039', 'Cream cheese',                  'G',   800, 0.0450),
('ING-040', 'Frutas vermelhas congeladas',   'G',  2000, 0.0450),
('ING-041', 'Morango',                       'G',  2000, 0.0250),
('ING-042', 'Creme de leite fresco',         'ML', 2000, 0.0300),
('ING-043', 'Granola',                       'G',   500, 0.0300),
('ING-044', 'Farinha de arroz glutinoso',    'G',  1000, 0.0350),
('ING-045', 'Chocolate meio amargo',         'G',   800, 0.0700),
('ING-046', 'Chocolate branco',              'G',   500, 0.0750),
('ING-047', 'Leite integral',                'ML', 4000, 0.0055),
('ING-048', 'Xarope de blueberry',           'ML',  500, 0.0600),
('ING-049', 'Xarope de uva',                 'ML',  800, 0.0500),
('ING-050', 'Xarope de morango',             'ML',  800, 0.0500),
('ING-051', 'Água com gás',                  'ML', 8000, 0.0040),
('ING-052', 'Limão',                         'G',   800, 0.0080),
('ING-053', 'Espaguete',                     'G',  2000, 0.0120),
('ING-054', 'Cogumelo shimeji',              'G',   800, 0.0500),
('ING-055', 'Queijo parmesão',               'G',   400, 0.0900);

-- ---------- Pratos ----------
-- Entram como ATIVO porque todos recebem ficha tecnica logo abaixo (regra RN01).
INSERT INTO prato (categoria_id, nome, descricao, preco_venda, tempo_preparo_min, anime, personagem, status)
SELECT c.id, v.nome, v.descricao, v.preco, v.tempo, v.anime, v.personagem, 'ATIVO'
FROM (VALUES
 ( 1, 'Prato principal', 'Ramen Ichiraku',          'Ramen com caldo encorpado, macarrão, carne suína, ovo cozido, cebolinha e narutomaki.',          32.90, 20, 'Naruto',            'Naruto'),
 ( 2, 'Prato principal', 'Curry do Sanji',          'Curry japonês cremoso servido com arroz, legumes e carne temperada.',                            34.90, 25, 'One Piece',         'Sanji'),
 ( 3, 'Prato principal', 'Omurice da Anya',         'Arroz temperado envolvido por uma omelete macia, finalizado com molho especial.',                27.90, 15, 'SPY×FAMILY',        'Anya'),
 ( 4, 'Prato principal', 'Black Garlic Ramen',      'Ramen de caldo intenso com alho negro, carne suína, ovo, cebolinha e óleo aromático.',           36.90, 20, 'Original',          NULL),
 ( 5, 'Prato principal', 'Gyudon do Denji',         'Arroz japonês coberto com carne bovina cozida com cebola em molho agridoce.',                    31.90, 15, 'Chainsaw Man',      'Denji'),
 ( 6, 'Bento',           'Bento do Tanjiro',        'Bento com arroz, frango teriyaki, legumes, ovo e acompanhamentos japoneses.',                    35.90, 20, 'Demon Slayer',      'Tanjiro'),
 ( 7, 'Petisco',         'Takoyaki U.A.',           'Bolinhos de polvo servidos com molho japonês, maionese e flocos de bonito.',                     18.90, 15, 'My Hero Academia',  'U.A.'),
 ( 8, 'Petisco',         'Gyoza do Mikey',          'Gyozas dourados recheados com carne suína e vegetais, acompanhados de molho especial.',          19.90, 15, 'Tokyo Revengers',   'Mikey'),
 ( 9, 'Petisco',         'Onigiri do Shikamaru',    'Bolinhos de arroz recheados e envolvidos em alga nori.',                                         13.90, 10, 'Naruto',            'Shikamaru'),
 (10, 'Acompanhamento',  'Batatas do Sukuna',       'Batatas crocantes temperadas com páprica, alho e molho picante da casa.',                        17.90, 12, 'Jujutsu Kaisen',    'Sukuna'),
 (11, 'Petisco',         'Frango Imperial da Yor',  'Tiras de frango crocantes acompanhadas de molho agridoce levemente picante.',                    22.90, 15, 'SPY×FAMILY',        'Yor'),
 (12, 'Petisco',         'Okonomiyaki U.A.',        'Panqueca japonesa recheada com vegetais e carne, coberta com molho especial e maionese.',        24.90, 18, 'My Hero Academia',  NULL),
 (13, 'Sobremesa',       'Sukuna Red Velvet',       'Bolo red velvet com creme de baunilha e cobertura de frutas vermelhas.',                         19.90,  8, 'Jujutsu Kaisen',    'Sukuna'),
 (14, 'Sobremesa',       'Parfait da Anya',         'Parfait de morango, creme, granola e calda de frutas vermelhas.',                                21.90,  8, 'SPY×FAMILY',        'Anya'),
 (15, 'Sobremesa',       'Dango da Nezuko',         'Tradicionais bolinhos de arroz japoneses em três cores, servidos com calda doce.',               14.90, 10, 'Demon Slayer',      'Nezuko'),
 (16, 'Sobremesa',       'Chocolate do Sebastian',  'Sobremesa de chocolate intenso com mousse cremosa e raspas de chocolate.',                       22.90,  8, 'Black Butler',      'Sebastian'),
 (17, 'Sobremesa',       'Bat Berry Cake',          'Bolo de chocolate com recheio de frutas vermelhas e decoração inspirada em morcegos.',           18.90,  8, 'Original',          NULL),
 (18, 'Sobremesa',       'Taiyaki do Gojo',         'Taiyaki crocante recheado com creme de baunilha e chocolate branco.',                            16.90, 12, 'Jujutsu Kaisen',    'Gojo'),
 (19, 'Bebida',          'Six Eyes',                'Soda refrescante de blueberry e limão com coloração azul intensa.',                              15.90,  5, 'Jujutsu Kaisen',    'Gojo'),
 (20, 'Bebida',          'Anya Strawberry Milk',    'Leite cremoso com morango e espuma de leite.',                                                   13.90,  5, 'SPY×FAMILY',        'Anya'),
 (21, 'Bebida',          'Cursed Energy',           'Soda de uva e frutas vermelhas com toque cítrico.',                                              16.90,  5, 'Jujutsu Kaisen',    NULL),
 (22, 'Bebida',          'Nezuko Berry Soda',       'Soda de morango e frutas vermelhas com limão e gelo.',                                           15.90,  5, 'Demon Slayer',      'Nezuko'),
 (23, 'Bebida',          'Midnight Soda',           'Soda de uva, amora e limão com visual escuro e sabor cítrico.',                                  14.90,  5, 'Original',          NULL),
 (24, 'Bebida especial', 'Blood Moon',              'Mocktail de frutas vermelhas, morango, limão e soda, inspirado em uma lua vermelha.',            18.90,  6, 'Original',          NULL),
 (25, 'Massa',           'Black Flame Pasta',       'Massa ao molho cremoso de alho negro, cogumelos e parmesão, finalizada com ervas.',              32.90, 18, 'Original',          NULL)
) AS v (ordem, categoria, nome, descricao, preco, tempo, anime, personagem)
JOIN categoria c ON c.nome = v.categoria
ORDER BY v.ordem;

-- ---------- Fichas tecnicas (uma por prato, rendimento de 1 porcao) ----------
INSERT INTO ficha_tecnica (prato_id, rendimento, modo_preparo)
SELECT p.id, 1, v.modo_preparo
FROM (VALUES
 ('Ramen Ichiraku',         '1. Aquecer o caldo. 2. Cozinhar o macarrão por 2 minutos. 3. Montar na tigela com o caldo. 4. Finalizar com chashu, ovo, narutomaki e cebolinha.'),
 ('Curry do Sanji',         '1. Refogar cebola e carne. 2. Juntar batata e cenoura com água e cozinhar. 3. Dissolver o roux e engrossar. 4. Servir ao lado do arroz.'),
 ('Omurice da Anya',        '1. Refogar frango e cebola na manteiga. 2. Juntar o arroz e o ketchup. 3. Fazer a omelete macia. 4. Cobrir o arroz e finalizar com ketchup.'),
 ('Black Garlic Ramen',     '1. Bater o alho negro com o óleo. 2. Aquecer o caldo e cozinhar o macarrão. 3. Montar com chashu e ovo. 4. Finalizar com o óleo de alho negro e cebolinha.'),
 ('Gyudon do Denji',        '1. Cozinhar a cebola no shoyu com açúcar. 2. Juntar a carne em tiras e cozinhar rapidamente. 3. Servir sobre o arroz quente.'),
 ('Bento do Tanjiro',       '1. Grelhar o frango e glacear com teriyaki. 2. Cozinhar brócolis e cenoura no vapor. 3. Cozinhar o ovo. 4. Montar o bento com o arroz.'),
 ('Takoyaki U.A.',          '1. Preparar a massa com farinha e ovo. 2. Encher a chapa, colocar o polvo e virar até dourar. 3. Cobrir com molho, maionese, katsuobushi e cebolinha.'),
 ('Gyoza do Mikey',         '1. Misturar carne, repolho, alho e cebolinha. 2. Rechear e fechar as massas. 3. Dourar na chapa e finalizar no vapor. 4. Servir com shoyu.'),
 ('Onigiri do Shikamaru',   '1. Misturar o atum com a maionese. 2. Modelar o arroz morno com o recheio no centro. 3. Envolver com a alga nori.'),
 ('Batatas do Sukuna',      '1. Fritar as batatas até ficarem crocantes. 2. Temperar com páprica e alho. 3. Servir com o molho picante.'),
 ('Frango Imperial da Yor', '1. Cortar o frango em tiras. 2. Empanar na farinha, ovo e panko. 3. Fritar até dourar. 4. Servir com molho agridoce.'),
 ('Okonomiyaki U.A.',       '1. Misturar farinha, ovo e repolho. 2. Dourar na chapa com a barriga suína por cima. 3. Virar e finalizar com molho, maionese e katsuobushi.'),
 ('Sukuna Red Velvet',      '1. Bater a massa com cacau e assar. 2. Preparar o creme com cream cheese. 3. Montar em camadas e cobrir com a calda de frutas vermelhas.'),
 ('Parfait da Anya',        '1. Bater o creme com açúcar. 2. Montar em camadas com granola, morango e creme. 3. Finalizar com a calda de frutas vermelhas.'),
 ('Dango da Nezuko',        '1. Misturar a farinha de arroz com água e modelar as bolinhas. 2. Cozinhar até boiarem. 3. Espetar e cobrir com a calda de shoyu e açúcar.'),
 ('Chocolate do Sebastian', '1. Derreter o chocolate com a manteiga. 2. Incorporar as gemas e o creme batido. 3. Gelar por 4 horas. 4. Servir com raspas de chocolate.'),
 ('Bat Berry Cake',         '1. Bater a massa de chocolate e assar. 2. Rechear com a geleia de frutas vermelhas. 3. Decorar com os morcegos de chocolate.'),
 ('Taiyaki do Gojo',        '1. Preparar a massa com farinha, ovo e leite. 2. Fazer o creme com chocolate branco. 3. Assar na forma de taiyaki com o recheio no centro.'),
 ('Six Eyes',               '1. Colocar gelo no copo. 2. Adicionar o xarope de blueberry e o suco de limão. 3. Completar com água com gás.'),
 ('Anya Strawberry Milk',   '1. Amassar o morango com o açúcar. 2. Adicionar o leite gelado. 3. Finalizar com a espuma de creme.'),
 ('Cursed Energy',          '1. Macerar as frutas vermelhas com limão. 2. Adicionar gelo e o xarope de uva. 3. Completar com água com gás.'),
 ('Nezuko Berry Soda',      '1. Macerar as frutas vermelhas com limão. 2. Adicionar gelo e o xarope de morango. 3. Completar com água com gás.'),
 ('Midnight Soda',          '1. Macerar as frutas vermelhas com limão. 2. Adicionar gelo e o xarope de uva. 3. Completar com água com gás.'),
 ('Blood Moon',             '1. Bater morango e frutas vermelhas com o xarope. 2. Coar sobre o gelo. 3. Adicionar limão e completar com água com gás.'),
 ('Black Flame Pasta',      '1. Cozinhar o espaguete. 2. Saltear o shimeji na manteiga. 3. Juntar o creme e o alho negro batido. 4. Misturar a massa e finalizar com parmesão.')
) AS v (prato, modo_preparo)
JOIN prato p ON p.nome = v.prato;

-- ---------- Itens das fichas tecnicas ----------
-- quantidade = o que vai no prato; fator_correcao = perda no preparo (casca, limpeza).
-- custo do prato = SOMA(quantidade x fator_correcao x custo_unitario) / rendimento
INSERT INTO ficha_tecnica_item (ficha_tecnica_id, ingrediente_id, quantidade, unidade, fator_correcao)
SELECT f.id, i.id, v.quantidade, i.unidade_padrao, v.fator
FROM (VALUES
 ('Ramen Ichiraku', 'ING-001', 130, 1.00), ('Ramen Ichiraku', 'ING-002', 400, 1.00),
 ('Ramen Ichiraku', 'ING-003',  80, 1.00), ('Ramen Ichiraku', 'ING-004',   1, 1.00),
 ('Ramen Ichiraku', 'ING-005',  10, 1.15), ('Ramen Ichiraku', 'ING-006',  20, 1.00),

 ('Curry do Sanji', 'ING-007', 120, 1.00), ('Curry do Sanji', 'ING-008',  40, 1.00),
 ('Curry do Sanji', 'ING-009',  80, 1.20), ('Curry do Sanji', 'ING-010',  50, 1.15),
 ('Curry do Sanji', 'ING-011',  50, 1.10), ('Curry do Sanji', 'ING-012', 120, 1.10),

 ('Omurice da Anya', 'ING-007', 120, 1.00), ('Omurice da Anya', 'ING-004',   3, 1.00),
 ('Omurice da Anya', 'ING-013',  60, 1.10), ('Omurice da Anya', 'ING-014',  40, 1.00),
 ('Omurice da Anya', 'ING-015',  15, 1.00), ('Omurice da Anya', 'ING-011',  30, 1.10),

 ('Black Garlic Ramen', 'ING-001', 130, 1.00), ('Black Garlic Ramen', 'ING-002', 400, 1.00),
 ('Black Garlic Ramen', 'ING-016',  15, 1.00), ('Black Garlic Ramen', 'ING-003',  80, 1.00),
 ('Black Garlic Ramen', 'ING-004',   1, 1.00), ('Black Garlic Ramen', 'ING-005',  10, 1.15),
 ('Black Garlic Ramen', 'ING-036',  10, 1.00),

 ('Gyudon do Denji', 'ING-007', 150, 1.00), ('Gyudon do Denji', 'ING-012', 130, 1.10),
 ('Gyudon do Denji', 'ING-011',  60, 1.10), ('Gyudon do Denji', 'ING-017',  30, 1.00),
 ('Gyudon do Denji', 'ING-037',  10, 1.00),

 ('Bento do Tanjiro', 'ING-007', 150, 1.00), ('Bento do Tanjiro', 'ING-013', 140, 1.10),
 ('Bento do Tanjiro', 'ING-018',  40, 1.00), ('Bento do Tanjiro', 'ING-019',  60, 1.30),
 ('Bento do Tanjiro', 'ING-010',  40, 1.15), ('Bento do Tanjiro', 'ING-004',   1, 1.00),

 ('Takoyaki U.A.', 'ING-021',  60, 1.00), ('Takoyaki U.A.', 'ING-004',   1, 1.00),
 ('Takoyaki U.A.', 'ING-020',  40, 1.00), ('Takoyaki U.A.', 'ING-022',  20, 1.00),
 ('Takoyaki U.A.', 'ING-023',  15, 1.00), ('Takoyaki U.A.', 'ING-024',   3, 1.00),
 ('Takoyaki U.A.', 'ING-005',   5, 1.15),

 ('Gyoza do Mikey', 'ING-025',   6, 1.00), ('Gyoza do Mikey', 'ING-026',  90, 1.00),
 ('Gyoza do Mikey', 'ING-027',  50, 1.20), ('Gyoza do Mikey', 'ING-005',   5, 1.15),
 ('Gyoza do Mikey', 'ING-035',   5, 1.10), ('Gyoza do Mikey', 'ING-017',  20, 1.00),
 ('Gyoza do Mikey', 'ING-036',  10, 1.00),

 ('Onigiri do Shikamaru', 'ING-007', 140, 1.00), ('Onigiri do Shikamaru', 'ING-028',   1, 1.00),
 ('Onigiri do Shikamaru', 'ING-029',  25, 1.00), ('Onigiri do Shikamaru', 'ING-023',  10, 1.00),

 ('Batatas do Sukuna', 'ING-030', 220, 1.00), ('Batatas do Sukuna', 'ING-031',   3, 1.00),
 ('Batatas do Sukuna', 'ING-035',   5, 1.10), ('Batatas do Sukuna', 'ING-032',  30, 1.00),
 ('Batatas do Sukuna', 'ING-036',  30, 1.00),

 ('Frango Imperial da Yor', 'ING-013', 160, 1.10), ('Frango Imperial da Yor', 'ING-033',  30, 1.00),
 ('Frango Imperial da Yor', 'ING-021',  20, 1.00), ('Frango Imperial da Yor', 'ING-004',   1, 1.00),
 ('Frango Imperial da Yor', 'ING-034',  40, 1.00), ('Frango Imperial da Yor', 'ING-036',  40, 1.00),

 ('Okonomiyaki U.A.', 'ING-021',  80, 1.00), ('Okonomiyaki U.A.', 'ING-004',   1, 1.00),
 ('Okonomiyaki U.A.', 'ING-027', 120, 1.20), ('Okonomiyaki U.A.', 'ING-003',  50, 1.00),
 ('Okonomiyaki U.A.', 'ING-022',  30, 1.00), ('Okonomiyaki U.A.', 'ING-023',  20, 1.00),
 ('Okonomiyaki U.A.', 'ING-024',   2, 1.00),

 ('Sukuna Red Velvet', 'ING-021',  50, 1.00), ('Sukuna Red Velvet', 'ING-037',  40, 1.00),
 ('Sukuna Red Velvet', 'ING-038',   5, 1.00), ('Sukuna Red Velvet', 'ING-015',  25, 1.00),
 ('Sukuna Red Velvet', 'ING-004',   1, 1.00), ('Sukuna Red Velvet', 'ING-039',  40, 1.00),
 ('Sukuna Red Velvet', 'ING-040',  30, 1.00),

 ('Parfait da Anya', 'ING-041', 100, 1.10), ('Parfait da Anya', 'ING-042',  60, 1.00),
 ('Parfait da Anya', 'ING-043',  30, 1.00), ('Parfait da Anya', 'ING-040',  30, 1.00),
 ('Parfait da Anya', 'ING-037',  10, 1.00),

 ('Dango da Nezuko', 'ING-044',  80, 1.00), ('Dango da Nezuko', 'ING-037',  20, 1.00),
 ('Dango da Nezuko', 'ING-017',  10, 1.00),

 ('Chocolate do Sebastian', 'ING-045',  50, 1.00), ('Chocolate do Sebastian', 'ING-042',  60, 1.00),
 ('Chocolate do Sebastian', 'ING-004',   1, 1.00), ('Chocolate do Sebastian', 'ING-037',  15, 1.00),
 ('Chocolate do Sebastian', 'ING-015',  10, 1.00),

 ('Bat Berry Cake', 'ING-021',  50, 1.00), ('Bat Berry Cake', 'ING-037',  40, 1.00),
 ('Bat Berry Cake', 'ING-038',  15, 1.00), ('Bat Berry Cake', 'ING-004',   1, 1.00),
 ('Bat Berry Cake', 'ING-015',  20, 1.00), ('Bat Berry Cake', 'ING-040',  40, 1.00),

 ('Taiyaki do Gojo', 'ING-021',  60, 1.00), ('Taiyaki do Gojo', 'ING-004',   1, 1.00),
 ('Taiyaki do Gojo', 'ING-047',  60, 1.00), ('Taiyaki do Gojo', 'ING-037',  15, 1.00),
 ('Taiyaki do Gojo', 'ING-046',  25, 1.00), ('Taiyaki do Gojo', 'ING-042',  30, 1.00),

 ('Six Eyes', 'ING-048',  40, 1.00), ('Six Eyes', 'ING-052',  30, 1.50),
 ('Six Eyes', 'ING-051', 250, 1.00),

 ('Anya Strawberry Milk', 'ING-047', 250, 1.00), ('Anya Strawberry Milk', 'ING-041',  70, 1.10),
 ('Anya Strawberry Milk', 'ING-037',  15, 1.00), ('Anya Strawberry Milk', 'ING-042',  20, 1.00),

 ('Cursed Energy', 'ING-049',  40, 1.00), ('Cursed Energy', 'ING-040',  30, 1.00),
 ('Cursed Energy', 'ING-052',  20, 1.50), ('Cursed Energy', 'ING-051', 250, 1.00),

 ('Nezuko Berry Soda', 'ING-050',  40, 1.00), ('Nezuko Berry Soda', 'ING-040',  30, 1.00),
 ('Nezuko Berry Soda', 'ING-052',  20, 1.50), ('Nezuko Berry Soda', 'ING-051', 250, 1.00),

 ('Midnight Soda', 'ING-049',  40, 1.00), ('Midnight Soda', 'ING-040',  20, 1.00),
 ('Midnight Soda', 'ING-052',  20, 1.50), ('Midnight Soda', 'ING-051', 250, 1.00),

 ('Blood Moon', 'ING-040',  50, 1.00), ('Blood Moon', 'ING-041',  50, 1.10),
 ('Blood Moon', 'ING-050',  20, 1.00), ('Blood Moon', 'ING-052',  20, 1.50),
 ('Blood Moon', 'ING-051', 200, 1.00),

 ('Black Flame Pasta', 'ING-053', 120, 1.00), ('Black Flame Pasta', 'ING-016',  10, 1.00),
 ('Black Flame Pasta', 'ING-054',  60, 1.10), ('Black Flame Pasta', 'ING-042',  80, 1.00),
 ('Black Flame Pasta', 'ING-055',  20, 1.00), ('Black Flame Pasta', 'ING-015',  10, 1.00)
) AS v (prato, sku, quantidade, fator)
JOIN prato p         ON p.nome = v.prato
JOIN ficha_tecnica f ON f.prato_id = p.id
JOIN ingrediente i   ON i.sku = v.sku;

-- ---------- Fornecedores (empresas ficticias, CNPJs com digito verificador valido) ----------
INSERT INTO fornecedor (razao_social, cnpj, telefone, email, categorias_produtos) VALUES
('Nippon Importadora de Alimentos Ltda', '48152736000101', '1130001001', 'vendas@nipponimportadora.example', 'Produtos orientais, molhos, massas'),
('Hortifruti Vale Verde Ltda',           '30591842000126', '1130001002', 'pedidos@valeverde.example',        'Legumes, verduras, frutas'),
('Frigorífico Serra Dourada Ltda',       '72630415000150', '1130001003', 'comercial@serradourada.example',   'Carnes, aves, ovos'),
('Doce Empório Distribuidora Ltda',      '19406287000182', '1130001004', 'contato@doceemporio.example',      'Confeitaria, laticínios, xaropes');

-- Catalogo: alguns ingredientes aparecem em mais de um fornecedor para a cotacao comparativa.
INSERT INTO fornecedor_produto (fornecedor_id, ingrediente_id, preco, unidade_venda)
SELECT fo.id, i.id, v.preco, i.unidade_padrao
FROM (VALUES
 ('48152736000101', 'ING-001', 0.0180), ('48152736000101', 'ING-006', 0.0600),
 ('48152736000101', 'ING-007', 0.0120), ('48152736000101', 'ING-008', 0.0500),
 ('48152736000101', 'ING-016', 0.1800), ('48152736000101', 'ING-017', 0.0180),
 ('48152736000101', 'ING-020', 0.1250), ('48152736000101', 'ING-024', 0.3000),
 ('48152736000101', 'ING-025', 0.3500), ('48152736000101', 'ING-028', 0.9000),
 ('30591842000126', 'ING-005', 0.0200), ('30591842000126', 'ING-009', 0.0060),
 ('30591842000126', 'ING-010', 0.0050), ('30591842000126', 'ING-011', 0.0050),
 ('30591842000126', 'ING-027', 0.0050), ('30591842000126', 'ING-041', 0.0250),
 ('30591842000126', 'ING-052', 0.0080), ('30591842000126', 'ING-054', 0.0500),
 ('30591842000126', 'ING-004', 0.8500),
 ('72630415000150', 'ING-003', 0.0320), ('72630415000150', 'ING-004', 0.8000),
 ('72630415000150', 'ING-012', 0.0450), ('72630415000150', 'ING-013', 0.0200),
 ('72630415000150', 'ING-026', 0.0250), ('72630415000150', 'ING-020', 0.1200),
 ('19406287000182', 'ING-037', 0.0050), ('19406287000182', 'ING-040', 0.0450),
 ('19406287000182', 'ING-042', 0.0300), ('19406287000182', 'ING-045', 0.0700),
 ('19406287000182', 'ING-046', 0.0750), ('19406287000182', 'ING-048', 0.0600),
 ('19406287000182', 'ING-041', 0.0270), ('19406287000182', 'ING-007', 0.0135)
) AS v (cnpj, sku, preco)
JOIN fornecedor fo ON fo.cnpj = v.cnpj
JOIN ingrediente i ON i.sku = v.sku;

-- Primeiro ponto do historico de precos (grafico do RF-026).
INSERT INTO fornecedor_preco_historico (fornecedor_id, ingrediente_id, preco)
SELECT fornecedor_id, ingrediente_id, preco FROM fornecedor_produto;

-- ---------- Estoque inicial ----------
-- Cada ingrediente recebe uma ENTRADA de ajuste. Tres ficam abaixo do minimo de
-- proposito (alho negro, polvo e katsuobushi) para os alertas terem o que mostrar.
INSERT INTO estoque_movimentacao (ingrediente_id, tipo, quantidade, motivo, lote, custo_unitario, usuario_id)
SELECT i.id, 'ENTRADA', v.quantidade, 'AJUSTE', 'INICIAL', i.custo_unitario, u.id
FROM (VALUES
 ('ING-001', 12000), ('ING-002', 40000), ('ING-003',  8000), ('ING-004',   300), ('ING-005',  1500),
 ('ING-006',  1500), ('ING-007', 25000), ('ING-008',  4000), ('ING-009',  8000), ('ING-010',  6000),
 ('ING-011',  8000), ('ING-012', 12000), ('ING-013', 15000), ('ING-014',  2500), ('ING-015',  4000),
 ('ING-016',   200), ('ING-017',  5000), ('ING-018',  4000), ('ING-019',  4000), ('ING-020',   600),
 ('ING-021', 15000), ('ING-022',  3000), ('ING-023',  2500), ('ING-024',    60), ('ING-025',   600),
 ('ING-026',  8000), ('ING-027',  8000), ('ING-028',   150), ('ING-029',  2500), ('ING-030', 20000),
 ('ING-031',   500), ('ING-032',  2500), ('ING-033',  4000), ('ING-034',  4000), ('ING-035',  1500),
 ('ING-036', 15000), ('ING-037', 10000), ('ING-038',  2000), ('ING-039',  4000), ('ING-040', 10000),
 ('ING-041',  8000), ('ING-042', 10000), ('ING-043',  2500), ('ING-044',  5000), ('ING-045',  4000),
 ('ING-046',  2500), ('ING-047', 20000), ('ING-048',  2500), ('ING-049',  4000), ('ING-050',  4000),
 ('ING-051', 40000), ('ING-052',  4000), ('ING-053', 10000), ('ING-054',  4000), ('ING-055',  2000)
) AS v (sku, quantidade)
JOIN ingrediente i ON i.sku = v.sku
JOIN usuario u     ON u.email = 'admin@email.com';
