pipeline{
    agent any

    tools{
        jdk'java-11'
        maven'maven'
    
    }
    
    stages{
        stage{'Git checkout'}{
            steps{
                git branch: 'main', url 'https://github.com/ManojKRISHNAPPA/test-1.git'
            }
        }

        stage('compile'){
            steps{
                sh"mvn compile"
            }
        
        }

        stage('Build'){
            steps{
                sh"mvn clean install"
            }
        
        }

        stage('codescan'){
            steps{
                withCredentials([string(credentialsId: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')]) {
                    sh "mvn sonar:sonar -Dsonar. login=$SONAR_AUTH_TOKEN -Dsonar.host.url=${SONAR_URL}"
            }
        
        }

        stage('Build and tag'){
            steps{
                sh"docker build -t manojkrishnappa/webapp:1 ."
            }
        }

        stage('Docker image scan') {
            steps {
                sh "trivy image --format table -o trivy-image-report.html manojkrishnappa/webapp:1"
         
            }    
        }

        stage('Containersation'){
            steps{
            sh'''
                docker stop c1
                docker rm c1
                docker run -it -d --name c1 -p 9001:8080 manojkrishnappa/webapp:1
            '''
            }
        
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin"
                    }
                }
            }
        }

        stage('Pushing image to repository') {
            steps {
                sh 'docker push manojkrishnappa/webapp:1'
            }
        }
    }
}   
}