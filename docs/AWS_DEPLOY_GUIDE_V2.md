# 🚀 AWS 클라우드 통합 배포 마스터 가이드 (Perfect Edition)

이 문서는 위탁사(Entrusting)와 수탁사(Trustee) 시스템의 AWS 인프라 구축부터 앱 배포, k3s 클러스터 구성 및 최종 연동까지의 **모든 단계**를 집대성한 끝판왕 가이드입니다.

---

## 🏛️ 전체 시스템 아키텍처
- **위탁사(Entrusting)**: 단일 Ubuntu EC2 (Nginx + Spring Boot) + AWS RDS (MySQL)
- **수탁사(Trustee)**: 하이브리드 k3s 클러스터 (보안 취약점 진단 타겟)
  - **Master**: Ubuntu EC2
  - **Worker (WAS)**: **Windows Server 2022** (컨테이너 기반 - 취약진단 대상!)
  - **Worker (Web)**: Ubuntu EC2 (Nginx)
  - **Database**: Ubuntu EC2 (MySQL 직접 설치)

---

## [단계 0] 공통 사전 준비
모든 작업 전, 로컬 터미널에서 환경 변수를 설정합니다.
```bash
export REGION="ap-northeast-2"
export DB_PASSWORD='continue12!'
export VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query "Vpcs[0].VpcId" --output text)
```

---

## [단계 1] 위탁사(Entrusting) 인프라 구축

### 1-1. 키 페어 및 보안 그룹 생성 (로컬 터미널)
```bash
# 1. 키 페어 생성 (다운로드 폴더에 보관 권장)
aws ec2 create-key-pair --key-name entrusting-key-v2 --query "KeyMaterial" --output text > entrusting-key-v2.pem

# 2. 보안 그룹 생성
aws ec2 create-security-group --group-name entrusting-web-sg-v2 --description "SG for Entrusting" --vpc-id $VPC_ID
export WEB_SG=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=entrusting-web-sg-v2" --query "SecurityGroups[0].GroupId" --output text)

# 3. 규칙 설정 (SSH, HTTP, 백엔드, RDP)
aws ec2 authorize-security-group-ingress --group-id $WEB_SG --protocol tcp --port 22 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $WEB_SG --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $WEB_SG --protocol tcp --port 8085 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $WEB_SG --protocol tcp --port 3389 --cidr 0.0.0.0/0 # 윈도우용
```

### 1-2. 위탁사 리소스 생성 (로컬 터미널)
```bash
# Ubuntu EC2 생성
aws ec2 run-instances --image-id ami-08a4fd517a4872931 --count 1 --instance-type t3.small --key-name entrusting-key-v2 --security-group-ids $WEB_SG --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=Entrusting-Ubuntu}]"

# RDS MySQL 생성
aws rds create-db-instance --db-instance-identifier entrusting-db-v2 --db-instance-class db.t3.micro --engine mysql --master-username admin --master-user-password $DB_PASSWORD --allocated-storage 20 --db-name entrusting_db --publicly-accessible
```

---

## [단계 2] 위탁사 앱 배포 (Current Work)

### 2-1. 서버 접속 및 환경 구성 (EC2 터미널)
```bash
sudo apt update && sudo apt install -y openjdk-17-jdk nginx git maven
git clone https://github.com/11mia1717/homepage-project.git
cd homepage-project
```

### 2-2. 프론트엔드 빌드 및 Nginx 설정
```bash
# 1. 빌드
cd entrusting-client/frontend
npm install && npm run build

# 2. Nginx 배포
sudo rm -rf /var/www/html/*
sudo cp -r dist/* /var/www/html/
sudo bash -c 'cat > /etc/nginx/sites-available/default <<EOF
server {
    listen 80;
    server_name _;
    root /var/www/html;
    index index.html;
    location / { try_files \$uri \$uri/ /index.html; }
}
EOF'
sudo systemctl restart nginx
```

### 2-3. 백엔드 빌드 및 (.env 기반) 가동
```bash
cd ~/homepage-project/entrusting-client/backend
# 1. 환경 설정 파일 생성
cat > .env <<EOF
DB_HOST=<RDS-엔드포인트-주소>
DB_PORT=3306
DB_NAME=entrusting_db
DB_USER=admin
DB_PASSWORD=continue12!
EOF

# 2. 빌드 및 실행
mvn clean package -DskipTests
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=8085 > ~/backend.log 2>&1 &
```

---

