package io.floci.testcontainers.testing;

import org.testcontainers.containers.GenericContainer;

public class ContainerUtils {

    private ContainerUtils() {
    }

    public static GenericContainer<?> genericContainer() {
        return new GenericContainer<>("alpine:latest");
    }
}
