//def site_deployment_url = "git@github.com:AvishkaSooriyapperuma/Portfolio.git";
//def inframaintainance_repo = "git@github.com:AvishkaSooriyapperuma/EC2_creation_tf.git";
SSH_KEY_FILE = "/var/jenkins_home/workspace/Site_deployment/rsa/mykey.pub"


pipeline {
  agent any

  // using the Timestamper plugin we can add timestamps to the console log
  options {
    timestamps()
  }

  environment {
    //Use Pipeline Utility Steps plugin to read information from pom.xml into env variables
    PATH="${PATH}:/usr/bin/terraform"
    rsa_key="/home/avishka/jenkins_home/.ssh/mykey.pub:/var/jenkins_home/.ssh"
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

    stage('install terraform'){
      steps{
        script{
                sh 'which ansible'
                sh "terraform version"
        }
      }
    } 

    stage('initialize and plan terraform') {
        steps{
          script{
            // sh script '''
            // pwd;
            // cd ${work_space}/terraform/;
            // terraform init
            // terraform plan -out tfplan
            // terraform show -no-color tfplan > tfplan.txt
            // '''
            sh 'pwd;cd /var/jenkins_home/workspace/Site_deployment/terraform/;terraform init'
            sh "pwd;cd /var/jenkins_home/workspace/Site_deployment/terraform/;terraform plan -out tfplan"  
            sh "pwd;cd /var/jenkins_home/workspace/Site_deployment/terraform/;terraform show -no-color tfplan > tfplan.txt"                
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
        script{
          sh "pwd;cd terraform/; terraform apply -input=false tfplan"
          sleep time: 60, unit: 'SECONDS'
        }
      }     
    }

    stage('Sleep') {
      when {expression { params.sleep == 'yes' }}
        steps {
            input message: 'Press OK to continue', ok: 'OK'
            echo 'Continuing with the pipeline execution'
        }
    }

    stage('Setup Nginx') {
      steps {
      withCredentials([sshUserPrivateKey(credentialsId: 'ansible', keyFileVariable: 'ansible', passphraseVariable: '', usernameVariable: 'ec2-user')]) {
      sh "pwd;cd /var/jenkins_home/workspace/Site_deployment/ansible/;ansible-playbook -i inventory setup_nginx.yml --private-key=${SSH_KEY_FILE} --user=ec2-user"
    }
  }
}


      // stage('Setup Nginx'){
      //   steps{
      //     script{

      //       sh "pwd;cd /var/jenkins_home/workspace/Site_deployment/ansible/;ansible-playbook setup_nginx.yml -i inventory --private-key='/var/jenkins_home/workspace/Site_deployment/rsa/mykey.pub'"
            
      //     }
      //   }
      // }




  }

  post {
    always{
      sh "pwd;cd terraform/; terraform destroy -auto-approve -var 'Name=creative_hub_assingment_ec2'"
    }
    success{
      echo 'Plan executed sucessfully.'
      input message: 'Press OK to continue', ok: 'OK'
      sh "pwd;cd terraform/; terraform destroy -auto-approve -var 'Name=creative_hub_assingment_ec2'"
    }
  }
}
