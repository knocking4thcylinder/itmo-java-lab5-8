package org.commands;

import java.io.InputStream;
import java.util.Scanner;

public class InputStreamParserBox<T extends InputStream> {

    private Scanner inputScanner;

    public InputStreamParserBox(T inputStream) {
        this.inputScanner = new Scanner(inputStream);
    }

    public Scanner getInputScanner() {
        return this.inputScanner;
    }
}
