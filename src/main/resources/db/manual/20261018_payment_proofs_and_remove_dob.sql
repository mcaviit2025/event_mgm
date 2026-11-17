ALTER TABLE registrations
    DROP COLUMN date_of_birth,
    DROP COLUMN utr_no,
    ADD COLUMN payment_proof_path VARCHAR(500) NULL,
    ADD COLUMN amount_due DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

ALTER TABLE team_members
    DROP COLUMN date_of_birth;

UPDATE registrations AS r
JOIN events AS e ON e.id = r.event_id
SET r.amount_due = COALESCE(e.event_fee, 0.00),
    r.payment_status = CASE
        WHEN COALESCE(e.event_fee, 0.00) = 0.00 THEN 'NOT_REQUIRED'
        ELSE r.payment_status
    END;
