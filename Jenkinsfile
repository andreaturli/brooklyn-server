pipeline {
    agent { docker 'maven:latest' }      
    def pom = readMavenPom file: 'pom.xml'
    def version = pom.version.replace("-SNAPSHOT", ".${currentBuild.number}")
    stages {
        stage('build') {
            steps {
                sh "-DreleaseVersion=${version} -DdevelopmentVersion=${pom.version} -DpushChanges=false -DlocalCheckout=true -DpreparationGoals=initialize release:prepare release:perform -B"
            }
        }
    }
}
