# demo-quarkus-k8s

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

This project also demonstrates how to update the configuration here from the config map in Kubernetes.

Initially, we will create a project by following this page: https://quarkus.io/guides/deploying-to-kubernetes


Mostly we use this page as a reference - https://quarkus.io/guides/config-reference

## Extensions used here

1 - Container image Jib - to create our local docker image

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-container-image-jib</artifactId>
</dependency>
```

2 - Yaml config - allow us to get values from the config map

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-config-yaml</artifactId>
</dependency>
```

3 - Minikube - to generate minikube YAML file

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
> [!TIP]
> In case you are under some Internet office network, include these parameters in Maven clean package execution:

```shell script
Dhttp.proxyHost=<PROXY IP> -Dhttp.proxyPort=<PROXY PORT> -Dhttps.proxyHost=<PROXY IP> -Dhttps.proxyPort=<PROXY PORT>
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
> [!TIP]
> There is a small possibility you may get an error "This control plane is not running! (state=Stopped)" (ie, by using Gokube).
> To workaround that, use this Kubernetes command to get the port number

```shell script
kubectl get svc demo-quarkus-k8s
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


## For monitoring

1 - Include this dependency:

```xml
<dependency>
      <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

2 - Only by doing that, we can check the metrics:

```shell
curl http://localhost:8080/q/metrics
```

3 - Include this `MeterRegistry` object to register meters

```java
private final MeterRegistry registry;
```

4 - Include one `gaugeCollectionSize`. Gauges measure a value that can increase or decrease over time, 
like the speedometer on a car. Gauges can be useful when monitoring the statistics for a cache or collection.

```java
LinkedList<Long> list = new LinkedList<>();

ExampleResource(MeterRegistry registry) {
    this.registry = registry;
    registry.gaugeCollectionSize("example.list.size", Tags.empty(), list);
}

@GET
@Path("gauge/{number}")
public Long checkListSize(@PathParam("number") long number) {
    if (number == 2 || number % 2 == 0) {
        // add even numbers to the list
        list.add(number);
    } else {
        // remove items from the list for odd numbers
        try {
            number = list.removeFirst();
        } catch (NoSuchElementException nse) {
            number = 0;
        }
    }
    return number;
}
```
When using Prometheus, the value of the created gauge and the size
of the list is observed when the Prometheus endpoint is visited.

```
# HELP http_server_requests_seconds  
http_server_requests_seconds_count{method="GET",outcome="SUCCESS",status="200",uri="/gauge/{number}"} 2.0
http_server_requests_seconds_sum{method="GET",outcome="SUCCESS",status="200",uri="/gauge/{number}"} 0.029338753
# TYPE http_server_requests_seconds_max gauge
# HELP http_server_requests_seconds_max  
http_server_requests_seconds_max{method="GET",outcome="SUCCESS",status="200",uri="/gauge/{number}"} 0.024757296
```

5 - Include Counters. Counters are used to measure values that only increase.

```java
@GET
@Path("prime/{number}")
public String checkIfPrime(@PathParam("number") long number) {
    if (number < 1) {
        return "Only natural numbers can be prime numbers.";
    }
    if (number % 2 == 0) {
        return number + " is not prime.";
    }

    if ( testPrimeNumber(number) ) {
        return number + " is prime.";
    } else {
        return number + " is not prime.";
    }
}

protected boolean testPrimeNumber(long number) {
    // Count the number of times we test for a prime number
    registry.counter("example.prime.number").increment();
    for (int i = 3; i < Math.floor(Math.sqrt(number)) + 1; i = i + 2) {
        if (number % i == 0) {
            return false;
        }
    }
    return true;
}
```

Adding a label or tag to the counter might be tempting to indicate what value was checked. 
Remember that each unique combination of a metric name (testPrimeNumber) and label value produces a unique time series. 
Using an unbounded data set as label values can lead to a "cardinality explosion", 
an exponential increase in the creation of new time series.

6 - Now we can run some examples and check the `example_prime_number_total` counters:

```shell
curl http://localhost:8080/prime/-1
curl http://localhost:8080/prime/0
curl http://localhost:8080/prime/1
curl http://localhost:8080/prime/2
curl http://localhost:8080/prime/3
curl http://localhost:8080/prime/15
curl http://localhost:8080/q/metrics
```

And we should find these metrics:

```
# TYPE http_server_requests_seconds summary
# HELP http_server_requests_seconds  
http_server_requests_seconds_count{method="GET",outcome="SUCCESS",status="200",uri="/prime/{number}"} 7.0
http_server_requests_seconds_sum{method="GET",outcome="SUCCESS",status="200",uri="/prime/{number}"} 0.020481525
http_server_requests_seconds_count{method="GET",outcome="CLIENT_ERROR",status="404",uri="NOT_FOUND"} 9.0
http_server_requests_seconds_sum{method="GET",outcome="CLIENT_ERROR",status="404",uri="NOT_FOUND"} 1.086705802
# TYPE http_server_requests_seconds_max gauge
# HELP http_server_requests_seconds_max  
http_server_requests_seconds_max{method="GET",outcome="SUCCESS",status="200",uri="/prime/{number}"} 0.007181645
http_server_requests_seconds_max{method="GET",outcome="CLIENT_ERROR",status="404",uri="NOT_FOUND"} 0.0
# TYPE jvm_gc_max_data_size_bytes gauge
# HELP jvm_gc_max_data_size_bytes Max size of long-lived heap memory pool
jvm_gc_max_data_size_bytes 2.057306112E9
# TYPE example_prime_number counter
# HELP example_prime_number  
example_prime_number_total 3.0
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

