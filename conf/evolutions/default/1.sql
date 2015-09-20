# --- !Ups

CREATE TABLE user_transaction(
    id INT PRIMARY KEY AUTO_INCREMENT,
    op_date DATE NOT NULL,
    name VARCHAR(20) NOT NULL,
    operation ENUM("BUY","SELL") NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(24,4) NOT NULL,
    currency VARCHAR(12) NOT NULL,
    fx_rate DECIMAL(12,4) NOT NULL,
    total_cost DECIMAL(24,4) NOT NULL,
    profit_loss DECIMAL(24,4) NULL,
    cumulative_profit_loss DECIMAL(24,4) NOT NULL
);

CREATE TABLE transaction_events(
    id INT PRIMARY KEY AUTO_INCREMENT,
    op_date DATE NOT NULL,
    name VARCHAR(20) NOT NULL,
    operation ENUM("BUY","SELL") NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(24,4) NOT NULL,
    currency VARCHAR(12) NOT NULL,
    fx_rate DECIMAL(12,4) NOT NULL
);

# --- !Downs

DROP TABLE user_transaction;

DROP TABLE transaction_events;