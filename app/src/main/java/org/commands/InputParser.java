package org.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.stream.Stream;

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
            this.parseField(instance, tmpSetterName, tmpFieldType);
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
                    parsedInput = inputSource.readLine();
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
                    "cannot properly parse string \"" +
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
                " - this is an object, you will be prompted to insert its fields now"
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
                this.parseField(tmpObject, tmpSetterName, tmpFieldType);
            }
            result = tmpObject;
            setterMethod.invoke(instance, tmpObject);
        } while (flag);
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
