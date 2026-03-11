package org.commands;

import org.App;
import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда для добавления нового элемента с заданным ключом.
 */

public class InsertCommand implements Executable {

    /**
     * Добавляет фильм с заданным ключом.
     * @param args аргументы команды, где args[0] - ключ
     * @return результат выполнения
     * @throws Exception при ошибке ввода
     */
    @Override
    public String exec(String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"insert\" accepts exactly one argument"
            );
        }
        String key = args[0];
        var collection = CollectionManager.getInstance().getCollection();
        InputParser inputParser = App.getInputParser();
        Movie movie = new Movie();
        movie = inputParser.parseObject(movie);
        collection.put(key, movie);
        return "element " + key + " successfully inserted";
    }
}
