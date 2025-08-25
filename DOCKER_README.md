# Docker Setup - Fleet Temperature Dashboard

Bu proje 3 microservice'den oluşur ve Docker ile çalıştırılabilir.

## 🏗️ Proje Yapısı

```
fleet-temperature/
├── fleet-temperature-1/           # Backend (Spring Boot)
├── fleet-temperature-frontend/    # Frontend (React)
└── vhf-simulator-standalone/      # Simulator (Spring Boot)
```

## 🚀 Hızlı Başlangıç

### 1. Tüm Servisleri Başlat
```bash
# Ana dizinde (fleet-temperature-1)
docker-compose up -d
```

### 2. Servisleri Kontrol Et
```bash
# Tüm servislerin durumunu gör
docker-compose ps

# Logları takip et
docker-compose logs -f
```

### 3. Servisleri Durdur
```bash
docker-compose down
```

## 📋 Servis Detayları

### **Infrastructure Services**
| Service | Port | Açıklama |
|---------|------|----------|
| **Zookeeper** | 2181 | Kafka için metadata management |
| **Kafka** | 9092 | Message queue |
| **PostgreSQL** | 5432 | TimescaleDB database |
| **Kafka UI** | 8080 | Kafka monitoring interface |

### **Microservices**
| Service | Port | Açıklama |
|---------|------|----------|
| **Backend** | 8082 | Spring Boot API |
| **Frontend** | 3000 | React UI |
| **Simulator** | 8081 | VHF message generator |

## 🔧 Docker Komutları

### **Build & Run**
```bash
# Tüm servisleri build et ve başlat
docker-compose up --build -d

# Sadece belirli servisi build et
docker-compose build fleet-temperature-backend

# Sadece belirli servisi başlat
docker-compose up -d fleet-temperature-backend
```

### **Monitoring**
```bash
# Servis loglarını gör
docker-compose logs fleet-temperature-backend

# Real-time log takibi
docker-compose logs -f fleet-temperature-backend

# Servis durumunu kontrol et
docker-compose ps
```

### **Maintenance**
```bash
# Servisleri yeniden başlat
docker-compose restart

# Belirli servisi yeniden başlat
docker-compose restart fleet-temperature-backend

# Tüm servisleri durdur ve sil
docker-compose down -v
```

## 🐛 Troubleshooting

### **Port Çakışması**
```bash
# Hangi portların kullanıldığını kontrol et
lsof -i :8082
lsof -i :3000
lsof -i :8081

# Servisleri durdur
docker-compose down
```

### **Build Hataları**
```bash
# Docker cache'i temizle
docker system prune -a

# Yeniden build et
docker-compose build --no-cache
```

### **Database Bağlantı Sorunları**
```bash
# PostgreSQL container'ını kontrol et
docker exec -it fleet-temperature-dashboard-db pg_isready -U postgres

# Database'e bağlan
docker exec -it fleet-temperature-dashboard-db psql -U postgres -d fleet_temperature
```

## 📊 Health Checks

Her servis health check ile izlenir:

- **Backend**: `http://localhost:8082/actuator/health`
- **Frontend**: `http://localhost:3000/`
- **Simulator**: `http://localhost:8081/actuator/health`
- **Kafka UI**: `http://localhost:8080/`

## 🔄 Development Workflow

### **1. Code Değişikliği Sonrası**
```bash
# Sadece değişen servisi rebuild et
docker-compose build fleet-temperature-backend

# Servisi yeniden başlat
docker-compose up -d fleet-temperature-backend
```

### **2. Yeni Dependency Ekleme**
```bash
# Backend için
cd fleet-temperature-1
mvn clean package
docker-compose build fleet-temperature-backend

# Frontend için
cd fleet-temperature-frontend
npm install
docker-compose build fleet-temperature-frontend
```

### **3. Environment Variables**
```bash
# .env dosyası oluştur
cp .env.example .env

# Environment variables'ları düzenle
nano .env
```

## 🚨 Önemli Notlar

1. **Port Mapping**: Her servis farklı port kullanır
2. **Dependencies**: Servisler sıralı olarak başlatılır
3. **Health Checks**: Tüm servisler health check ile izlenir
4. **Volumes**: Database verisi kalıcı olarak saklanır
5. **Networks**: Servisler otomatik olarak aynı network'te çalışır

## 📚 Ek Kaynaklar

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [React Docker](https://create-react-app.dev/docs/deployment/#docker)
- [TimescaleDB Docker](https://docs.timescale.com/install/latest/self-hosted/docker/)
