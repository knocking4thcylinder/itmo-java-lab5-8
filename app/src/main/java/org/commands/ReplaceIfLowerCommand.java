package org.commands;

import org.App;
import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда для замены значения по ключу, если новое меньше старого.
 */

public class ReplaceIfLowerCommand implements Executable {

    /**
     * Заменяет фильм если новое значение меньше старого.
     * @param args аргументы команды, где args[0] - ключ
     * @return результат выполнения
     * @throws Exception при ошибке ввода
     */
    @Override
    public String exec(String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"replace_if_lower\" accepts exactly one argument"
            );
        }

        String key = args[0];
        var collection = CollectionManager.getInstance().getCollection();
        if (!collection.containsKey(key)) {
            return "no element with key " + key + " exists in the collection";
        }
        InputParser inputParser = App.getInputParser();
        Movie movie = new Movie();
        movie = inputParser.parseObject(movie);
        if (movie.compareTo(collection.get(key)) < 0) {
            collection.put(key, movie);
            return "element " + key + " successfully updated";
        }
        return "element " + key + " was not replaced (new value is not lower)";
    }
}
