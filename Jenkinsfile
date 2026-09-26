pipeline{
    agent any

    environment{
        PROJECT_NAME="backend"
        PROJECT_VERSION=1
        BUILD_NAME="${PROJECT_NAME}-${PROJECT_VERSION}"
        GIT_CREDENTIALS=credentials('github-username-password')
        GIT_REPO="https://github.com/hanumanflow/backend.git"
        BRANCH="feature/sonarqube"
        MAVEN_DEPENDENCIES="${HOME}/.m2/repository"
        NVD_API_KEY=credentials('NVD_API_KEY')
        TEMP_STOP=true
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
        stage("Test"){
            when{
                expression { env.TEMP_STOP }
            }
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

        //Dependencies scanning

        stage("OWASP dependency scan"){
            when{
                expression { env.TEMP_STOP }
            }
            steps{
            //    sh """
            //         ./mvnw -B -ntp org.owasp:dependency-check-maven:check -DnvdApiKeyEnvironmentVariable=NVD_API_KEY -DfailBuildOnCVSS=7
            //      """
                dependencyCheck (
                    additionalArguments: '--scan ./pom.xml --scan ./target --format XML --format HTML --out ./ ' ,
                    odcInstallation: 'OWASP-depCheck-12',
                    nvdCredentialsId: 'NVD_API_KEY'
                )
                
                dependencyCheckPublisher (
                    pattern: 'dependency-check-report.xml' ,
                    failedTotalCritical: 47,
                    failedTotalHigh: 20,
                    failedTotalMedium: 30
                )
        
            }
        }
        //Sonarquebe 

        
        stage("Deploy"){
            when{
                expression { env.TEMP_STOP }
            }
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
            when{
                expression { env.TEMP_STOP }
            }
            steps{
                sh """  
                    sleep 20
                    curl  --connect-timeout 20 --max-time 30 http://localhost:8081
                """
            }
        }
    }
}