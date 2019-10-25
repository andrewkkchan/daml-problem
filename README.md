#Contract-Service

Contract service is the micro-service which create and exercise DA contracts. Orchestration is currently also done in this service, which in larger scale work, shall be moved out.
Steps to run the app:
- run against mysql or maria-db the script at {{base_path}}/database/schema_and_data.sql
- plugin the  environmental variables, e.g., AUTH_DOMAIN=http://localhost:3001;CONTRACT_SERVICE_PORT=3011;CONTRACT_DATABASE_URL=jdbc:mariadb://localhost:3306/contract;CONTRACT_DATABASE_USER=sample_user;CONTRACT_DATABASE_PASSWORD=19283746
- just run the spring boot app
