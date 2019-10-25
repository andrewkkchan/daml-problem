pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn -Dmaven.test.skip=true install'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Deploy') {
            steps {
                sh 'whoami'
                sh 'env'
                script {
                    pid = sh(script: 'lsof -t -i :$CONTRACT_SERVICE_PORT || true', returnStdout: true)
                    if (pid) {
                        sh "kill -9 ${pid}"
                    }
                }
                sh 'JENKINS_NODE_COOKIE=dontKillMe nohup mvn spring-boot:run > output.log &'
            }
        }
    }
}
