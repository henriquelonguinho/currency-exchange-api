CREATE TABLE purchase_transaction (
    id          UUID            DEFAULT RANDOM_UUID() PRIMARY KEY,
    description VARCHAR(50)     NOT NULL,
    date        DATE            NOT NULL,
    amount      DECIMAL(10, 2)  NOT NULL
);
