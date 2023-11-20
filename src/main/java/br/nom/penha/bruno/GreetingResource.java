package br.nom.penha.bruno;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.LinkedList;
import java.util.NoSuchElementException;

@Path("/")
public class GreetingResource {

    @ConfigProperty(name = "env.name")
    String envName;

    /**
     * To register meters, you need a reference to a MeterRegistry,
     * which is configured and maintained by the Micrometer extension
     */
    private final MeterRegistry registry;

    LinkedList<Long> list = new LinkedList<>();

    public GreetingResource(MeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive from: " + envName;
    }

    @GET
    @Path("prime/{number}")
    public String checkIfPrime(@PathParam("number") long number) {
        if (number < 1) {
            return "Only natural numbers can be prime numbers.";
        }
        if (number == 1 || number == 2 || number % 2 == 0) {
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
}
