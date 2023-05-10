def site_deployment_url = "git@github.com:AvishkaSooriyapperuma/Portfolio.git";
def inframaintainance_repo = "git@github.com:AvishkaSooriyapperuma/EC2_creation_tf.git";

pipeline {
  agent any

  // using the Timestamper plugin we can add timestamps to the console log
  options {
    timestamps()
  }

  environment {
    //Use Pipeline Utility Steps plugin to read information from pom.xml into env variables
    id_rsa_key = "/home/ec2-user/.ssh/idrsa.pub"
  }
  parameters {
    string(name: 'pipeline_branch', defaultValue: 'main', description: 'github repo branch');
  }

  stages {
    stage('Checkout source repo') {
        // main repo will be checkedout here and from it we take the tf
            //git credentialsId: 'github', url:"https://github.com/AvishkaSooriyapperuma/EC2_creation_tf.git"
          steps{
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: 'main']],
                    userRemoteConfigs: [[
                        url: 'git@github.com:AvishkaSooriyapperuma/EC2_creation_tf.git',
                        credentialsId: 'github'
                    ]]
                ])
          }
          }
  }

//   post {
//     failure {
//       // notify users when the Pipeline fails
//       mail to: 'team@example.com',
//           subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
//           body: "Something is wrong with ${env.BUILD_URL}"
//     }
//   }
}