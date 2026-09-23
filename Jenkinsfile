pipeline{
    agent any

    environment{
        PROJECT_NAME="backend"
        PROJECT_VERSION=1
        BUILD_NAME="${PROJECT_NAME}-${PROJECT_VERSION}"
        GIT_CREDENTIALS=credentials('github-username-password')
        GIT_REPO="https://github.com/hanumanflow/backend.git"
        BRANCH="feature/advanced"
        MAVEN_DEPENDENCIES="${HOME}/.m2/repository"
        NVD_API_KEY=credentials('NVD_API_KEY')
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
                echo "------------------Checking out repo -----------------"
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

                // checkout scm

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

        stage("verify"){
            steps{
                sh """
                    ./mvnw clean verify -DskipTests
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
                    junit(testResults: "target/surefire-reports/*.xml" , allowEmptyResults: true )
                }
            }
        }

        //Dependencies scanning

        stage("OWASP dependency scan"){
            steps{
            //    sh """
            //         ./mvnw -B -ntp org.owasp:dependency-check-maven:check -DnvdApiKeyEnvironmentVariable=NVD_API_KEY -DfailBuildOnCVSS=7
            //      """
                sh """
                    pwd
                    ls -l
                    ls -l target/
                """
                dependencyCheck (
                    additionalArguments: '''
                                            --scan ./pom.xml 
                                            --scan ./target/*.jar
                                            --format XML
                                            --format HTML
                                        
                                            --out ./
                                            ''' ,
                    odcInstallation: 'OWASP-depCheck-12',
                    nvdCredentialsId: 'NVD_API_KEY'
                )
            }
            // post{
            //     always{
            //         archiveArtifacts "target/dependency-check-report.html"
            //     }
            // }
        }
        //Sonarquebe 

        stage("Package"){
            steps{
                sh """
                    ./mvnw -ntp package  -Dmaven.repo.local=${MAVEN_DEPENDENCIES} -DskipTests -Dproject.name="${BUILD_NAME}"
                """
            }
            post{
                success{
                    echo "----------- Packaging is success -----------"
                    archiveArtifacts(artifacts: "target/${BUILD_NAME}.jar" ,
                                     fingerprint: true)
                }
            }
        }
        stage("Deploy"){
            steps{
                // withEnv(['JENKINS_NODE_COOKIE=donotkill']){
                sh """
                    ls -l target/
                    nohup java -jar -Dserver.port=8081 "target/${BUILD_NAME}.jar" &>>backend.log &
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