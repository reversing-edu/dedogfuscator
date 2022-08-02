package edu.reversing.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

//TODO parse this from file, maybe yml is good for basic config options like this
public class Configuration {

    private final Path input;
    private final Path output;

    public Configuration(Path input, Path output) {
        this.input = input;
        this.output = output;
    }

    public static Configuration valueOf(String... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException();
        }

        return new Configuration(
                Paths.get(args[0]),
                Paths.get(args[1])
        );
    }

    public Path getInput() {
        return input;
    }

    public Path getOutput() {
        return output;
    }
}
