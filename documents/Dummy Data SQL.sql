-- Transaction start to ensure atomicity
BEGIN;

-- ---------------------------------------------------------
-- BOND 1: US Treasury Note (Safety Anchor)
-- Maturity: 5 Years | Coupon: 4.25% | Semi-annual payments
-- ---------------------------------------------------------
WITH new_bond AS (
    INSERT INTO bpra.bond (portfolio_id, isin, maturity_date, coupon_rate, face_value, market_price)
    VALUES (
        1,                  -- Assuming portfolio_id 1 exists
        'US912828Z946',     -- Realistic Dummy US Treasury ISIN
        '2028-05-15',       -- Maturity Date
        4.25,               -- Coupon Rate (%)
        1000.00,            -- Face Value
        995.50              -- Market Price (trading slightly below par)
    )
    RETURNING id
)
INSERT INTO bpra.bond_coupon_date (bond_id, coupon_date)
SELECT id, unnest(ARRAY[
    '2024-05-15', '2024-11-15',
    '2025-05-15', '2025-11-15',
    '2026-05-15', '2026-11-15',
    '2027-05-15', '2027-11-15',
    '2028-05-15'
]::timestamp[])
FROM new_bond;


-- ---------------------------------------------------------
-- BOND 2: Tech Corp Bond (Income Generator)
-- Maturity: 10 Years | Coupon: 5.50% | Semi-annual payments
-- ---------------------------------------------------------
WITH new_bond AS (
    INSERT INTO bpra.bond (portfolio_id, isin, maturity_date, coupon_rate, face_value, market_price)
    VALUES (
        1,
        'US037833CZ12',     -- Realistic Dummy Corp ISIN
        '2033-09-30',
        5.50,
        1000.00,
        1025.20             -- Trading at a premium (price > face value)
    )
    RETURNING id
)
INSERT INTO bpra.bond_coupon_date (bond_id, coupon_date)
SELECT id, unnest(ARRAY[
    '2024-03-30', '2024-09-30',
    '2025-03-30', '2025-09-30',
    '2026-03-30', '2026-09-30',
    '2027-03-30', '2027-09-30',
	'2028-03-30', '2028-09-30',
	'2029-03-30', '2029-09-30',
	'2030-03-30', '2030-09-30',
	'2031-03-30', '2031-09-30',
	'2032-03-30', '2032-09-30',
    '2033-03-30', '2033-09-30'
]::timestamp[])
FROM new_bond;


-- ---------------------------------------------------------
-- BOND 3: Municipal Bond (Tax Free)
-- Maturity: 3 Years | Coupon: 3.00% | Annual payments (typical for some munis)
-- ---------------------------------------------------------
WITH new_bond AS (
    INSERT INTO bpra.bond (portfolio_id, isin, maturity_date, coupon_rate, face_value, market_price)
    VALUES (
        1,
        'US592663AB15',     -- Realistic Dummy Muni ISIN
        '2026-07-01',
        3.00,
        5000.00,            -- Munis often have higher face values (e.g. 5k)
        4980.00
    )
    RETURNING id
)
INSERT INTO bpra.bond_coupon_date (bond_id, coupon_date)
SELECT id, unnest(ARRAY[
    '2024-07-01',
    '2025-07-01',
    '2026-07-01'
]::timestamp[])
FROM new_bond;

COMMIT;