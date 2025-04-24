pipeline {
    agent any

    environment {
                SONAR_TOKEN = credentials('sonarqube-token')
                COMPOSE_SERVICES = 'app rabbitmq books_db elasticsearch kibana'
    }

    tools {
            maven 'Maven 3.9.9'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', credentialsId: '64e03b24-12a9-4dd8-941f-12bdcf239f3b', url: 'https://github.com/EKoregin/BookReactApp.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN'
                }
            }
        }

        stage('Test') {
            steps {
                bat 'mvn test -Dtest=!BookItTest'
            }
        }

        stage('Build Docker Image...') {
            steps {
                bat 'docker build -t bookreactapp:latest .'
            }
        }

        stage('Deploy BookReactApp...') {
            steps {
                bat 'docker-compose down || exit 0'
                bat 'timeout /t 5'
                // Проверяем, что все контейнеры удалены
                script {
                    def maxAttempts = 10
                    def attempt = 0
                    def containersExist = true
                    while (containersExist && attempt < maxAttempts) {
                        containersExist = false
                        for (service in env.COMPOSE_SERVICES.split()) {
                            def result = bat(script: "docker ps -a -q --filter \"name=${service}\"", returnStatus: true)
                            if (result == 0) {
                                containersExist = true
                                echo "Container ${service} still exists, waiting..."
                            }
                        }
                        if (containersExist) {
                            bat 'timeout /t 2 /nobreak'
                            attempt++
                        }
                    }
                    if (containersExist) {
                        error "Failed to remove containers after ${maxAttempts} attempts"
                    }
                    echo 'All containers removed, proceeding with deployment...'
                }
                // Запускаем новые контейнеры
                bat 'docker-compose up -d --build'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
