FROM openjdk:17

#copia meu jar gerado e joga pro container com mesmo nome
COPY target/aws_project01-0.0.1-SNAPSHOT.jar aws_project01-0.0.1-SNAPSHOT.jar

#esse comando é reposavel por subir nosso arquivo jar
ENTRYPOINT [ "java","-jar","aws_project01-0.0.1-SNAPSHOT.jar" ]




#fazer o login com docker login para subir a imagem para dockerhub

#colocar as dp e plugins do maven no projeto 

#gerar os .jar sudo mvn clean package

#executar o comando para gerar a imagem docker build -t dennerol/curso_aws_project01:v1 .

#mandar para o dockerHub docker push dennerol/curso_aws_project01:v1
