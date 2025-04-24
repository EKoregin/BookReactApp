pipeline {
    agent none

    environment {
            SONAR_TOKEN = credentials('sonarqube-token')
    }

    tools {
            maven 'Maven 3.9.9'
    }

    stages {
        stage('Checkout') {
            agent any
            steps {
                git branch: 'main', credentialsId: '45e51d01-cc3e-4931-ab52-ed9246fb4236', url: 'https://github.com/EKoregin/BookReactApp.git'
            }
        }

        stage('SonarQube Analysis') {
              agent any
              steps {
                  withSonarQubeEnv('SonarQube') {
                      sh 'mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN'
                  }
              }
        }

        stage('Build') {
            agent any
            steps {
                echo 'Сборка проекта...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            agent any
            steps {
                echo 'Запуск тестов...'
                sh 'mvn test -Dtest=!BookItTest'
            }
        }

        stage('Build Docker Image...') {
             agent { label 'windows-agent' }
             steps {
                 dir('c:/projects/study_projects/reactive/BookReactApp') {
                     bat 'docker build -t bookreactapp:latest .'
                 }
             }
        }

        stage('Deploy BookReactApp...') {
             agent { label 'windows-agent' }
             steps {
                 dir('c:/projects/study_projects/reactive/BookReactApp') {
                      bat 'docker-compose down || exit 0'
                      bat 'docker-compose up -d --build'
                 }
             }
        }
    }

    post {
            always {
                junit '**/target/surefire-reports/*.xml'
            }
            success {
                echo 'Сборка и тесты прошли успешно!'
            }
            failure {
                echo 'Произошла ошибка при сборке или тестировании.'
            }
        }
}