CREATE TABLE payment_event(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    is_payment_done BOOLEAN NOT NULL DEFAULT FALSE,
    payment_key VARCHAR(255) UNIQUE,
    order_id VARCHAR(255) UNIQUE,
    type ENUM('NORMAL') NOT NULL,
    order_name VARCHAR(255) NOT NULL,
    method ENUM('EASY_PAY'),
    psp_raw_data JSON,
    approved_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payment_order(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_event_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_id VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    order_status ENUM('NOT_STARTED', 'EXECUTING', 'SUCCESS', 'FAILURE', 'UNKNOWN') NOT NULL DEFAULT 'NOT_STARTED',
    ledger_updated BOOLEAN NOT NULL DEFAULT FALSE,
    wallet_updated BOOLEAN NOT NULL DEFAULT FALSE,
    failed_count TINYINT NOT NULL DEFAULT 5,
    threshold TINYINT NOT NULL DEFAULT 5,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_event_id) REFERENCES payment_event(id)
);

CREATE TABLE payment_order_histories(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_order_id BIGINT NOT NULL,
    previous_status ENUM('NOT_STARTED', 'EXECUTING', 'SUCCESS', 'FAILURE', 'UNKNOWN'),
    new_status ENUM('NOT_STARTED', 'EXECUTING', 'SUCCESS', 'FAILURE', 'UNKNOWN'),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(255),
    reason VARCHAR(255),
    FOREIGN KEY (payment_order_id) REFERENCES payment_order(id)
);

CREATE TABLE outboxes(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    status ENUM('INIT', 'SUCCESS', 'FAILURE') DEFAULT 'INIT',
    type VARCHAR(40) NOT NULL,
    partition_key INT NOT NULL DEFAULT 0,
    payload JSON,
    metadata JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);