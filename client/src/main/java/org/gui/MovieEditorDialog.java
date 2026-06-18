package org.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.dataclasses.Coordinates;
import org.dataclasses.Location;
import org.dataclasses.Movie;
import org.dataclasses.Person;
import org.dataclasses.enums.Country;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;

final class MovieEditorDialog extends JDialog {
    private final JTextField key = new JTextField();
    private final JTextField name = new JTextField();
    private final JTextField x = new JTextField();
    private final JTextField y = new JTextField();
    private final JTextField oscars = new JTextField();
    private final JComboBox<MovieGenre> genre = new JComboBox<>(MovieGenre.values());
    private final JComboBox<MpaaRating> rating = new JComboBox<>(MpaaRating.values());
    private final JTextField director = new JTextField();
    private final JTextField weight = new JTextField("70.0");
    private final JTextField passportId = new JTextField();
    private final JComboBox<Country> country = new JComboBox<>(Country.values());
    private final JTextField locationX = new JTextField("0");
    private final JTextField locationY = new JTextField("0.0");
    private final JTextField locationName = new JTextField();
    private final JLabel validation = new JLabel(" ");
    private final Function<String, String> translator;
    private Movie movie;
    private boolean saved;

    MovieEditorDialog(JFrame owner, MovieRow row, Function<String, String> translator) {
        super(owner, row == null ? translator.apply("button.add") : translator.apply("button.edit"), true);
        this.translator = translator;
        setLayout(new BorderLayout(10, 10));

        JPanel fields = new JPanel(new GridLayout(0, 2, 0, 0));
        addField(fields, "field.key", key);
        addField(fields, "field.name", name);
        addField(fields, "field.coordinateX", x);
        addField(fields, "field.coordinateY", y);
        addField(fields, "field.oscars", oscars);
        addField(fields, "field.genre", genre);
        addField(fields, "field.rating", rating);
        addField(fields, "field.director", director);
        addField(fields, "field.weight", weight);
        addField(fields, "field.passportId", passportId);
        addField(fields, "field.country", country);
        addField(fields, "field.locationX", locationX);
        addField(fields, "field.locationY", locationY);
        addField(fields, "field.locationName", locationName);
        add(fields, BorderLayout.CENTER);

        validation.setOpaque(true);
        validation.setBackground(new Color(254, 249, 195));
        validation.setBorder(BorderFactory.createLineBorder(new Color(202, 138, 4)));
        validation.setHorizontalAlignment(JLabel.CENTER);
        add(validation, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 0, 0));
        JButton save = new JButton(translator.apply("button.save"));
        JButton delete = new JButton(translator.apply("button.delete"));
        JButton cancel = new JButton(translator.apply("button.cancel"));
        buttons.add(save);
        buttons.add(delete);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);

        save.addActionListener(event -> save());
        delete.setEnabled(false);
        cancel.addActionListener(event -> setVisible(false));
        populate(row);
        pack();
        setSize(520, 420);
        setLocationRelativeTo(owner);
    }

    private void addField(JPanel panel, String label, JComponent field) {
        JLabel cell = new JLabel(translator.apply(label));
        cell.setOpaque(true);
        cell.setBackground(UiTheme.SOFT);
        cell.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        panel.add(cell);
        panel.add(field);
    }

    private void populate(MovieRow row) {
        if (row == null) {
            return;
        }
        key.setText(row.key());
        key.setEnabled(false);
        name.setText(row.name());
        x.setText(Integer.toString(row.x()));
        y.setText(Integer.toString(row.y()));
        oscars.setText(Integer.toString(row.oscars()));
        genre.setSelectedItem(MovieGenre.valueOf(row.genre()));
        rating.setSelectedItem(MpaaRating.valueOf(row.rating()));
        director.setText(row.director());
        if (!row.weight().isBlank()) {
            weight.setText(row.weight());
        }
        if (!row.country().isBlank()) {
            country.setSelectedItem(Country.valueOf(row.country()));
        }
        passportId.setText(row.passportId());
        locationX.setText(row.locationX().isBlank() ? "0" : row.locationX());
        locationY.setText(row.locationY().isBlank() ? "0.0" : row.locationY());
        locationName.setText(row.locationName());
    }

    private void save() {
        String error = validateInput();
        if (error != null) {
            showValidation(error);
            return;
        }
        try {
            movie = buildMovie();
            saved = true;
            setVisible(false);
        } catch (IllegalArgumentException | NullPointerException e) {
            showValidation(e.getMessage());
        }
    }

    boolean saved() {
        return saved;
    }

    String key() {
        return key.getText().trim();
    }

    Movie movie() {
        return movie;
    }

    private String validateInput() {
        if (key.isEnabled() && key.getText().trim().isBlank()) {
            return translator.apply("validation.key.empty");
        }
        if (name.getText().trim().isBlank()) {
            return translator.apply("validation.movie.name.empty");
        }
        if (parseInteger(x) == null) {
            return translator.apply("validation.coordinates.x.integer");
        }
        if (parseInteger(y) == null) {
            return translator.apply("validation.coordinates.y.integer");
        }
        Integer oscarsValue = parseInteger(oscars);
        if (oscarsValue == null) {
            return translator.apply("validation.movie.oscars.integer");
        }
        if (oscarsValue <= 0) {
            return translator.apply("validation.movie.oscars.positive");
        }
        if (genre.getSelectedItem() == null) {
            return translator.apply("validation.movie.genre.empty");
        }
        if (rating.getSelectedItem() == null) {
            return translator.apply("validation.movie.rating.empty");
        }
        if (director.getText().trim().isBlank()) {
            return translator.apply("validation.person.name.empty");
        }
        Double weightValue = parseDouble(weight);
        if (weightValue == null) {
            return translator.apply("validation.person.weight.number");
        }
        if (weightValue <= 0) {
            return translator.apply("validation.person.weight.positive");
        }
        if (country.getSelectedItem() == null) {
            return translator.apply("validation.person.country.empty");
        }
        String passportValue = passportId.getText().trim();
        if (!passportValue.isEmpty() && passportValue.length() < 8) {
            return translator.apply("validation.person.passport.length");
        }
        if (parseLong(locationX) == null) {
            return translator.apply("validation.location.x.integer");
        }
        if (parseDouble(locationY) == null) {
            return translator.apply("validation.location.y.number");
        }
        if (locationName.getText() != null && locationName.getText().isBlank() && !locationName.getText().isEmpty()) {
            return translator.apply("validation.location.name.empty");
        }
        return null;
    }

    private Integer parseInteger(JTextField field) {
        String text = field.getText().trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(JTextField field) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(JTextField field) {
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showValidation(String message) {
        validation.setText(message == null ? " " : message);
        validation.setBackground(new Color(254, 226, 226));
        validation.setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38)));
    }

    private Movie buildMovie() {
        Person person = new Person(
            director.getText().trim(),
            Double.parseDouble(weight.getText().trim()),
            passportId.getText().trim().isEmpty() ? null : passportId.getText().trim(),
            (Country) country.getSelectedItem(),
            new Location(
                Long.parseLong(locationX.getText().trim()),
                Double.parseDouble(locationY.getText().trim()),
                locationName.getText().trim().isEmpty()
                    ? null
                    : locationName.getText().trim()
            )
        );
        return new Movie(
            name.getText().trim(),
            new Coordinates(Integer.parseInt(x.getText().trim()), Integer.parseInt(y.getText().trim())),
            Integer.parseInt(oscars.getText().trim()),
            (MovieGenre) genre.getSelectedItem(),
            (MpaaRating) rating.getSelectedItem(),
            person
        );
    }
}
