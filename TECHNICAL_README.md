# Fleet Temperature Dashboard - Technical Documentation

## 📋 Proje Özeti

**Fleet Temperature Dashboard**, uçak kabin sıcaklık verilerini gerçek zamanlı olarak izleyen, Kafka üzerinden veri alan, TimescaleDB'de saklayan ve WebSocket ile frontend'e ileten bir Spring Boot uygulamasıdır.

## 🏗️ Mimari Yapı

```
VHF Simulator → Kafka → Fleet Temperature Dashboard → TimescaleDB
                                    ↓
                              WebSocket → React Frontend
```

### **Teknoloji Stack:**
- **Backend**: Java 21, Spring Boot 3.2.0
- **Database**: TimescaleDB (PostgreSQL extension)
- **Message Queue**: Apache Kafka
- **Real-time Communication**: WebSocket (STOMP)
- **Frontend**: React.js
- **Containerization**: Docker & Docker Compose

## 🚀 Performance Optimizations

### **1. Asynchronous Processing**
```java
@Async("asyncExecutor")
@Transactional(timeout = 60)
public void processBatchMessages(List<VHFMessageDto> messages)
```
- **Thread Pool**: 20 core → 100 max threads
- **Queue Capacity**: 2000 tasks
- **Keep-alive**: 120 seconds

### **2. Kafka Consumer Optimization**
```java
@Async("kafkaConsumerExecutor")
public void consumeVHFMessageBatch(...)
```
- **Thread Pool**: 4 core → 8 max threads
- **Queue Capacity**: 1000 tasks
- **Batch Processing**: 100 messages per batch
- **Poll Timeout**: 10 minutes (600,000ms)

### **3. Database Connection Pool Tuning**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### **4. WAL (Write-Ahead Log) Optimization**
```yaml
# postgresql.conf
synchronous_commit = off
wal_buffers = 16MB
checkpoint_segments = 32
checkpoint_completion_target = 0.9
max_wal_size = 2GB
min_wal_size = 80MB
```

## 🛡️ Data Loss Prevention

### **1. Kafka Consumer Acknowledgment**
```java
@KafkaListener(
    topics = KafkaConstants.MAIN_TOPIC,
    groupId = KafkaConstants.CONSUMER_GROUP,
    batch = "true"
)
public void consumeVHFMessageBatch(
    @Payload List<VHFMessageDto> messages,
    @Header(KafkaHeaders.ACKNOWLEDGMENT) Acknowledgment acknowledgment
) {
    try {
        processBatchMessages(messages);
        acknowledgment.acknowledge(); // ✅ Başarılı işlem sonrası
    } catch (Exception e) {
        handleBatchProcessingError(messages, e);
        acknowledgment.acknowledge(); // ✅ Hata durumunda da acknowledge
    }
}
```

### **2. Dead Letter Queue (DLQ) Implementation**
```java
private void sendToDeadLetterQueue(VHFMessageDto message, Exception error) {
    try {
        DLQMessage dlqMessage = createDLQMessage(message, error);
        kafkaTemplate.send(KafkaConstants.DLQ_TOPIC, message.getMessageId(), dlqMessage);
    } catch (Exception e) {
        throw new KafkaProcessingException("Failed to send message to DLQ", ...);
    }
}
```

### **3. Transaction Management**
```java
@Transactional(timeout = 30)
public void processVHFMessage(VHFMessageDto message) {
    // Her message için ayrı transaction
}

@Transactional(timeout = 60)
public void processBatchMessages(List<VHFMessageDto> messages) {
    // Batch processing için extended timeout
}
```

### **4. Error Handling & Retry Logic**
- **Exception Types**: Custom exception hierarchy
- **Global Exception Handler**: Centralized error handling
- **Logging Standardization**: Structured logging with constants
- **Graceful Degradation**: Partial failure handling

## 🗄️ TimescaleDB Configuration & Usage

### **Neden TimescaleDB?**

TimescaleDB, zaman serisi verileri için optimize edilmiş PostgreSQL extension'ıdır:

1. **Hypertables**: Otomatik partitioning by time
2. **Continuous Aggregates**: Pre-computed time-based aggregations
3. **Compression**: Automatic data compression
4. **Retention Policies**: Automatic data cleanup

