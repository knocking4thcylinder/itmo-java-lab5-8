package org.server.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class AdminConsole {

    private final BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(System.in, StandardCharsets.UTF_8)
    );

    public String pollCommand() throws IOException {
        if (!bufferedReader.ready()) {
            return null;
        }
        return bufferedReader.readLine();
    }
}
