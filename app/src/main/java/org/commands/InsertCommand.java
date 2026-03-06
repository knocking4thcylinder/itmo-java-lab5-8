package org.commands;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Scanner;
import java.util.stream.Stream;
import org.App;
import org.dataclasses.Movie;

public class InsertCommand implements Executable {

    public static void exec(String... args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"insert\" accepts exactly one argument"
            );
        }
        String key = args[0];
        var collection = App.getCollection();
    }
}
