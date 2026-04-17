package org.client.io;

import java.io.Closeable;

public interface InputSource extends Closeable {
    String readLine();

    boolean hasNextLine();
}
