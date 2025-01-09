# image-recognizition

# car-recognizition

This repository contains the application for detecting cars in images using AWS Rekognition. The application is implemented in Java and utilizes the Maven build tool to manage dependencies.

## Features

- **Image Processing**: Detects cars in images using AWS Rekognition.
- **Queue Integration**: Identifies images containing cars and places them into an AWS SQS queue for further processing.
- **Cloud Integration**: Designed to run on an AWS EC2 instance.

## Prerequisites

- Java Development Kit (JDK)
- Maven
- AWS Account with AWS Rekognition and SQS enabled
- EC2 Instance for deployment

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd car-recognizition
   ```
2. Build the project:
   ```bash
   mvn clean package
   ```

## Deployment

1. **Create an EC2 Instance**:
   - Go to the AWS Management Console and create an EC2 instance (referred to as EC-A).
   - Use a key pair for secure access.

2. **Install Java**:
   ```bash
   sudo yum clean metadata
   sudo yum install -y java-1.8.0-amazon-corretto
   ```

3. **Upload the JAR File**:
   ```bash
   scp -i "ec2-cert.pem" car-recognizition-1.0-SNAPSHOT-jar-with-dependencies.jar ec2-user@<EC-A-Public-DNS>:
   ```

4. **Run the Application**:
   ```bash
   java -jar car-recognizition-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

---

# text-recognizition

This repository contains the application for detecting text in images using AWS Rekognition. The application is implemented in Java and utilizes the Maven build tool to manage dependencies.

## Features

- **Text Extraction**: Consumes images from an AWS SQS queue and detects text in them using AWS Rekognition.
- **Cloud Integration**: Designed to run on an AWS EC2 instance with EBS storage.
- **Output Storage**: Stores images containing both cars and text in the EBS-mounted directory.

## Prerequisites

- Java Development Kit (JDK)
- Maven
- AWS Account with AWS Rekognition, SQS, and EBS enabled
- EC2 Instance for deployment with mounted EBS storage

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd text-recognizition
   ```
2. Build the project:
   ```bash
   mvn clean package
   ```

## Deployment

1. **Create an EC2 Instance**:
   - Go to the AWS Management Console and create an EC2 instance (referred to as EC-B).
   - Use a key pair for secure access.

2. **Mount EBS Storage**:
   - During instance creation, add EBS storage.
   - Mount the EBS volume:
     ```bash
     lsblk
     sudo file -s /dev/xvdb
     sudo mkfs -t xfs /dev/xvdb
     sudo mkdir /ebs-data
     sudo mount /dev/xvdb /ebs-data
     ```

3. **Install Java**:
   ```bash
   sudo yum clean metadata
   sudo yum install -y java-1.8.0-amazon-corretto
   ```

4. **Upload the JAR File**:
   ```bash
   scp -i "ec2-cert.pem" text-recognizition-1.0-SNAPSHOT-jar-with-dependencies.jar ec2-user@<EC-B-Public-DNS>:
   ```

5. **Run the Application**:
   ```bash
   java -jar text-recognizition-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

## Workflow

1. **Car Recognition**:
   - The `car-recognizition` application runs on EC-A.
   - Detects images containing cars and places their references in an AWS SQS queue.

2. **Text Recognition**:
   - The `text-recognizition` application runs on EC-B.
   - Consumes car image references from the SQS queue and detects text in them.
   - Stores images that contain both cars and text in the EBS-mounted directory (`/ebs-data/image-recognition.output.txt`).

## Output

- The output is written to the EBS-mounted directory (`/ebs-data/image-recognition.output.txt`).
