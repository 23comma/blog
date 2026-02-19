# 🚀 STS Boot Sample Blog Project (MariaDB)

23comma님의 블로그 프로젝트를 기반으로 한 **Spring Boot + MariaDB** 샘플 블로그 애플리케이션입니다. 
이 프로젝트는 기본적인 CRUD 기능과 JPA를 활용한 데이터베이스 연동 실습을 목적으로 합니다.

---

## 🛠 Tech Stack
- **Framework**: Spring Boot
- **Build Tool**: Gradle / Maven
- **Language**: Java 25
- **Database**: MariaDB
- **ORM**: Spring Data JPA
- **View Engine**: Thymeleaf (or Mustache)

---

## ⚙️ Environment Setup

### 1. Database (MariaDB)
프로젝트 실행 전, MariaDB에 접속하여 데이터베이스를 생성해야 합니다.

```sql
CREATE DATABASE blog;
