CREATE DATABASE IF NOT EXISTS opendata;
CREATE TABLE IF NOT EXISTS opendata.events (id UInt64, name String, ts DateTime DEFAULT now(), payload Nullable(String)) ENGINE = MergeTree ORDER BY id;
INSERT INTO opendata.events (id, name, payload) VALUES (1,'alpha','{"a":1}'),(2,'beta',NULL),(3,'gamma','x');
CREATE VIEW IF NOT EXISTS opendata.events_v AS SELECT id, name FROM opendata.events;
