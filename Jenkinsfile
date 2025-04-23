pipeline {
    agent any

    tools {
            maven 'Maven 3.9.9'
            docker 'Docker latest'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', credentialsId: '45e51d01-cc3e-4931-ab52-ed9246fb4236', url: 'https://github.com/EKoregin/BookReactApp.git'
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
                sh 'mvn test'
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