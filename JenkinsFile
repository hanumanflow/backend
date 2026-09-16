pipeline{
    agent any

    environment{
        PROJECT_NAME="backend"
        PROJECT_VERSION=1
    }

    stages{
        stage("Checkout"){
            steps{
                checkout scm
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
                        ./mvnw package -Dname="${PROJECT_NAME}-${PROJECT_VERSION}"
                """
            }
        }
        stage("Deploy"){
            steps{
                sh """
                    JENKINS_NODE_COOKIE=dontKillMe
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