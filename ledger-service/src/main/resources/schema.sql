CREATE TABLE legder_transaction {
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(100) NOT NULL,
    reference_id BIGINT NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    order_id VARCHAR NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
};

CREATE TABLE accounts {
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
};

CREATE TABLE ledger_entries {
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(15, 2) NOT NULL,
    account_id BIGINT NOT NULL,
    transaction_id BIGINT NOT NULL,
    type ENUM('CREDIT', 'DEBIT') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(transaction_id) REFERENCES accounts(id)
}