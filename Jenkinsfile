pipeline {
    agent { docker 'maven:latest' }
    stages {
        stage('build') {
            steps {
                sh 'mvn --version'
            }
        }
    }
}
