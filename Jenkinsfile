pipeline {
    agent any

    parameters {
        choice(name: 'ENV', choices: ['staging', 'production'], description: 'Deploy target')
    }

    environment {
        IMAGE_NAME = "yashlokare/tree-plantation-portal"
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    tools {
        maven 'Maven3'
        jdk 'JDK-21'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/YASH-5055/tree-plantation-portal.git'
            }
        }

        stage('Build') {
            steps { sh 'mvn clean compile' }
        }

        stage('Test') {
            steps { sh 'mvn test' }
        }

        stage('Publish Test Report') {
            steps { junit 'target/surefire-reports/*.xml' }
        }

        stage('Package') {
            steps { sh 'mvn package -DskipTests' }
        }

        stage('Archive Artifact') {
            steps { archiveArtifacts artifacts: 'target/*.war' }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DUSER', passwordVariable: 'DPASS')]) {
                    sh "echo \$DPASS | docker login -u \$DUSER --password-stdin"
                    sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker push ${IMAGE_NAME}:latest"
                }
            }
        }

        stage('Deploy Fresh Container') {
            steps {
                sh """
                docker rm -f tpp-container || true
                docker run -d --name tpp-container -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                echo "Deployed to ${params.ENV}"
                """
            }
        }
    }
}
