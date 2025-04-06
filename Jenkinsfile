pipeline{
    agent{
        label "any"
    }
    stages{
        stage("checkout"){
            steps{
                echo "========executing checkout========"
                checkout svm
            }
        }

        stage("gradle build"){
            steps{
                echo "========executing gradle build========"
                sh './gradlew build'
                sh './gradlew bootJar'
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