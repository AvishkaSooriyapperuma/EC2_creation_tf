provider "aws" {
    region = "eu-north-1"
}

resource "aws_instance" "ec2_ins" {
    ami = "ami-0a6351192ce04d50c"
    instance_type = "t2.micro"
    tags = {
        "Name" = "Creativ_hub_assignment_ec2"
        "instance_type" = "t2.micro"
    }
}


