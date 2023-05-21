provider "aws" {
    region = "eu-north-1"
}

resource "aws_instance" "ec2_ins" {
    ami           = "ami-0874ff0d73a3ab8cf"
    instance_type = "t3.micro"
    
    tags = {
        "Name" = "Creativ_hub_assignment_ec2"
        "instance_type" = "t2.micro"
    }
}


