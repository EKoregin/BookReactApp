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
                git branch: 'main', credentialsId: '45e51d01-cc3e-4931-ab52-ed9246fb4236', url: 'https://github.com/EKoregin/BookReactApp.git'
            }
        }

        stage('SonarQube Analysis') {
              steps {
                  withSonarQubeEnv('SonarQube') {
                      sh 'mvn sonar:sonar -Dsonar.login=$SONAR_TOKEN'
                  }
              }
        }

        stage('Build') {
            steps {
                echo 'Сборка проекта...'
                sh 'mvn clean compile'
            }
        }
        stage('Test') {
            steps {
                echo 'Запуск тестов...'
                sh 'mvn test -Dtest=!BookItTest'
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