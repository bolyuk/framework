pipeline {
    agent any
    tools {
        maven 'Default'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Deploy') {
            when {
                branch 'master'
            }
            steps {
                configFileProvider([configFile(fileId: '413c037e-55bf-4e38-96e3-0428513e6856', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS clean deploy'
                }
            }
        }
    }
}
