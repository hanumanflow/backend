pipeline{
    agent any

    environment{
        PROJECT_NAME="backend"
        PROJECT_VERSION=1
    }
    options{
        timestamps()
        buildDiscarder(
            logRotator(
                numToKeepStr: '3'
                artifactNumToKeepStr: '3'
            )
        )
    }

    stages{
        stage("Checkout"){
            steps{
                checkout scm
                sh """
                    chmod +x mvnw
                """
            }
        }
        stage("Test"){
            steps{
                sh """
                    ./mvnw -B -ntp clean test
                """
            }
        }

        stage("Package"){
            steps{
                sh """
                        ./mvnw -ntp package -Dname="${PROJECT_NAME}-${PROJECT_VERSION}"
                """
            }
        }
        stage("Deploy"){
            steps{
                sh """
                    ls -l target/
                    nohup java -jar -Dserver.port=8081 "target/${PROJECT_NAME}-${PROJECT_VERSION}.jar" &>>backend.log &

                """
            }
        }

        stage("Integration tests"){
            steps{
                sh """
                        curl http://localhost:8081
                """
            }
        }
        
    }

}