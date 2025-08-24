# Fleet Temperature Dashboard

## Proje Açıklaması

Fleet Temperature Dashboard, havayolu filosundaki uçakların kabin sıcaklık verilerini gerçek zamanlı olarak izleyen ve analiz eden bir uygulamadır. VHF data link üzerinden gelen sensör verilerini Kafka'dan alır, TimescaleDB'de saklar ve web dashboard üzerinde görselleştirir.

## Özellikler

### 🚀 **Gerçek Zamanlı İzleme**
- VHF mesajlarından gelen sıcaklık verilerini anlık işleme
- WebSocket ile gerçek zamanlı dashboard güncellemeleri
- 100-5000 mesaj/saniye yüksek performans desteği

### 📊 **Dashboard ve Görselleştirme**
- Uçak bazında sıcaklık haritaları
- Zaman serisi grafikleri ve trend analizi
- Sıcaklık alarmları ve uyarı sistemi
- Responsive web arayüzü

### 🔔 **Alarm Sistemi**
- Yüksek sıcaklık uyarıları (25°C üzeri)
- Kritik sıcaklık alarmları (28°C üzeri)
- E-posta ve WebSocket bildirimleri
- Alarm geçmişi ve yönetimi

### ⚡ **Yüksek Performans**
- Kafka batch processing
- Asenkron mesaj işleme
- TimescaleDB hypertables ve compression
- Connection pooling ve WAL optimizasyonları

## Teknik Gereksinimler

- Java 21
- Spring Boot 3.2.0
- PostgreSQL 15 + TimescaleDB
- Apache Kafka
- Maven 3.8+

## Kurulum

### **1. Projeyi Klonla**
```bash
git clone <repository-url>
cd fleet-temperature-1
```

### **2. Maven Dependencies**
```bash
mvn clean install
```

### **3. Docker Compose ile Servisleri Başlat**
```bash
docker-compose up -d
```

Bu komut şunları başlatacak:
- PostgreSQL (TimescaleDB) - Port 5432
- Zookeeper - Port 2181
- Kafka - Port 9092

### **4. Kafka Topic'leri Oluştur**
```bash
# Kafka topic'lerini oluştur
./kafka-init.sh
```

### **5. Uygulamayı Çalıştır**
```bash
mvn spring-boot:run
```

Uygulama `http://localhost:8080` adresinde çalışacak.

## 🎮 **Kullanım**

### **REST API Endpoints**

#### **Dashboard Verileri**
```bash
# Tüm uçakların genel durumu
GET /api/dashboard/overview

# Belirli uçak için detaylı bilgi
GET /api/dashboard/aircraft/{aircraftId}

# Zaman aralığında sıcaklık verileri
GET /api/dashboard/temperatures?startTime=2024-01-01T00:00:00Z&endTime=2024-01-01T23:59:59Z
```

#### **Alarm Yönetimi**
```bash
# Aktif alarmları listele
GET /api/alarms/active

# Alarm geçmişi
GET /api/alarms/history?startDate=2024-01-01&endDate=2024-01-31

# Alarm durumunu güncelle
PUT /api/alarms/{alarmId}/status
```

#### **Sağlık Kontrolü**
```bash
# Uygulama sağlığı
GET /actuator/health

# Metrikler
GET /actuator/metrics
```

## 🏗️ **Mimari**