## [단계 3] 수탁사(Trustee) DB 및 k3s 구축

### 3-1. 수탁사 전용 DB 서버 (EC2 터미널)
```bash
# MySQL 설치 및 외부 접속 허용
sudo apt update && sudo apt install -y mysql-server
sudo sed -i "s/127.0.0.1/0.0.0.0/" /etc/mysql/mysql.conf.d/mysqld.cnf
sudo systemctl restart mysql

# DB 초기화 및 샘플 데이터 10건 삽입
sudo mysql -e "CREATE DATABASE trustee_db; CREATE USER 'admin'@'%' IDENTIFIED BY 'continue12\!'; GRANT ALL PRIVILEGES ON trustee_db.* TO 'admin'@'%'; FLUSH PRIVILEGES;"
sudo mysql trustee_db -e "CREATE TABLE carrier_users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50), phone VARCHAR(20), carrier VARCHAR(20));
INSERT INTO carrier_users (name, phone, carrier) VALUES ('김중수','010-9511-9924','SKT'),('방수진','010-8717-6882','KT'),('김은수','010-5133-7437','LG U+'),('이광진','010-3065-9593','ALDDLE'),('임혜진','010-3731-5819','SKT'),('전용준','010-5047-0664','KT'),('김유진','010-9287-7379','LG U+'),('장민아','010-4932-8977','SKT'),('이승원','010-9212-8221','KT'),('홍길동','010-0000-0000','SKT');"
```

### 3-2. k3s 마스터 노드 (Ubuntu)
```bash
# EC2 접속 후 k3s 설치
curl -sfL https://get.k3s.io | sh -
# 토큰 확인 (윈도우 워커 조인용)
sudo cat /var/lib/rancher/k3s/server/node-token
K1086dd191541490c9b78d32b5fa1798e7ec6bcd9c01d1ce7619c794fe929c07fa7::server:c790c8069339f48361b3478b36dcb2cd(지)

```

    # 1. k3s 윈도우 실행 파일 다운로드 (보안 연결 설정 포함)
    # [인터넷 연결/TLS 오류 방지용]
### 3-3. k3s 윈도우 워커 노드 (Windows Server 2022)
> [!TIP]
> 기술 컨설팅 및 취약점 진단을 위해 Windows 환경을 유지합니다. 윈도우에서 직접 파일을 못 받는 경우 **"로컬에서 다운로드 후 바탕화면에 복사/붙여넣기"** 하세요! ㅠ

1.  **k3s.exe 파일 준비 (가장 확실한 방법! 🚀)**
    - [이 주소](https://github.com/k3s-io/k3s/releases/download/v1.31.5%2Bk3s1/k3s-windows-amd64.exe)를 **사용자님 로컬 PC**에서 클릭해 다운로드하세요.
    - 다운로드 된 `k3s-windows-amd64.exe` 파일을 원격 데스크톱(RDP) 창을 통해 **윈도우 서버 바탕화면**으로 복사(Ctrl+C) 후 붙여넣기(Ctrl+V) 하세요.
    - 파일 이름을 **`k3s.exe`**로 바꿉니다.

2.  **컨테이너 활성화 및 k3s 조인 (PowerShell 관리자)**
    ```powershell
    # 1. 컨테이너 기능 활성화 (이미 하셨다면 패스)
    Install-WindowsFeature -Name Containers; Restart-Computer -Force

    # 2. (재부팅 후) k3s 조인 실행
    $MASTER_URL = "https://172.31.33.220:6443"
    $TOKEN = "K1086dd191541490c9b78d32b5fa1798e7ec6bcd9c01d1ce7619c794fe929c07fa7::server:c790c8069339f48361b3478b36dcb2cd"
    
    .\k3s.exe agent --server $MASTER_URL --token $TOKEN --node-name "trustee-win-worker"
    ```
    *💡 성공하면 마스터(Ubuntu)에서 `kubectl get nodes`에 나타납니다!*
    ```

---

## [단계 4] 최종 컨테이너 배포 및 E2E 테스트
1.  **Trustee Web/WAS 이미지 빌드 및 Push**
2.  **k3s Manifest (`kubectl apply`) 배포**
3.  **위탁사 -> 수탁사 API 호출 테스트**

---
**Guide Version: v-Comprehensive Master** ㅠ 🚀
모든 과정은 이 문서 하나로 정리가 끝납니다! ㅠ 막히는 부분은 언제든 물어봐 주세요! ㅠ 🚀
