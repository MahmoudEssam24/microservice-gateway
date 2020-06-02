# Springboot- Zuul Gateway- Microservice


Minimal [Spring Boot](http://projects.spring.io/spring-boot/) Sample Gateway service.

## Requirements

For building and running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)

## Building the application locally


To build your app and install dependencies, you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

Make sure that the `core` (https://gitlab.com/sample-microservices-project/core) jar is built.

In the core project directory
```shell
"<path to project directory>/core/mvnw" clean -f "<path to project directory>/core/pom.xml"
"<path to project directory>/core/mvnw" install -f "<path to project directory>/core/pom.xml"
```

In gateway directory
```shell
"<path to project directory>/gateway/mvnw" clean -f "<path to project directory>/gateway/pom.xml"
"<path to project directory>/gateway/mvnw" install -f "<path to project directory>/gateway/pom.xml"
```

Note: 
If you have Maven installed globally you can just use:

```shell
"mvn clean"
"mvn install"
```

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the `sa.com.me.gateway` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

```shell
mvn run
```

## Running the application locally using dockers

### Requirements

install docker daemons on your machine, you can find all the steps here: (https://docs.docker.com/docker-for-mac/)

TO start the application using docker, there is a Dockerfile in the root directory of the application. Navigate to root directory and in your terminal run the following:

```shell
"<path to project directory>/gateway/mvnw" clean -f "<path to project directory>/gateway/pom.xml"
"<path to project directory>/gateway/mvnw" install -f "<path to project directory>/gateway/pom.xml"
docker image build -t gateway-service .
```

That will create your image, then you run your container using:

```shell
docker container run  -it --name gateway -p 8763:8763 -d gateway-service java -jar gateway.jar --eureka.client.serviceUrl.defaultZone=http://<Eureka Container Ip>:8761/eureka/
```

To get `Eureka Container Ip` After starting Eureka container, run this command:

```shell
docker inspect <containerNameOrId> | grep '"IPAddress"' | head -n 1
```

`containerName` in our case is `eureka`



