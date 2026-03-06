package org.commands;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Stream;

public class CommandParser implements AutoCloseable, Iterable<String[]> {

    private Scanner inputScanner;
    public InputStream inputStream;

    public CommandParser(InputStream inputStream) {
        this.inputStream = inputStream;
        this.inputScanner = new Scanner(inputStream);
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        this.inputScanner = new Scanner(inputStream, "UTF-8");
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public String[] parseCommand() {
        return inputScanner.nextLine().trim().split(" ");
    }

    public <T> T parseObject(Class<T> objectType) throws Exception {
        T instance = objectType.getDeclaredConstructor().newInstance();

        Stream<Method> setterArray = Stream.of(objectType.getMethods()).filter(
            e ->
                e.getName().startsWith("set") &&
                e.getParameterCount() == 1 &&
                !Modifier.isStatic(e.getModifiers())
        );
        for (Object setter : setterArray.toArray()) {
            Method tmpSetter = (Method) setter;
            String tmpSetterName = tmpSetter.getName();
            Class<?> tmpFieldType = tmpSetter.getParameterTypes()[0];
            System.out.println(tmpSetterName + " " + tmpFieldType);
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
        Method setterMethod = instance
            .getClass()
            .getMethod(setterName, fieldType);
        System.out.print(
            "insert value for field " +
                Character.toLowerCase(setterName.charAt(3)) +
                setterName.substring(4) +
                " > "
        );
        try {
            if (
                fieldType.equals(int.class) || fieldType.equals(Integer.class)
            ) {
                result = inputScanner.nextInt();
            } else if (
                fieldType.equals(long.class) || fieldType.equals(Long.class)
            ) {
                result = inputScanner.nextLong();
            } else if (
                fieldType.equals(double.class) || fieldType.equals(Double.class)
            ) {
                result = inputScanner.nextDouble();
            } else if (fieldType.equals(String.class)) {
                result = inputScanner.nextLine();
            } else if (fieldType.equals(LocalDateTime.class)) {
                result = LocalDateTime.parse(inputScanner.nextLine());
            } else if (fieldType.isEnum()) {
                System.out.print("\npossible values for this field: ");
                Object[] enumConstants = fieldType.getEnumConstants();
                ArrayList<String> enumConstantNames = new ArrayList<String>();
                for (Object constant : enumConstants) {
                    Enum<?> value = (Enum<?>) constant;
                    enumConstantNames.add(value.name());
                }
                System.out.println(String.join(", ", enumConstantNames));
                result = Enum.valueOf(
                    (Class<Enum>) fieldType,
                    inputScanner.nextLine()
                );
            }
        } catch (InputMismatchException e) {}
        if (result != null) {
            setterMethod.invoke(instance, result);
            return;
        }

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
            .toArray();

        for (Object setter : setterArray) {
            Method tmpSetter = (Method) setter;
            String tmpSetterName = tmpSetter.getName();
            Class<?> tmpFieldType = tmpSetter.getParameterTypes()[0];
            System.out.println(tmpSetterName + " " + tmpFieldType);
            this.parseField(tmpObject, tmpSetterName, tmpFieldType);
        }
        result = tmpObject;
        setterMethod.invoke(instance, tmpObject);
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
