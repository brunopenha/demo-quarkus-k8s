# demo-quarkus-k8s

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

This project also is a demonstration on how to update the configuration here from config map in Kubernetes.

Initially we will create a project by following this page: https://quarkus.io/guides/deploying-to-kubernetes


Monstly we use this page as referece - https://quarkus.io/guides/config-reference

## Extentions used here

1 - Yaml config - allow us to get values from config map

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-config-yaml</artifactId>
</dependency>
```

1 - Minikube - to generate minikube YAML file

```xml
    <dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-minikube</artifactId>
</dependency>
```

## Creating minikube image

1 - Clean up - remove old images to save local space

```shell
docker rm -f $(docker ps | grep $(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout)  | awk '{print $1}' ) || echo not working && \
docker rmi -f $(docker images | grep $(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout)  | awk '{print $3}' );
```

2 - Create the package:

```shell script
mvn clean package -Dquarkus.container-image.build=true
```

3 - Tag the created image

```shell
docker tag $(docker images | grep $(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout) | awk '{print $3}' ) brunopenha/\
$(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout):\
$(docker images | grep $(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout) | awk '{print $2}')
```

4 - Push the images

```shell
docker push brunopenha/$(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout):\
$(docker images | grep $(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout) | awk '{print $2}')
```

5 - Apply configuration

```shell
kubectl apply -f src/main/k8s-files/config_map.yml
```

6 - Check the service

```shell
curl $(minikube service demo-quarkus-k8s --url)/hello
```

7 - Edit configuration

```shell
kubectl edit cm demo-quarkus-k8s
```

8 - Restart the pod to apply it

```shell
kubectl delete pod $(kubectl get pods | grep $(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout) | awk '{print $1}')
```

9 - Check the service again

```shell
curl $(minikube service $(mvn help:evaluate -Dexpression=project.version -Dexpression=project.name  -q -DforceStdout) --url)/hello
```



## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/demo-quarkus-k8s-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Related Guides

- Minikube ([guide](https://quarkus.io/guides/kubernetes)): Generate Minikube resources from annotations
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, Jakarta Persistence)

## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

