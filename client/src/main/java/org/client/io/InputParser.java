package org.client.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.stream.Stream;

public final class InputParser implements AutoCloseable {

    private InputSource inputSource;

    public InputParser(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    public void setInputSource(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    public InputSource getInputSource() {
        return inputSource;
    }

    public boolean hasNextLine() {
        return inputSource.hasNextLine();
    }

    public String[] parseCommand() {
        String line = inputSource.readLine();
        if (line == null || line.isBlank()) {
            return new String[] { "" };
        }
        return line.trim().split("\\s+");
    }

    public <T> T parseObject(T instance) throws Exception {
        Stream<Method> setterArray = Stream.of(instance.getClass().getMethods())
            .filter(
                method ->
                    method.getName().startsWith("set") &&
                    method.getParameterCount() == 1 &&
                    !Modifier.isStatic(method.getModifiers())
            )
            .sorted(Comparator.comparing(Method::getName));
        for (Method setterMethod : setterArray.toList()) {
            Class<?> fieldType = setterMethod.getParameterTypes()[0];
            while (true) {
                try {
                    parseField(instance, setterMethod, fieldType);
                    break;
                } catch (InputMismatchException exception) {
                    System.out.println(exception.getMessage());
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                    System.out.println(
                        "Cannot properly parse input, the value must be of type \"" +
                        fieldType.getSimpleName() +
                        "\". Please try again."
                    );
                }
            }
        }
        return instance;
    }

    private void parseField(
        Object instance,
        Method setterMethod,
        Class<?> fieldType
    ) throws Exception {
        String fieldName =
            Character.toLowerCase(setterMethod.getName().charAt(3)) +
            setterMethod.getName().substring(4);
        System.out.print(
            "Insert value for field " +
            fieldName +
            " with type " +
            fieldType.getSimpleName()
        );

        Object result = null;
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            result = Integer.valueOf(readValue(instance, fieldName));
        } else if (
            fieldType.equals(long.class) || fieldType.equals(Long.class)
        ) {
            result = Long.valueOf(readValue(instance, fieldName));
        } else if (
            fieldType.equals(double.class) || fieldType.equals(Double.class)
        ) {
            result = Double.valueOf(readValue(instance, fieldName));
        } else if (fieldType.equals(String.class)) {
            result = readValue(instance, fieldName);
            if (((String) result).isEmpty()) {
                result = null;
            }
        } else if (fieldType.isEnum()) {
            System.out.print(
                ", possible values: " +
                String.join(
                    ", ",
                    Stream.of(fieldType.getEnumConstants())
                        .map(constant -> ((Enum<?>) constant).name())
                        .toList()
                )
            );
            result = Enum.valueOf(
                (Class<Enum>) fieldType,
                readValue(instance, fieldName)
            );
        }

        if (result != null) {
            try {
                setterMethod.invoke(instance, result);
                return;
            } catch (InvocationTargetException exception) {
                throw (Exception) exception.getCause();
            }
        }

        System.out.println(" - nested object, entering nested fields");
        Object nestedObject = fieldType.getDeclaredConstructor().newInstance();
        setterMethod.invoke(instance, parseObject(nestedObject));
    }

    private String readValue(Object instance, String fieldName) {
        System.out.print(
            "\n" +
            instance.getClass().getSimpleName() +
            "." +
            fieldName +
            ": "
        );
        return inputSource.readLine().trim();
    }

    @Override
    public void close() {
        if (inputSource instanceof AutoCloseable autoCloseable) {
            try {
                autoCloseable.close();
            } catch (Exception exception) {
                throw new IllegalStateException(
                    "Failed to close input source",
                    exception
                );
            }
        }
    }
}
