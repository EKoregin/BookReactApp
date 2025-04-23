pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/EKoregin/BookReactApp.git'
            }
        }
        stage('Build') {
            steps {
                echo 'Сборка проекта...'
                sh 'mvn clean install'
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