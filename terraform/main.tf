provider "aws" {
    region = "eu-north-1"
}

resource "aws_instance" "ec2_ins" {
    ami           = "ami-0874ff0d73a3ab8cf"
    instance_type = "t3.micro"
    key_name      = aws_key_pair.mykey.key_name
    associate_public_ip_address = false

    depends_on = [aws_eip.example]
    
    tags = {
        Name = var.Name
        "instance_type" = "t3.micro"
    }
}

resource "aws_key_pair" "mykey" {
  key_name   = "mykey"
  public_key = file("/var/jenkins_home/workspace/Site_deployment/rsa/mykey.pub")
}

variable "Name" {
  description = "creative_hub_assingment_ec2"
  type        = string
  default     = "creative_hub_assingment_ec2"
}

resource "aws_eip" "eip_ec2" {
  vpc                = true
  instance           = aws_instance.ec2_ins.id
  associate_with_private_ip = "13.50.23.144"
}

