package org.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class SerializationUtils {

    private SerializationUtils() {}

    public static byte[] serialize(Object object) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(object);
            objectStream.flush();
            return byteStream.toByteArray();
        }
    }

    public static Object deserialize(byte[] payload)
        throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(payload);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return objectStream.readObject();
        }
    }

    public static int serializedSize(Object object) {
        try {
            return serialize(object).length;
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Unable to measure serialized object size",
                exception
            );
        }
    }
}
