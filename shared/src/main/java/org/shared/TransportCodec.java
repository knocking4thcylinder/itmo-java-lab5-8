package org.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * Кодек для сериализации объектов транспортного уровня.
 */
public final class TransportCodec {

    private TransportCodec() {}

    /**
     * Сериализует объект в буфер с префиксом длины.
     *
     * @param object сериализуемый объект
     * @return буфер, готовый к записи в канал
     * @throws IOException если сериализация завершилась ошибкой
     */
    public static ByteBuffer encode(Object object) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(object);
            objectStream.flush();
        }
        byte[] payload = byteStream.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + payload.length);
        buffer.putInt(payload.length);
        buffer.put(payload);
        buffer.flip();
        return buffer;
    }

    /**
     * Десериализует объект из массива байтов.
     *
     * @param payload массив байтов без префикса длины
     * @return десериализованный объект
     * @throws IOException если чтение завершилось ошибкой
     * @throws ClassNotFoundException если тип объекта не найден
     */
    public static Object decode(byte[] payload)
        throws IOException, ClassNotFoundException {
        try (
            ObjectInputStream objectStream = new ObjectInputStream(
                new ByteArrayInputStream(payload)
            )
        ) {
            return objectStream.readObject();
        }
    }
}
