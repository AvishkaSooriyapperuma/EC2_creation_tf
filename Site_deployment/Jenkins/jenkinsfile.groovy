def site_deployment_url = "git@github.com:AvishkaSooriyapperuma/Portfolio.git";
def inframaintainance_repo = "git@github.com:AvishkaSooriyapperuma/EC2_creation_tf.git";
def terraform_dir = "Site_deployment/terraform"

pipeline {
  agent any

  // using the Timestamper plugin we can add timestamps to the console log
  options {
    timestamps()
  }

  environment {
    //Use Pipeline Utility Steps plugin to read information from pom.xml into env variables
    AWS_ACCESS_KEY_ID = credentials('AWS_ACCESS_KEY_ID')
    AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
  }
  parameters {
    string(name: 'pipeline_branch', defaultValue: 'main', description: 'github repo branch');
    booleanParam(name: 'autoApprove', defaultValue: false, description: 'Automatically runs the tf apply after generating the plan');
    choice(name: 'sleep', choices: ['yes', 'no'], description: 'Sleep?:')
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
                        url: 'https://github.com/AvishkaSooriyapperuma/EC2_creation_tf.git',
                        credentialsId: 'github_pat'
                    ]]
                ])
          }     
          }

    stage('initialize and plan terraform') {
        steps{
          script{
            sh "pwd;cd terraform;terraform init"
            sh "pwd;cd terraform;terraform plan -out tfplan"  
            sh "pwd;cd terraform; terraform show -no-color tfplan > tfplan.txt"                
          }
        }
    }

    stage('Terraform approve before apply'){
      when {
        not {
          equals expected: true, actual: params.autoApprove
        }
      }
      steps{
        script{
          def plan = readFile '${terraform_dir}/tfplan.txt'
          input message: "Do you want to apply the plan?",
          parameters: [text(name: 'Plan', description: 'Please review the plan', defaultValue:plan)]
        }
      }
    }

    stage('Terraform apply'){
      steps{
        sh "pwd;cd terraform; terraform apply -input=false tfplan"
      }
    }

    stage('Sleep') {
      when {expression { params.sleep == 'yes' }}
        steps {
            input message: 'Press OK to continue', ok: 'OK'
            echo 'Continuing with the pipeline execution'
        }
    }

    stage('Terraform destroy'){
      steps{
        sh "pwd;cd terraform; terraform destroy -input=false tfplan"
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