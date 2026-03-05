-- ============================================================
--  Sample data for Trade Finance Service
--  Works with both MySQL and H2 (in-memory)
-- ============================================================

-- Parties
INSERT INTO party (id, name, type) VALUES (1, 'Acme Imports SRL',        'IMPORTER');
INSERT INTO party (id, name, type) VALUES (2, 'Global Traders SRL',      'IMPORTER');
INSERT INTO party (id, name, type) VALUES (3, 'Banca Transilvania',      'BANK');
INSERT INTO party (id, name, type) VALUES (4, 'ING Bank Romania',        'BANK');
INSERT INTO party (id, name, type) VALUES (5, 'EuroExport GmbH',         'EXPORTER');
INSERT INTO party (id, name, type) VALUES (6, 'Asia Pacific Goods Ltd',  'EXPORTER');

-- Trade Requests
INSERT INTO trade_request (id, importer_id, bank_id, exporter_id, goods_description, quantity, currency, status, created_at, final_price, delivery_details, exporter_decline_reason, vat_amount, version)
VALUES (1, 1, 3, 5, 'Industrial steel pipes, grade A', 500, 'EUR', 'SUBMITTED',     NOW(), NULL, NULL, NULL, NULL, 0);

INSERT INTO trade_request (id, importer_id, bank_id, exporter_id, goods_description, quantity, currency, status, created_at, final_price, delivery_details, exporter_decline_reason, vat_amount, version)
VALUES (2, 1, 3, 6, 'Electronic components batch #42', 1000, 'USD', 'BANK_APPROVED',  NOW(), NULL, NULL, NULL, NULL, 0);

INSERT INTO trade_request (id, importer_id, bank_id, exporter_id, goods_description, quantity, currency, status, created_at, final_price, delivery_details, exporter_decline_reason, vat_amount, version)
VALUES (3, 2, 4, 5, 'Textile goods – winter collection', 200, 'EUR', 'BANK_REJECTED',  NOW(), NULL, NULL, NULL, NULL, 0);

INSERT INTO trade_request (id, importer_id, bank_id, exporter_id, goods_description, quantity, currency, status, created_at, final_price, delivery_details, exporter_decline_reason, vat_amount, version)
VALUES (4, 2, 3, 6, 'Raw coffee beans, Arabica 100%', 5000, 'USD', 'EXPORTER_ACCEPTED', NOW(), 75000, 'Delivery via DHL within 30 days, FOB Rotterdam', NULL, 33750, 0);

INSERT INTO trade_request (id, importer_id, bank_id, exporter_id, goods_description, quantity, currency, status, created_at, final_price, delivery_details, exporter_decline_reason, vat_amount, version)
VALUES (5, 1, 4, 5, 'Pharmaceutical packaging materials', 300, 'EUR', 'EXPORTER_DECLINED', NOW(), NULL, NULL, 'Cannot fulfill order due to capacity constraints', NULL, 0);

-- History for trade request #1 (SUBMITTED)
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (1, 'CREATED', 'SUBMITTED', 'SUBMITTED', 1, 'IMPORTER', 'Trade request created', NOW());

-- History for trade request #2 (BANK_APPROVED)
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (2, 'CREATED',       'SUBMITTED',     'SUBMITTED',     1, 'IMPORTER', 'Trade request created', NOW());
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (2, 'BANK_APPROVED', 'SUBMITTED',     'BANK_APPROVED', 3, 'BANK',     NULL,                    NOW());

-- History for trade request #3 (BANK_REJECTED)
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (3, 'CREATED',      'SUBMITTED', 'SUBMITTED',    2, 'IMPORTER', 'Trade request created',              NOW());
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (3, 'BANK_REJECTED','SUBMITTED', 'BANK_REJECTED', 4, 'BANK',    'Insufficient documentation provided', NOW());

-- History for trade request #4 (EXPORTER_ACCEPTED)
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (4, 'CREATED',           'SUBMITTED',     'SUBMITTED',        2, 'IMPORTER', 'Trade request created',                           NOW());
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (4, 'BANK_APPROVED',     'SUBMITTED',     'BANK_APPROVED',    3, 'BANK',     NULL,                                              NOW());
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (4, 'EXPORTER_ACCEPTED', 'BANK_APPROVED', 'EXPORTER_ACCEPTED',6, 'EXPORTER','Delivery via DHL within 30 days, FOB Rotterdam',   NOW());

-- History for trade request #5 (EXPORTER_DECLINED -> back to bank)
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (5, 'CREATED',           'SUBMITTED',     'SUBMITTED',         1, 'IMPORTER', 'Trade request created',                               NOW());
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (5, 'BANK_APPROVED',     'SUBMITTED',     'BANK_APPROVED',     4, 'BANK',     NULL,                                                  NOW());
INSERT INTO trade_request_history (trade_request_id, action, from_status, to_status, actor_party_id, actor_party_type, comment, happened_at)
VALUES (5, 'EXPORTER_DECLINED', 'BANK_APPROVED', 'EXPORTER_DECLINED', 5, 'EXPORTER','Cannot fulfill order due to capacity constraints',     NOW());


-- Reset auto-increment sequences after manual inserts
ALTER TABLE party ALTER COLUMN id RESTART WITH 10;
ALTER TABLE trade_request ALTER COLUMN id RESTART WITH 10;
ALTER TABLE trade_request_history ALTER COLUMN id RESTART WITH 100;