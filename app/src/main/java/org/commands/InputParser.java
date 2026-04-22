package org.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Iterator;

/**
 * Класс для парсинга пользовательского ввода.
 * Обеспечивает ввод команд и данных фильмов из источника ввода.
 */
public class InputParser implements AutoCloseable, Iterable<String[]> {

    private InputSource inputSource;

    /**
     * Конструктор с источником ввода.
     * @param inputSource источник ввода (например, ScannerInputSource)
     */
    public InputParser(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    /**
     * Устанавливает новый источник ввода.
     * @param inputSource новый источник ввода
     */
    public void setInputSource(InputSource inputSource) {
        this.inputSource = inputSource;
    }

    /**
     * Возвращает текущий источник ввода.
     * @return текущий источник ввода
     */
    public InputSource getInputSource() {
        return inputSource;
    }

    /**
     * Парсит команду из входного потока.
     * @return массив строк - команда и её аргументы
     */
    public String[] parseCommand() {
        return inputSource.readLine().trim().split(" ");
    }

    /**
     * Парсит объект, заполняя его поля через пользовательский ввод.
     * @param instance экземпляр объекта для заполнения
     * @param <T> тип объекта
     * @return заполненный объект
     * @throws Exception при ошибке парсинга
     */
    public <T> T parseObject(T instance) throws Exception {
        for (var field : Arrays.stream(instance.getClass().getDeclaredFields()).toList()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String fieldName = field.getName();
            String setterName =
                "set" +
                Character.toUpperCase(fieldName.charAt(0)) +
                fieldName.substring(1);
            Method setterMethod;
            try {
                setterMethod = instance
                    .getClass()
                    .getMethod(setterName, field.getType());
            } catch (NoSuchMethodException e) {
                continue;
            }
            Class<?> fieldType = setterMethod.getParameterTypes()[0];
            while (true) {
                try {
                    this.parseField(instance, setterName, fieldType);
                    break;
                } catch (InputMismatchException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    System.out.print(
                        "cannot properly parse input, the value must be of type \"" +
                            fieldType.getSimpleName() +
                            "\". please try again\n"
                    );
                }
            }
        }
        return instance;
    }

    /**
     * Парсит отдельное поле объекта.
     * @param instance объект, которому принадлежит поле
     * @param setterName имя сеттера для поля
     * @param fieldType тип поля
     * @throws Exception при ошибке парсинга
     */
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
        if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
            System.out.print(
                "\n" +
                    instance.getClass().getSimpleName() +
                    "." +
                    fieldName +
                    ": "
            );
            parsedInput = inputSource.readLine();
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
            parsedInput = inputSource.readLine();
            result = Long.valueOf(parsedInput.trim());
        } else if (
            fieldType.equals(double.class) || fieldType.equals(Double.class)
        ) {
            System.out.print(
                "\n" +
                    instance.getClass().getSimpleName() +
                    "." +
                    fieldName +
                    ": "
            );
            parsedInput = inputSource.readLine();
            result = Double.valueOf(parsedInput.trim());
        } else if (fieldType.equals(String.class)) {
            System.out.print(
                "\n" +
                    instance.getClass().getSimpleName() +
                    "." +
                    fieldName +
                    ": "
            );

            parsedInput = inputSource.readLine();
            result = escapeXml(parsedInput.trim());
        } else if (fieldType.isEnum()) {
            System.out.print(", possible values for this field: ");
            Object[] enumConstants = fieldType.getEnumConstants();
            ArrayList<String> enumConstantNames = new ArrayList<String>();
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
            parsedInput = inputSource.readLine();
            result = Enum.valueOf((Class<Enum>) fieldType, parsedInput.trim());
        }
        if (result != null) {
            if (fieldType.equals(String.class) && ((String) result).isEmpty()) {
                result = null;
            }
            try {
                setterMethod.invoke(instance, result);
                return;
            } catch (InvocationTargetException e) {
                throw (IllegalArgumentException) e.getCause();
            }
        }

        System.out.println(
            " - this is an object, you will be prompted to insert its fields now"
        );
        Object tmpObject;
        try {
            tmpObject = fieldType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw e;
        }
        parseObject(tmpObject);
        result = tmpObject;
        setterMethod.invoke(instance, tmpObject);
    }

    /**
     * Возвращает итератор для перебора команд из входного потока.
     * @return итератор массивов строк (команд)
     */
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {
            public boolean hasNext() {
                return inputSource.hasNextLine();
            }

            public String[] next() {
                return parseCommand();
            }
        };
    }

    private String escapeXml(String input) {
        return input
            .replace("&",  "&amp;")
            .replace("<",  "&lt;")
            .replace(">",  "&gt;")
            .replace("\"", "&quot;")
            .replace("'",  "&apos;");
    }

    /**
     * Закрывает источник ввода.
     */
    public void close() {
        if (inputSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) inputSource).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
