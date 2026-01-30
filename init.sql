-- ============================================================
-- Continue Bank - Docker 초기화 SQL
-- docker-compose.yml의 init.sql로 사용
-- ============================================================

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS trustee_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS entrusting_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS callcenter_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER IF NOT EXISTS 'continue'@'%' IDENTIFIED BY 'continue12!';
GRANT ALL PRIVILEGES ON entrusting_db.* TO 'continue'@'%';
GRANT ALL PRIVILEGES ON trustee_db.* TO 'continue'@'%';
GRANT ALL PRIVILEGES ON callcenter_db.* TO 'continue'@'%';
FLUSH PRIVILEGES;
