-- Enable TimescaleDB extension
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Create aircraft table
CREATE TABLE IF NOT EXISTS aircraft (
    id BIGSERIAL PRIMARY KEY,
    aircraft_id VARCHAR(10) UNIQUE NOT NULL,
    manufacturer VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    registration_number VARCHAR(20) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create cabin_temperature_readings table
CREATE TABLE IF NOT EXISTS cabin_temperature_readings (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(36) UNIQUE NOT NULL,
    aircraft_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    cabin_zone VARCHAR(10) NOT NULL,
    temperature_celsius DECIMAL(4,1) NOT NULL,
    temperature_fahrenheit DECIMAL(4,1) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aircraft FOREIGN KEY (aircraft_id) REFERENCES aircraft(id) ON DELETE CASCADE
);

-- Create temperature_alarms table
CREATE TABLE IF NOT EXISTS temperature_alarms (
    id BIGSERIAL PRIMARY KEY,
    aircraft_id BIGINT NOT NULL,
    reading_id BIGINT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    threshold_temperature DECIMAL(4,1) NOT NULL,
    threshold_unit VARCHAR(20) NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_alarm_aircraft FOREIGN KEY (aircraft_id) REFERENCES aircraft(id) ON DELETE CASCADE,
    CONSTRAINT fk_alarm_reading FOREIGN KEY (reading_id) REFERENCES cabin_temperature_readings(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_aircraft_aircraft_id ON aircraft(aircraft_id);
CREATE INDEX IF NOT EXISTS idx_aircraft_registration ON aircraft(registration_number);
CREATE INDEX IF NOT EXISTS idx_aircraft_status ON aircraft(status);
CREATE INDEX IF NOT EXISTS idx_aircraft_manufacturer_model ON aircraft(manufacturer, model);

CREATE INDEX IF NOT EXISTS idx_temperature_readings_message_id ON cabin_temperature_readings(message_id);
CREATE INDEX IF NOT EXISTS idx_temperature_readings_aircraft_id ON cabin_temperature_readings(aircraft_id);
CREATE INDEX IF NOT EXISTS idx_temperature_readings_timestamp ON cabin_temperature_readings(timestamp);
CREATE INDEX IF NOT EXISTS idx_temperature_readings_cabin_zone ON cabin_temperature_readings(cabin_zone);
CREATE INDEX IF NOT EXISTS idx_temperature_readings_status ON cabin_temperature_readings(status);
CREATE INDEX IF NOT EXISTS idx_temperature_readings_temperature ON cabin_temperature_readings(temperature_celsius);

CREATE INDEX IF NOT EXISTS idx_alarms_aircraft_id ON temperature_alarms(aircraft_id);
CREATE INDEX IF NOT EXISTS idx_alarms_reading_id ON temperature_alarms(reading_id);
CREATE INDEX IF NOT EXISTS idx_alarms_status ON temperature_alarms(status);
CREATE INDEX IF NOT EXISTS idx_alarms_severity ON temperature_alarms(severity);
CREATE INDEX IF NOT EXISTS idx_alarms_triggered_at ON temperature_alarms(triggered_at);

-- Convert cabin_temperature_readings to TimescaleDB hypertable
SELECT create_hypertable('cabin_temperature_readings', 'timestamp', if_not_exists => TRUE);

-- Create time-based compression policy (compress data older than 7 days)
SELECT add_compression_policy('cabin_temperature_readings', INTERVAL '7 days');

-- Create retention policy (keep data for 1 year)
SELECT add_retention_policy('cabin_temperature_readings', INTERVAL '1 year');

-- Create continuous aggregates for hourly and daily aggregations
CREATE MATERIALIZED VIEW IF NOT EXISTS cabin_temperature_hourly
WITH (timescaledb.continuous) AS
SELECT 
    aircraft_id,
    cabin_zone,
    time_bucket('1 hour', timestamp) AS bucket,
    AVG(temperature_celsius) AS avg_temperature_celsius,
    AVG(temperature_fahrenheit) AS avg_temperature_fahrenheit,
    MAX(temperature_celsius) AS max_temperature_celsius,
    MIN(temperature_celsius) AS min_temperature_celsius,
    COUNT(*) AS reading_count,
    COUNT(CASE WHEN status = 'WARNING' THEN 1 END) AS warning_count,
    COUNT(CASE WHEN status = 'CRITICAL' THEN 1 END) AS critical_count
FROM cabin_temperature_readings
GROUP BY aircraft_id, cabin_zone, bucket;

CREATE MATERIALIZED VIEW IF NOT EXISTS cabin_temperature_daily
WITH (timescaledb.continuous) AS
SELECT 
    aircraft_id,
    cabin_zone,
    time_bucket('1 day', timestamp) AS bucket,
    AVG(temperature_celsius) AS avg_temperature_celsius,
    AVG(temperature_fahrenheit) AS avg_temperature_fahrenheit,
    MAX(temperature_celsius) AS max_temperature_celsius,
    MIN(temperature_celsius) AS min_temperature_celsius,
    COUNT(*) AS reading_count,
    COUNT(CASE WHEN status = 'WARNING' THEN 1 END) AS warning_count,
    COUNT(CASE WHEN status = 'CRITICAL' THEN 1 END) AS critical_count
FROM cabin_temperature_readings
GROUP BY aircraft_id, cabin_zone, bucket;

-- Set refresh policies for continuous aggregates
SELECT add_continuous_aggregate_policy('cabin_temperature_hourly',
    start_offset => INTERVAL '3 hours',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour');

SELECT add_continuous_aggregate_policy('cabin_temperature_daily',
    start_offset => INTERVAL '3 days',
    end_offset => INTERVAL '1 day',
    schedule_interval => INTERVAL '1 day');

-- -- Insert sample aircraft data for testing
-- INSERT INTO aircraft (aircraft_id, manufacturer, model, year, registration_number, status)
-- VALUES
--     ('TC-ABC', 'Boeing', '737-800', 2020, 'TC-ABC', 'ACTIVE'),
--     ('TC-DEF', 'Airbus', 'A320', 2021, 'TC-DEF', 'ACTIVE'),
--     ('TC-GHI', 'Boeing', '777-300ER', 2019, 'TC-GHI', 'ACTIVE')
-- ON CONFLICT (aircraft_id) DO NOTHING;
