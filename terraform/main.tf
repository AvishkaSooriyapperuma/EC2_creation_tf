provider "aws" {
    region = "eu-north-1"
}

resource "aws_instance" "ec2_ins" {
    ami           = "ami-0874ff0d73a3ab8cf"
    instance_type = "t3.micro"
    key_name      = aws_key_pair.mykey.key_name
    
    tags = {
        Name = var.Name
        "instance_type" = "t2.micro"
    }
}

resource "aws_key_pair" "mykey" {
  key_name   = "mykey"
  public_key = file("/var/jenkins_home/workspace/Site_deployment/rsa/mykey.pem")
}

variable "Name" {
  description = "creative_hub_assingment_ec2"
  type        = string
  default     = "creative_hub_assingment_ec2"
}