### **Katman Yapısı**
```
┌─────────────────────────────────────┐
│           Web Layer                 │
│  ┌─────────────┐ ┌──────────────┐  │
│  │ Controller  │ │ WebSocket    │  │
│  └─────────────┘ └──────────────┘  │
├─────────────────────────────────────┤
│           Service Layer             │
│  ┌─────────────┐ ┌──────────────┐  │
│  │ VHF Message │ │ Dashboard    │  │
│  │ Processor   │ │ Service      │  │
│  └─────────────┘ └──────────────┘  │
├─────────────────────────────────────┤
│           Data Layer                │
│  ┌─────────────┐ ┌──────────────┐  │
│  │ Repository  │ │ Kafka       │  │
│  │ Layer       │ │ Consumer    │  │
│  └─────────────┘ └──────────────┘  │
├─────────────────────────────────────┤
│           Infrastructure            │
│  ┌─────────────┐ ┌──────────────┐  │
│  │ TimescaleDB │ │ Kafka       │  │
│  │ PostgreSQL  │ │ Message     │  │
│  │             │ │ Queue       │  │
│  └─────────────┘ └──────────────┘  │
└─────────────────────────────────────┘
```

### **Veri Akışı**
1. **VHF Mesajları** → Kafka Topic
2. **Kafka Consumer** → Mesajları alır
3. **Message Processor** → Verileri işler
4. **Database** → TimescaleDB'ye kaydeder
5. **WebSocket** → Dashboard'a gönderir

## 📊 **Veritabanı Şeması**

### **Ana Tablolar**
- `aircraft` - Uçak bilgileri
- `cabin_temperature_readings` - Sıcaklık okumaları (Hypertable)
- `temperature_alarms` - Sıcaklık alarmları

### **TimescaleDB Özellikleri**
- **Hypertables**: Zaman serisi veriler için optimize edilmiş tablolar
- **Compression**: Otomatik veri sıkıştırma
- **Continuous Aggregates**: Saatlik/günlük özet veriler
- **Retention Policies**: Otomatik veri temizleme

## 🔧 **Konfigürasyon**

### **Kafka Ayarları**
```yaml
spring:
  kafka:
    consumer:
      group-id: fleet-temperature-group
      auto-offset-reset: earliest
      properties:
        max.poll.records: 1000
        max.poll.interval.ms: 300000
```

### **Database Ayarları**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
  jpa:
    hibernate:
      jdbc.batch_size: 100
```

## 📈 **Performans Optimizasyonları**

### **Kafka Consumer**
- Batch processing (1000 mesaj/batch)
- 5 concurrent consumer
- Optimized poll timeouts

### **Database**
- Connection pooling (HikariCP)
- Batch inserts (100 kayıt/batch)
- WAL optimizasyonları
- TimescaleDB compression

### **Async Processing**
- Thread pool executor (10-50 threads)
- Non-blocking message processing
- WebSocket async updates

## 🧪 **Test Senaryoları**

### **Yük Testleri**
```bash
# Düşük yük: 100 msg/s
# Orta yük: 1000 msg/s  
# Yüksek yük: 5000 msg/s
```

### **Test Komutları**
```bash
# Unit testler
mvn test

# Integration testler
mvn verify

# Test coverage
mvn jacoco:report
```

## 📝 **Geliştirme Notları**

### **Kod Kalitesi**
- **OOP Principles**: Encapsulation, Inheritance, Polymorphism
- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **Design Patterns**: Repository, Service, Factory, Observer

### **Best Practices**
- Lombok annotations for boilerplate code
- Spring Boot auto-configuration
- Exception handling with proper logging
- Async processing for high throughput

## 🚀 **Gelecek Geliştirmeler**

- [ ] Web UI dashboard
- [ ] Grafana metrik entegrasyonu
- [ ] Machine learning ile sıcaklık tahmini
- [ ] Mobile app desteği
- [ ] Multi-tenant architecture
- [ ] Kubernetes deployment
- [ ] Prometheus metrik export

## 📚 **Dokümantasyon**

- **API Docs**: Swagger/OpenAPI
- **Database Schema**: ERD diagrams
- **Architecture**: C4 model diagrams
- **Deployment**: Docker & Kubernetes guides

## 🤝 **Katkıda Bulunma**

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 **Lisans**

Bu proje MIT lisansı altında lisanslanmıştır.

## 📞 **İletişim**

Proje ile ilgili sorularınız için:
- GitHub Issues
- Email: [email]
- Slack: [slack-channel]
