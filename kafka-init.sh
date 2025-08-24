#!/bin/bash

# Kafka topic'leri oluştur
echo "Creating Kafka topics..."

# Ana VHF mesaj topic'i - 8 partition ile yüksek throughput
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic fleet.cabin_temperature.raw \
  --partitions 8 \
  --replication-factor 1 \
  --config retention.ms=604800000 \
  --config segment.ms=3600000 \
  --config cleanup.policy=delete

# Single message topic (fallback için)
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic fleet.cabin_temperature.raw.single \
  --partitions 4 \
  --replication-factor 1

# Dead Letter Queue topic
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic fleet.cabin_temperature.raw.dlq \
  --partitions 2 \
  --replication-factor 1 \
  --config retention.ms=2592000000

# Topic'leri listele
echo "Created topics:"
kafka-topics --bootstrap-server localhost:9092 --list

# Topic detaylarını göster
echo "Topic details:"
kafka-topics --bootstrap-server localhost:9092 --describe --topic fleet.cabin_temperature.raw
