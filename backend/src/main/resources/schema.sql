-- Tabla Preaprobados de clientes, basicamente maneja los campos mencionados en la prueba
CREATE TABLE IF NOT EXISTS pre_approved (
    id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    available_amount DECIMAL(15, 2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0 ---Adiciona este campo porque va a ser el que maneja los cambios a los montos
);

-- Tabla Historial de solicitudes procesadas
CREATE TABLE IF NOT EXISTS usage_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_reference VARCHAR(100) NOT NULL UNIQUE, -- "Codigo de factura"
    pre_approved_id VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL, --Se adiciona a esta tabla el campo del estado, para saber en que quedo la solicitud
    rejection_reason VARCHAR(255), -- Si se rechaza, se almacena la observacion o el por que?
    processed_at TIMESTAMP NOT NULL -- fecha de la solcilitud
);

-- Datos iniciales dados en la prueba
DELETE FROM pre_approved;
INSERT INTO pre_approved (id, customer_id, status, available_amount, version) VALUES
('PRA-1001', 'USR-10', 'ACTIVE', 1000000.00, 0),
('PRA-1002', 'USR-10', 'BLOCKED', 800000.00, 0),
('PRA-2001', 'USR-20', 'ACTIVE', 2000000.00, 0);
