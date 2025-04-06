pipeline{
    agent any
    stages{

        // stage("gradle build"){
        //     steps{
        //         echo "========executing gradle build========"
        //         sh './gradlew build'
        //         sh './gradlew bootJar'
        //     }
        // }

        stage("docker build"){
            steps {
                echo "========executing docker build========"
                def image = docker.build("192.168.31.33:5000/lunarapi")
                image.push('latest')
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