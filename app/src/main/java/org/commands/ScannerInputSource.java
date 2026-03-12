package org.commands;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Реализация InputSource на основе Scanner.
 * Считывает данные из InputStream (например, System.in или файла).
 */
public class ScannerInputSource implements InputSource {
    private Scanner scanner;

    /**
     * Конструктор с потоком ввода.
     * @param inputStream поток ввода (System.in, FileInputStream и т.д.)
     */
    public ScannerInputSource(InputStream inputStream) {
        this.scanner = new Scanner(inputStream);
    }

    /**
     * Считывает одну строку из потока.
     * @return считанная строка или null, если ввод закончился
     */
    @Override
    public String readLine() {
        return scanner.nextLine();
    }

    /**
     * Проверяет, есть ли следующая строка.
     * @return true, если есть следующая строка
     */
    @Override
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }
}
