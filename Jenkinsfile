pipeline{
    agent any
    stages{

        stage("gradle build"){
            steps{
                echo "========executing gradle build========"
                sh './gradlew build'
                sh './gradlew bootJar'
            }
        }

        stage("docker build"){
            steps {
                echo "========executing docker build========"
                sh 'docker build -t 192.168.31.33:5000/lunarapi:latest .'
            }
        }

        stage("public docker image"){
            steps {
                echo "========executing public docker image========"
                sh 'docker push 192.168.31.33:5000/lunarapi:latest'
            }
        }
    }
    post{
        always{
            echo "========always========"
        }
        success{
            echo "========pipeline executed successfully ========"
        }
        failure{
            echo "========pipeline execution failed========"
        }
    }
}