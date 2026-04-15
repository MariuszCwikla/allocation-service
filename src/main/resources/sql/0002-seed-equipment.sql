INSERT INTO equipments (type, brand, model, state, condition_score, purchase_date, retired_at, retirement_reason, version, created_at, updated_at)
SELECT
    type,
    brand,
    model,
    state,
    ROUND((random())::numeric, 2)              AS condition_score,
    (CURRENT_DATE - (random() * 1825)::int)     AS purchase_date,
    CASE WHEN state = 'retired' THEN NOW() - (random() * INTERVAL '365 days') END AS retired_at,
    CASE WHEN state = 'retired' THEN (ARRAY['damaged', 'end_of_life', 'lost', 'stolen'])[1 + (random() * 3)::int] END AS retirement_reason,
    0                                                                            AS version,
    NOW() - (random() * INTERVAL '730 days')                                    AS created_at,
    NOW() - (random() * INTERVAL '30 days')                                     AS updated_at
FROM (
    SELECT
        i,
        (ARRAY['main_computer', 'monitor', 'keyboard', 'mouse'])[1 + (i % 4)]  AS type,
        CASE (i % 4)
            WHEN 0 THEN (ARRAY['Dell', 'HP', 'Lenovo', 'Apple', 'Asus'])[1 + (i % 5)]
            WHEN 1 THEN (ARRAY['Dell', 'LG', 'Samsung', 'BenQ', 'AOC'])[1 + (i % 5)]
            WHEN 2 THEN (ARRAY['Logitech', 'Apple', 'Microsoft', 'Keychron', 'Corsair'])[1 + (i % 5)]
            WHEN 3 THEN (ARRAY['Logitech', 'Apple', 'Microsoft', 'Razer', 'SteelSeries'])[1 + (i % 5)]
        END AS brand,
        CASE (i % 4)
            WHEN 0 THEN (ARRAY['Latitude 5540', 'EliteBook 840 G10', 'ThinkPad X1 Carbon', 'MacBook Pro 14', 'ExpertBook B9'])[1 + (i % 5)]
            WHEN 1 THEN (ARRAY['UltraSharp U2723DE', '27UK850-W', 'Odyssey G7', 'PD2700U', '27G2SP'])[1 + (i % 5)]
            WHEN 2 THEN (ARRAY['MX Keys', 'Magic Keyboard', 'Ergonomic Keyboard', 'K2 Pro', 'K100 RGB'])[1 + (i % 5)]
            WHEN 3 THEN (ARRAY['MX Master 3S', 'Magic Mouse 3', 'Arc Mouse', 'DeathAdder V3', 'Rival 650'])[1 + (i % 5)]
        END AS model,
        (ARRAY['available', 'available', 'available', 'reserved', 'assigned', 'assigned', 'retired'])[1 + (i % 7)] AS state
    FROM generate_series(1, 5000) AS i
) data;