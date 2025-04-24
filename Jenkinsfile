pipeline {
    agent any

    environment {
                SONAR_TOKEN = credentials('sonarqube-token')
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

        stage('SonarQube Analysis') {
              steps {
                  withSonarQubeEnv('SonarQube') {
                      bat 'mvn sonar:sonar -Dsonar.token=$SONAR_TOKEN'
                  }
              }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
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