### **Docker Configuration**
```yaml
postgres:
  image: timescale/timescaledb:latest-pg15
  environment:
    POSTGRES_SHARED_PRELOAD_LIBRARIES: "timescaledb"
    POSTGRES_MAX_CONNECTIONS: 200
    POSTGRES_SHARED_BUFFERS: 256MB
    POSTGRES_EFFECTIVE_CACHE_SIZE: 1GB
```

### **Schema Design**
```sql
-- Hypertable creation
SELECT create_hypertable('cabin_temperature_readings', 'timestamp');

-- Continuous aggregates (5, 10, 15 minute intervals)
CREATE MATERIALIZED VIEW temperature_5min_avg
WITH (timescaledb.continuous) AS
SELECT time_bucket('5 minutes', timestamp) AS bucket,
       aircraft_id,
       cabin_zone,
       AVG(temperature_celsius) AS avg_temp,
       COUNT(*) AS reading_count
FROM cabin_temperature_readings
GROUP BY bucket, aircraft_id, cabin_zone;

-- Compression policy
SELECT add_compression_policy('cabin_temperature_readings', INTERVAL '7 days');

-- Retention policy
SELECT add_retention_policy('cabin_temperature_readings', INTERVAL '90 days');
```

### **Performance Benefits**
- **Query Performance**: 10-100x faster than regular PostgreSQL
- **Storage Efficiency**: Up to 90% compression ratio
- **Automatic Maintenance**: Background compression and cleanup
- **Scalability**: Handles millions of time-series records

## 📊 Kafka Configuration

### **Producer Settings (VHF Simulator)**
```java
configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
configProps.put(ProducerConfig.ACKS_CONFIG, "all");
```

### **Consumer Settings (Dashboard)**
```yaml
spring:
  kafka:
    consumer:
      max.poll.records: 100
      max.poll.interval.ms: 600000
      session.timeout.ms: 60000
      heartbeat.interval.ms: 20000
      fetch.min.bytes: 1024
      fetch.max.wait.ms: 500
```

### **Topic Configuration**
```yaml
kafka:
  topic:
    main: fleet.cabin_temperature.raw
    dlq: fleet.cabin_temperature.raw.dlq
    final-dlq: fleet.cabin_temperature.raw.final.dlq
```

## 🔄 Real-time Data Flow

### **1. Data Ingestion**
```
VHF Simulator → Kafka Producer → fleet.cabin_temperature.raw
```

### **2. Data Processing**
```
Kafka Consumer → Batch Processing → VHFMessageProcessorService
```

### **3. Data Storage**
```
VHFMessageProcessorService → CabinTemperatureReading → TimescaleDB
```

### **4. Real-time Updates**
```
TimescaleDB → WebSocketService → React Frontend
```

## 📈 Monitoring & Observability

### **Spring Actuator Endpoints**
- `/actuator/health` - Application health
- `/actuator/metrics` - Performance metrics
- `/actuator/prometheus` - Prometheus metrics

### **Kafka Monitoring**
- Consumer lag monitoring
- Producer throughput metrics
- Topic partition distribution

### **Database Monitoring**
- Connection pool status
- Query performance
- WAL generation rate

## 🚀 Deployment

### **Quick Start**
```bash
# 1. Start infrastructure
docker-compose up -d

# 2. Build application
mvn clean package

# 3. Run application
java -jar target/fleet-temperature-dashboard-1.0-SNAPSHOT.jar
```

### **Environment Variables**
```bash
export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fleet_temperature
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
```

## 🔧 Troubleshooting

### **Common Issues**

#### **1. Kafka Consumer Poll Timeout**
```bash
# Check consumer group status
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group fleet-temperature-group
```

#### **2. Database Connection Issues**
```bash
# Check TimescaleDB status
docker exec -it fleet-temperature-dashboard-db pg_isready -U postgres
```

#### **3. Memory Issues**
```bash
# Check JVM memory usage
jstat -gc <pid>
```

### **Performance Tuning**
1. **Increase thread pool sizes** for high message volumes
2. **Adjust batch sizes** based on memory constraints
3. **Optimize TimescaleDB compression policies**
4. **Monitor WAL generation** and adjust checkpoint settings

## 📚 Additional Resources

- [TimescaleDB Documentation](https://docs.timescale.com/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kafka Performance Tuning](https://kafka.apache.org/documentation/#performance)

---

**Not**: Bu dokümantasyon, projenin teknik detaylarını ve performans optimizasyonlarını açıklamaktadır. Production deployment öncesi güvenlik ve monitoring konfigürasyonları eklenmelidir.
