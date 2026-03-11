package org.commands;

import org.App;
import org.CollectionManager;
import org.dataclasses.Movie;

/**
 * Команда для обновления элемента коллекции по id.
 */

public class UpdateCommand implements Executable {

    /**
     * Обновляет фильм по id.
     * @param args аргументы команды, где args[0] - id фильма
     * @return результат выполнения
     * @throws Exception при ошибке ввода
     */
    @Override
    public String exec(String... args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "command \"update\" accepts exactly one argument"
            );
        }

        int id;
        try {
            id = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            return "\"" + args[0] + "\" is not a valid id";
        }
        var collection = CollectionManager.getInstance().getCollection();
        InputParser inputParser = App.getInputParser();
        for (var entry : collection.entrySet()) {
            if (entry.getValue().getId() == id) {
                Movie movie = inputParser.parseObject(entry.getValue());
                collection.put(entry.getKey(), movie);
                return "element " + entry.getKey() + " successfully updated";
            }
        }
        return "No element with that id exists";
    }
}
