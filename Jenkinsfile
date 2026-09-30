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
    steps {
        sh '''
            echo "Starting Spring Boot application on port 8081..."

            nohup mvn spring-boot:run \
                -Dspring-boot.run.arguments="--server.port=8081" \
                > app.log 2>&1 &

            echo $! > app.pid

            echo "Waiting for application to start..."

            for i in {1..30}; do
                if curl -s http://localhost:8081/ > /dev/null; then
                    echo "Application is ready!"
                    break
                fi

                sleep 2
            done

            echo "Running Selenium tests..."

            mvn test -DbaseUrl=http://localhost:8081
        '''
    }

    post {
        always {
            echo "Stopping Spring Boot application..."

            sh '''
                if [ -f app.pid ]; then
                    kill $(cat app.pid) || true
                fi

                echo "===== Spring Boot Log ====="
                cat app.log || true
            '''
        }
    }
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
