pipeline{
    agent any

    environment{
        PROJECT_NAME="backend"
        PROJECT_VERSION=1
        GIT_CREDENTIALS=credentials('github-username-password')
        GIT_REPO="https://github.com/hanumanflow/backend.git"
        BRANCH="feature/advanced"
    }
    options{
        timestamps()
        buildDiscarder(
            logRotator(
                numToKeepStr: '3',
                artifactNumToKeepStr: '3'
            )
        )
    }

    stages{
        stage("Checkout"){
            steps{
                deleteDir() //to delete previous directory
                checkout([
                    $class: 'GitSCM',
                    branches: [
                        [
                            name: "${env.BRANCH}"
                        ]
                    ],
                    userRemoteConfigs: [
                        [
                            credentialsId: 'github-username-password',
                            url: "${GIT_REPO}"
                        ]
                    ]
                ])

                script{
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short=8 HEAD',
                        returnStdout: true
                    )
                    env.IMAGE_TAG = "${PROJECT_NAME}-${env.GIT_COMMIT_SHORT}"
                    echo "Application commit = ${env.GIT_COMMIT_SHORT}"
                    echo "Image Tag = ${env.IMAGE_TAG}"
                }

                sh """
                    chmod +x ./mvnw
                """
            }
        }
        stage("Test"){
            steps{
                sh """
                    ./mvnw -B -ntp clean test
                """
            }

            post{
                always{
                    junit(testResults: "target/surefire-reports/*.xml" , allowEmptyResults: true)
                }
            }
        }

        stage("Package"){
            steps{
                sh """
                    ./mvnw -ntp package -Dproject.name="${PROJECT_NAME}-${PROJECT_VERSION}"
                """
            }
        }
        stage("Deploy"){
            steps{
                // withEnv(['JENKINS_NODE_COOKIE=donotkill']){
                sh """
                    ls -l target/
                    nohup java -jar -Dserver.port=8081 "target/${PROJECT_NAME}-${PROJECT_VERSION}.jar" &>>backend.log &
                """
                // }
            }
        }

        stage("Integration tests"){
            steps{
                sh """  
                    sleep 20
                    curl  --connect-timeout 20 --max-time 30 http://localhost:8081
                """
            }
        }
    }
}