package org.commands;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Stream;

public class InputParser implements AutoCloseable, Iterable<String[]> {

    private Scanner inputScanner;

    public InputParser(InputStream inputStream) {
        this.inputScanner = new Scanner(inputStream);
    }

    public void setInputStream(InputStream inputStream) {
        this.inputScanner = new Scanner(inputStream);
    }

    public String[] parseCommand() {
        return inputScanner.nextLine().trim().split(" ");
    }

    public <T> T parseObject(T instance) throws Exception {
        Stream<Method> setterArray = Stream.of(instance.getClass().getMethods())
            .filter(
                e ->
                    e.getName().startsWith("set") &&
                    e.getParameterCount() == 1 &&
                    !Modifier.isStatic(e.getModifiers())
            )
            .sorted(Comparator.comparing(Method::getName));
        for (Object setter : setterArray.toArray()) {
            Method tmpSetter = (Method) setter;
            String tmpSetterName = tmpSetter.getName();
            Class<?> tmpFieldType = tmpSetter.getParameterTypes()[0];
            // System.out.println(tmpSetterName + " " + tmpFieldType);
            this.parseField(instance, tmpSetterName, tmpFieldType);
        }
        return instance;
    }

    private void parseField(
        Object instance,
        String setterName,
        Class<?> fieldType
    ) throws Exception {
        Object result = null;
        String parsedInput = "";
        Method setterMethod = instance
            .getClass()
            .getMethod(setterName, fieldType);
        String fieldName =
            Character.toLowerCase(setterName.charAt(3)) +
            setterName.substring(4);
        System.out.print(
            "insert value for field " +
                fieldName +
                " with type " +
                fieldType.getSimpleName()
        );
        boolean flag = false;
        do {
            flag = false;
            try {
                if (
                    fieldType.equals(int.class) ||
                    fieldType.equals(Integer.class)
                ) {
                    System.out.print(
                        "\n" +
                            instance.getClass().getSimpleName() +
                            "." +
                            fieldName +
                            ": "
                    );
                    parsedInput = inputScanner.nextLine();
                    result = Integer.valueOf(parsedInput.trim());
                } else if (
                    fieldType.equals(long.class) || fieldType.equals(Long.class)
                ) {
                    System.out.print(
                        "\n" +
                            instance.getClass().getSimpleName() +
                            "." +
                            fieldName +
                            ": "
                    );
                    parsedInput = inputScanner.nextLine();
                    result = Long.valueOf(parsedInput.trim());
                } else if (
                    fieldType.equals(double.class) ||
                    fieldType.equals(Double.class)
                ) {
                    System.out.print(
                        "\n" +
                            instance.getClass().getSimpleName() +
                            "." +
                            fieldName +
                            ": "
                    );
                    parsedInput = inputScanner.nextLine();
                    result = Double.valueOf(parsedInput.trim());
                } else if (fieldType.equals(String.class)) {
                    System.out.print(
                        "\n" +
                            instance.getClass().getSimpleName() +
                            "." +
                            fieldName +
                            ": "
                    );

                    parsedInput = inputScanner.nextLine();
                    result = parsedInput.trim();
                } else if (fieldType.isEnum()) {
                    System.out.print(", possible values for this field: ");
                    Object[] enumConstants = fieldType.getEnumConstants();
                    ArrayList<String> enumConstantNames = new ArrayList<
                        String
                    >();
                    for (Object constant : enumConstants) {
                        Enum<?> value = (Enum<?>) constant;
                        enumConstantNames.add(value.name());
                    }
                    System.out.print(String.join(", ", enumConstantNames));
                    System.out.print(
                        "\n" +
                            instance.getClass().getSimpleName() +
                            "." +
                            fieldName +
                            ": "
                    );
                    parsedInput = inputScanner.nextLine();
                    result = Enum.valueOf(
                        (Class<Enum>) fieldType,
                        parsedInput.trim()
                    );
                }
            } catch (InputMismatchException e) {
                e.printStackTrace();
                flag = true;
                continue;
            } catch (IllegalArgumentException e) {
                System.out.print(
                    "cannot propperly parse string \"" +
                        parsedInput +
                        "\", the value must be of type \"" +
                        fieldType.getSimpleName() +
                        "\". please try again"
                );
                flag = true;
                continue;
            }
            if (result != null) {
                if (
                    fieldType.equals(String.class) &&
                    ((String) result).isEmpty()
                ) {
                    result = null;
                }
                try {
                    setterMethod.invoke(instance, result);
                    return;
                } catch (InvocationTargetException e) {
                    System.out.print(
                        e.getCause().getMessage() + ", please try again"
                    );
                    flag = true;
                    continue;
                }
            }

            System.out.println(
                " - this is an object, you will be prompted to insert it's fields now"
            );
            Object tmpObject;
            try {
                tmpObject = fieldType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw e;
            }

            Object[] setterArray = Stream.of(fieldType.getMethods())
                .filter(
                    e ->
                        e.getName().startsWith("set") &&
                        e.getParameterCount() == 1 &&
                        !Modifier.isStatic(e.getModifiers())
                )
                .sorted(Comparator.comparing(Method::getName))
                .toArray();

            for (Object setter : setterArray) {
                Method tmpSetter = (Method) setter;
                String tmpSetterName = tmpSetter.getName();
                Class<?> tmpFieldType = tmpSetter.getParameterTypes()[0];
                // System.out.println(tmpSetterName + " " + tmpFieldType);
                this.parseField(tmpObject, tmpSetterName, tmpFieldType);
            }
            result = tmpObject;
            setterMethod.invoke(instance, tmpObject);
        } while (flag);
    }

    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {
            public boolean hasNext() {
                return inputScanner.hasNextLine();
            }

            public String[] next() {
                return parseCommand();
            }
        };
    }

    public void close() {
        inputScanner.close();
    }
}
