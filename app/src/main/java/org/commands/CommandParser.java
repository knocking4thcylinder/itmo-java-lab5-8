package org.commands;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

public class CommandParser implements AutoCloseable, Iterable<String[]> {

    private Scanner inputScanner;

    public CommandParser(InputStream inputStream) {
        this.inputScanner = new Scanner(inputStream);
    }

    public String[] parseLine() {
        return inputScanner.nextLine().trim().split(" ");
    }

    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {
            public boolean hasNext() {
                return inputScanner.hasNextLine();
            }

            public String[] next() {
                return parseLine();
            }
        };
    }

    public void close() {
        inputScanner.close();
    }
}
