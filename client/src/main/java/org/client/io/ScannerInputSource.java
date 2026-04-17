package org.client.io;

import java.io.InputStream;
import java.util.Scanner;

public final class ScannerInputSource implements InputSource, AutoCloseable {

    private final Scanner scanner;

    public ScannerInputSource(InputStream inputStream) {
        this.scanner = new Scanner(inputStream);
    }

    @Override
    public String readLine() {
        return scanner.nextLine();
    }

    @Override
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    @Override
    public void close() {
        scanner.close();
    }
}
