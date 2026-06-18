package org.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.commands.ClientCommandInvoker;
import org.commands.ClientContext;
import org.commands.Command;
import org.commands.CommandFactory;
import org.commands.ExecuteScriptCommand;
import org.commands.FilterByGenreCommand;
import org.commands.FilterContainsNameCommand;
import org.commands.FilterLessThanMpaaRatingCommand;
import org.commands.InputParser;
import org.commands.InsertCommand;
import org.commands.LoginCommand;
import org.commands.RegisterCommand;
import org.commands.RemoveGreaterKeyCommand;
import org.commands.RemoveKeyCommand;
import org.commands.RemoveLowerKeyCommand;
import org.commands.ReplaceIfLowerCommand;
import org.commands.ScannerInputSource;
import org.commands.ServerEndpoint;
import org.commands.SharedCommand;
import org.commands.UiSnapshotCommand;
import org.commands.UpdateCommand;
import org.dataclasses.Coordinates;
import org.dataclasses.Location;
import org.dataclasses.Movie;
import org.dataclasses.Person;
import org.dataclasses.enums.Country;
import org.dataclasses.enums.MovieGenre;
import org.dataclasses.enums.MpaaRating;

/**
 * Swing implementation of the approved Lab 8 UI mockup.
 */
public class MovieCollectionFrame extends JFrame {

    private static final Color BORDER = new Color(100, 116, 139);
    private static final Color HEADER = new Color(51, 65, 85);
    private static final Color SOFT = new Color(226, 232, 240);
    private final ClientContext context;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final MovieTableModel tableModel = new MovieTableModel();
    private final JTable table = new JTable(tableModel);
    private final VisualizationPanel visualization = new VisualizationPanel();
    private final JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 0, 0));
    private final JLabel authMessageLabel = new JLabel(" ");
    private final JLabel userLabel = new JLabel();
    private final JLabel connectionLabel = new JLabel();
    private final JLabel countLabel = new JLabel();
    private final JComboBox<ServerEndpoint> serverBox = new JComboBox<>();
    private final List<JComboBox<LanguageItem>> languageBoxes = new ArrayList<>();
    private final JComboBox<String> filterColumn = new JComboBox<>(MovieTableModel.COLUMNS);
    private final JComboBox<String> filterValue = new JComboBox<>();
    private final JComboBox<String> sortColumn = new JComboBox<>(MovieTableModel.COLUMNS);
    private final JComboBox<String> sortDirection = new JComboBox<>();
    private ResourceBundle bundle = Labels.bundle(Locale.forLanguageTag("en-NZ"));
    private Locale currentLocale = Locale.forLanguageTag("en-NZ");
    private boolean authenticatedView;
    private boolean tableSelectionListenerInstalled;
    private boolean updatingServerBox;
    private boolean refreshInProgress;
    private boolean refreshAgainRequested;
    private UpdateSubscription updateSubscription;
    private List<MovieRow> allRows = new ArrayList<>();
    private MovieRow selectedRow;

    public MovieCollectionFrame(String host, int port) {
        super("Movie collection");
        ClientCommandInvoker invoker = new ClientCommandInvoker(host, port, false);
        InputParser inputParser = new InputParser(new ScannerInputSource(System.in));
        CommandFactory commandFactory = new CommandFactory(inputParser);
        context = new ClientContext(inputParser, commandFactory, invoker);
        context.connect(new ServerEndpoint(host, port));
        refreshServerBox();
        serverBox.addActionListener(event -> selectServerFromBox());
        filterColumn.addActionListener(event -> updateFilterValueControl());
        updateFilterValueControl();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                stopUpdateSubscription();
                worker.shutdownNow();
            }
        });
        setMinimumSize(new Dimension(1100, 760));
        setContentPane(buildContent());
        setLocationByPlatform(true);
        relabel();
    }

    private JComponent buildContent() {
        languageBoxes.clear();
        return authenticatedView ? buildMainPanel() : buildAuthPanel();
    }

    private JComponent buildAuthPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets.set(8, 8, 8, 8);

        JPanel form = new JPanel(new BorderLayout(0, 0));
        form.setPreferredSize(new Dimension(650, 260));
        form.setBorder(BorderFactory.createLineBorder(BORDER));
        form.add(sectionHeader("section.authorization"), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 2, 0, 0));
        JTextField login = new JTextField();
        JPasswordField password = new JPasswordField();
        JButton signIn = button("button.signIn");
        JButton register = button("button.register");
        fields.add(cell("field.language", true));
        fields.add(languageBox());
        fields.add(cell("field.login", true));
        fields.add(login);
        fields.add(cell("field.password", true));
        fields.add(password);
        fields.add(signIn);
        fields.add(register);
        form.add(fields, BorderLayout.CENTER);
        authMessageLabel.setOpaque(true);
        authMessageLabel.setBackground(new Color(254, 249, 195));
        authMessageLabel.setBorder(BorderFactory.createLineBorder(new Color(202, 138, 4)));
        authMessageLabel.setHorizontalAlignment(JLabel.CENTER);
        form.add(authMessageLabel, BorderLayout.SOUTH);

        signIn.addActionListener(event ->
            authenticate(false, login.getText(), new String(password.getPassword()), signIn, register)
        );
        register.addActionListener(event ->
            authenticate(true, login.getText(), new String(password.getPassword()), signIn, register)
        );
        centeringPanel.add(form, gbc);
        panel.add(centeringPanel, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildMainPanel() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new GridLayout(0, 1, 0, 10));
        top.add(statusPanel());
        top.add(commandsPanel());
        top.add(filterPanel());
        root.add(top, BorderLayout.NORTH);

        configureTable();

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.add(sectionHeader("section.table"), BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 22, 0));
        bottom.add(visualSection());
        bottom.add(detailsSection());
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.add(bottom, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    private JPanel statusPanel() {
        JPanel panel = section("section.status");
        panel.setLayout(new GridLayout(1, 8, 0, 0));
        panel.add(userLabel);
        panel.add(cell("field.language", true));
        panel.add(languageBox());
        panel.add(connectionLabel);
        panel.add(cell("field.server", true));
        panel.add(serverBox);
        JButton connect = button("button.connect");
        connect.addActionListener(event -> connectServer());
        panel.add(connect);
        panel.add(countLabel);
        return panel;
    }

    private JPanel commandsPanel() {
        JPanel panel = section("section.commands");
        panel.setLayout(new GridLayout(1, 8, 0, 0));
        JButton add = button("button.add");
        JButton edit = button("button.edit");
        JButton delete = button("button.delete");
        JButton clear = button("button.clear");
        JButton info = button("button.info");
        JButton refresh = button("button.refresh");
        JButton more = button("button.moreCommands");
        JButton logout = button("button.logout");
        add.addActionListener(event -> {
            if (ensureAuthenticated()) {
                openEditor(null);
            }
        });
        edit.addActionListener(event -> editSelected());
        delete.addActionListener(event -> deleteSelected());
        clear.addActionListener(event -> {
            if (ensureAuthenticated()) {
                runCommand(new org.commands.ClearCommand(), result -> refreshRows());
            }
        });
        info.addActionListener(event -> {
            if (ensureAuthenticated()) {
                runCommand(new org.commands.InfoCommand(), this::showInfoPopup);
            }
        });
        refresh.addActionListener(event -> {
            if (ensureAuthenticated()) {
                refreshRows();
            }
        });
        more.addActionListener(event -> {
            if (ensureAuthenticated()) {
                showMoreCommandsDialog();
            }
        });
        logout.addActionListener(event -> {
            stopUpdateSubscription();
            context.clearAuthentication();
            authenticatedView = false;
            clearRows();
            rebuildContent();
            relabel();
            showMessage(t("message.loggedOut"));
        });
        panel.add(add);
        panel.add(edit);
        panel.add(delete);
        panel.add(clear);
        panel.add(info);
        panel.add(refresh);
        panel.add(more);
        panel.add(logout);
        return panel;
    }

    private JPanel filterPanel() {
        JPanel panel = section("section.filterSort");
        panel.setLayout(new GridLayout(1, 8, 0, 0));
        JButton apply = button("button.apply");
        JButton reset = button("button.reset");
        apply.addActionListener(event -> applyFilterAndSort());
        reset.addActionListener(event -> {
            filterValue.setSelectedItem("");
            tableModel.setRows(allRows);
            visualization.setRows(allRows);
        });
        panel.add(cell("field.filter", true));
        panel.add(filterColumn);
        panel.add(filterValue);
        panel.add(cell("field.sort", true));
        panel.add(sortColumn);
        panel.add(sortDirection);
        panel.add(apply);
        panel.add(reset);
        return panel;
    }

    private JPanel visualSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.add(sectionHeader("section.visualization"), BorderLayout.NORTH);
        panel.add(visualization, BorderLayout.CENTER);
        visualization.setPreferredSize(new Dimension(500, 190));
        visualization.addMovieClickListener(row -> {
            selectedRow = row;
            tableModel.select(table, row);
            updateSelection();
        });
        return panel;
    }

    private JPanel detailsSection() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.add(sectionHeader("section.objectDetails"), BorderLayout.NORTH);
        detailsPanel.setBorder(BorderFactory.createLineBorder(BORDER));
        outer.add(detailsPanel, BorderLayout.CENTER);
        JPanel actions = new JPanel(new GridLayout(1, 3, 0, 0));
        JButton save = button("button.save");
        JButton delete = button("button.delete");
        JButton cancel = button("button.cancel");
        save.addActionListener(event -> editSelected());
        delete.addActionListener(event -> deleteSelected());
        cancel.addActionListener(event -> {
            table.clearSelection();
            selectedRow = null;
            updateSelection();
        });
        actions.add(save);
        actions.add(delete);
        actions.add(cancel);
        outer.add(actions, BorderLayout.SOUTH);
        updateSelection();
        return outer;
    }

    private void showMoreCommandsDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 0));
        JButton filterByGenre = button("button.filterByGenre");
        JButton filterContainsName = button("button.filterContainsName");
        JButton filterLessThanRating = button("button.filterLessThanRating");
        JButton removeLowerKey = button("button.removeLowerKey");
        JButton removeGreaterKey = button("button.removeGreaterKey");
        JButton replaceIfLower = button("button.replaceIfLower");
        JButton executeScript = button("button.executeScript");

        filterByGenre.addActionListener(event -> {
            MovieGenre genre = chooseEnum("field.genre", MovieGenre.values());
            if (genre != null) {
                runCommand(new FilterByGenreCommand(genre), this::showInfoPopup);
            }
        });
        filterContainsName.addActionListener(event -> {
            String value = promptText("field.name");
            if (value != null) {
                runCommand(new FilterContainsNameCommand(value), this::showInfoPopup);
            }
        });
        filterLessThanRating.addActionListener(event -> {
            MpaaRating rating = chooseEnum("field.rating", MpaaRating.values());
            if (rating != null) {
                runCommand(new FilterLessThanMpaaRatingCommand(rating), this::showInfoPopup);
            }
        });
        removeLowerKey.addActionListener(event -> runKeyCommand("field.key", key ->
            new RemoveLowerKeyCommand(key)
        ));
        removeGreaterKey.addActionListener(event -> runKeyCommand("field.key", key ->
            new RemoveGreaterKeyCommand(key)
        ));
        replaceIfLower.addActionListener(event -> replaceIfLower());
        executeScript.addActionListener(event -> {
            String fileName = promptText("field.scriptFile");
            if (fileName != null) {
                runCommand(new ExecuteScriptCommand(fileName), result -> {
                    showInfoPopup(result);
                    refreshRows();
                });
            }
        });

        panel.add(filterByGenre);
        panel.add(filterContainsName);
        panel.add(filterLessThanRating);
        panel.add(removeLowerKey);
        panel.add(removeGreaterKey);
        panel.add(replaceIfLower);
        panel.add(executeScript);

        JOptionPane.showMessageDialog(
            this,
            panel,
            t("button.moreCommands"),
            JOptionPane.PLAIN_MESSAGE
        );
    }

    private <E extends Enum<E>> E chooseEnum(String title, E[] values) {
        JComboBox<E> comboBox = new JComboBox<>(values);
        int result = JOptionPane.showConfirmDialog(
            this,
            comboBox,
            t(title),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        return result == JOptionPane.OK_OPTION
            ? comboBox.getItemAt(comboBox.getSelectedIndex())
            : null;
    }

    private String promptText(String title) {
        String value = JOptionPane.showInputDialog(this, t(title));
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isBlank() ? null : value;
    }

    private void runKeyCommand(String title, Function<String, SharedCommand> factory) {
        String key = promptText(title);
        if (key != null) {
            runCommand(factory.apply(key), result -> refreshRows());
        }
    }

    private void replaceIfLower() {
        MovieEditorDialog dialog = new MovieEditorDialog(this, null, this::t);
        dialog.setVisible(true);
        if (dialog.saved()) {
            runCommand(
                new ReplaceIfLowerCommand(dialog.key(), dialog.movie()),
                result -> refreshRows()
            );
        }
    }

    private JPanel section(String title) {
        return new SectionPanel(t(title));
    }

    private JLabel sectionHeader(String title) {
        JLabel label = new JLabel(t(title));
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setBackground(HEADER);
        label.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    private JLabel cell(String text, boolean header) {
        JLabel label = new JLabel(t(text));
        label.setOpaque(true);
        label.setBackground(header ? SOFT : Color.WHITE);
        label.setBorder(BorderFactory.createLineBorder(BORDER));
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }

    private JButton button(String text) {
        JButton button = new JButton(t(text));
        button.setFocusPainted(false);
        return button;
    }

    private void configureTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new OwnershipRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        if (tableSelectionListenerInstalled) {
            return;
        }
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                selectedRow = row < 0 ? null : tableModel.rowAt(table.convertRowIndexToModel(row));
                updateSelection();
            }
        });
        tableSelectionListenerInstalled = true;
    }

    private JComboBox<LanguageItem> languageBox() {
        JComboBox<LanguageItem> box = new JComboBox<>(LanguageItem.items());
        box.setSelectedItem(new LanguageItem("", currentLocale));
        languageBoxes.add(box);
        box.addActionListener(event -> {
            LanguageItem item = (LanguageItem) box.getSelectedItem();
            if (item != null && !item.locale().equals(currentLocale)) {
                currentLocale = item.locale();
                bundle = Labels.bundle(currentLocale);
                for (JComboBox<LanguageItem> other : languageBoxes) {
                    if (other != box) {
                        other.setSelectedItem(item);
                    }
                }
                rebuildContent();
            }
        });
        return box;
    }

    private void rebuildContent() {
        setContentPane(buildContent());
        tableModel.setRows(tableModel.rows());
        visualization.setRows(tableModel.rows());
        updateSelection();
        relabel();
        revalidate();
        repaint();
    }

    private void refreshServerBox() {
        updatingServerBox = true;
        DefaultComboBoxModel<ServerEndpoint> model = new DefaultComboBoxModel<>();
        for (ServerEndpoint endpoint : context.sessions().keySet()) {
            model.addElement(endpoint);
        }
        serverBox.setModel(model);
        serverBox.setSelectedItem(context.activeEndpoint());
        updatingServerBox = false;
    }

    private void selectServerFromBox() {
        if (updatingServerBox) {
            return;
        }
        ServerEndpoint endpoint = (ServerEndpoint) serverBox.getSelectedItem();
        if (endpoint == null || endpoint.equals(context.activeEndpoint())) {
            return;
        }
        context.useServer(endpoint);
        showMessage(t("message.activeServer") + " " + endpoint);
        if (context.session(endpoint).isAuthenticated()) {
            authenticatedView = true;
            rebuildContent();
            startUpdateSubscription();
            refreshRows();
        } else {
            stopUpdateSubscription();
            clearRows();
            authenticatedView = false;
            rebuildContent();
            showAuthMessage(t("message.logInTo") + " " + endpoint, false);
        }
    }

    private void connectServer() {
        JTextField hostField = new JTextField("localhost");
        JTextField portField = new JTextField("12345");
        JPanel form = new JPanel(new GridLayout(0, 2, 0, 0));
        form.add(cell("field.host", true));
        form.add(hostField);
        form.add(cell("field.port", true));
        form.add(portField);
        int result = JOptionPane.showConfirmDialog(
            this,
            form,
            t("button.connect"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            ServerEndpoint endpoint = new ServerEndpoint(
                hostField.getText(),
                Integer.parseInt(portField.getText().trim())
            );
            context.connect(endpoint);
            refreshServerBox();
            showMessage(t("message.connectedTo") + " " + endpoint);
            if (context.session(endpoint).isAuthenticated()) {
                authenticatedView = true;
                rebuildContent();
                startUpdateSubscription();
                refreshRows();
            } else {
                stopUpdateSubscription();
                clearRows();
                authenticatedView = false;
                rebuildContent();
                showAuthMessage(t("message.logInTo") + " " + endpoint, false);
            }
        } catch (NumberFormatException e) {
            showMessage(t("message.invalidPort"));
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage());
        }
    }

    private String t(String text) {
        return bundle.containsKey(text) ? bundle.getString(text) : text;
    }

    private void authenticate(
        boolean register,
        String login,
        String password,
        JButton signIn,
        JButton registerButton
    ) {
        String validationError = validateCredentials(login, password);
        if (validationError != null) {
            showAuthMessage(validationError, true);
            return;
        }
        signIn.setEnabled(false);
        registerButton.setEnabled(false);
        showAuthMessage(t("message.connecting"), false);
        SharedCommand command = register
            ? new RegisterCommand(login, password)
            : new LoginCommand(login, password);
        runCommand(
            command,
            result -> {
                showAuthMessage(result, false);
                signIn.setEnabled(true);
                registerButton.setEnabled(true);
                authenticatedView = true;
                rebuildContent();
                startUpdateSubscription();
                refreshRows();
            },
            error -> {
                showAuthMessage(error, true);
                signIn.setEnabled(true);
                registerButton.setEnabled(true);
            }
        );
    }

    private String validateCredentials(String login, String password) {
        if (login == null || login.isBlank()) {
            return t("validation.login.empty");
        }
        if (password == null || password.isBlank()) {
            return t("validation.password.empty");
        }
        return null;
    }

    private void refreshRows() {
        refreshRows(true);
    }

    private void refreshRows(boolean showError) {
        if (refreshInProgress) {
            refreshAgainRequested = true;
            return;
        }
        refreshInProgress = true;
        refreshAgainRequested = false;
        runCommand(new UiSnapshotCommand(), response -> {
            refreshInProgress = false;
            allRows = MovieRow.parse(response);
            tableModel.setRows(allRows);
            visualization.setRows(allRows);
            selectedRow = null;
            updateSelection();
            relabel();
            runPendingRefresh(showError);
        }, error -> {
            refreshInProgress = false;
            if (showError) {
                showMessage(error);
            }
            runPendingRefresh(showError);
        });
    }

    private void runPendingRefresh(boolean showError) {
        if (refreshAgainRequested) {
            refreshRows(showError);
        }
    }

    private void startUpdateSubscription() {
        stopUpdateSubscription();
        if (context.login() == null || context.password() == null) {
            return;
        }
        updateSubscription = new UpdateSubscription(
            context.activeEndpoint(),
            context.login(),
            context.password(),
            () -> refreshRows(false),
            error -> {
                if (error != null && !error.isBlank()) {
                    showAuthMessage(t("message.updateSubscriptionLost") + ": " + error, true);
                }
            }
        );
        updateSubscription.start();
    }

    private void stopUpdateSubscription() {
        if (updateSubscription != null) {
            updateSubscription.close();
            updateSubscription = null;
        }
    }

    private void clearRows() {
        allRows = List.of();
        tableModel.setRows(allRows);
        visualization.setRows(allRows);
        selectedRow = null;
        updateSelection();
    }

    private void runCommand(Command command, java.util.function.Consumer<String> onSuccess) {
        runCommand(command, onSuccess, this::showMessage);
    }

    private void runCommand(
        Command command,
        java.util.function.Consumer<String> onSuccess,
        java.util.function.Consumer<String> onError
    ) {
        worker.submit(() -> {
            try {
                String result = context.commandInvoker().invoke(command, context);
                SwingUtilities.invokeLater(() -> {
                    onSuccess.accept(result);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
            }
        });
    }

    private boolean ensureAuthenticated() {
        if (context.login() != null) {
            return true;
        }
        showMessage(t("message.logInFirst"));
        authenticatedView = false;
        rebuildContent();
        return false;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message == null ? " " : message,
            bundle.getString("app.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showInfoPopup(String message) {
        JTextArea textArea = new JTextArea(message == null ? " " : message, 10, 48);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(
            textArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            t("button.info"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showAuthMessage(String message, boolean error) {
        authMessageLabel.setText(message == null ? " " : message);
        authMessageLabel.setBackground(error ? new Color(254, 226, 226) : new Color(220, 252, 231));
        authMessageLabel.setBorder(BorderFactory.createLineBorder(error ? new Color(220, 38, 38) : new Color(22, 163, 74)));
    }

    private void applyFilterAndSort() {
        String column = Objects.toString(filterColumn.getSelectedItem(), "");
        String value = Objects.toString(filterValue.getSelectedItem(), "")
            .trim()
            .toLowerCase(Locale.ROOT);
        String sort = Objects.toString(sortColumn.getSelectedItem(), "");
        boolean desc = sortDirection.getSelectedIndex() == 1;
        Comparator<MovieRow> comparator = MovieRow.comparator(sort);
        if (desc) {
            comparator = comparator.reversed();
        }
        List<MovieRow> filtered = allRows.stream()
            .filter(row -> value.isBlank() || row.value(column).toLowerCase(Locale.ROOT).contains(value))
            .sorted(comparator)
            .toList();
        tableModel.setRows(filtered);
        visualization.setRows(filtered);
    }

    private void updateFilterValueControl() {
        String column = Objects.toString(filterColumn.getSelectedItem(), "");
        if ("genre".equals(column)) {
            filterValue.setEditable(false);
            filterValue.setModel(enumModel(MovieGenre.values()));
        } else if ("rating".equals(column)) {
            filterValue.setEditable(false);
            filterValue.setModel(enumModel(MpaaRating.values()));
        } else if ("country".equals(column)) {
            filterValue.setEditable(false);
            filterValue.setModel(enumModel(Country.values()));
        } else {
            filterValue.setEditable(true);
            filterValue.setModel(new DefaultComboBoxModel<>(new String[] {""}));
        }
    }

    private <E extends Enum<E>> DefaultComboBoxModel<String> enumModel(E[] values) {
        String[] items = new String[values.length + 1];
        items[0] = "";
        for (int i = 0; i < values.length; i++) {
            items[i + 1] = values[i].name();
        }
        return new DefaultComboBoxModel<>(items);
    }

    private void updateSelection() {
        detailsPanel.removeAll();
        visualization.setSelected(selectedRow);
        if (selectedRow == null) {
            detailsPanel.add(cell("message.noObjectSelected", false));
        } else {
            addDetail("field.key", selectedRow.key());
            addDetail("table.column.owner", selectedRow.owner());
            addDetail("field.genre", selectedRow.genre());
            addDetail("field.coordinates", selectedRow.x() + ", " + selectedRow.y());
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void addDetail(String key, String value) {
        detailsPanel.add(cell(key, true));
        detailsPanel.add(cell(value, false));
    }

    private void openEditor(MovieRow row) {
        MovieEditorDialog dialog = new MovieEditorDialog(this, row, this::t);
        dialog.setVisible(true);
        if (!dialog.saved()) {
            return;
        }
        Movie movie = dialog.movie();
        SharedCommand command = row == null
            ? new InsertCommand(dialog.key(), movie)
            : new UpdateCommand(row.id(), movie);
        runCommand(command, result -> refreshRows());
    }

    private void editSelected() {
        MovieRow row = currentSelectedRow();
        if (row == null) {
            showMessage(t("message.selectObjectFirst"));
            return;
        }
        if (!row.editable()) {
            showMessage(t("message.readOnlyEdit"));
            return;
        }
        openEditor(row);
    }

    private MovieRow currentSelectedRow() {
        int selectedViewRow = table.getSelectedRow();
        if (selectedViewRow >= 0) {
            selectedRow = tableModel.rowAt(table.convertRowIndexToModel(selectedViewRow));
        }
        return selectedRow;
    }

    private void deleteSelected() {
        MovieRow row = currentSelectedRow();
        if (row == null) {
            showMessage(t("message.selectObjectFirst"));
            return;
        }
        if (!row.editable()) {
            showMessage(t("message.readOnlyDelete"));
            return;
        }
        runCommand(new RemoveKeyCommand(row.key()), result -> refreshRows());
    }

    private void relabel() {
        setTitle(bundle.getString("app.title"));
        tableModel.setLocale(currentLocale);
        tableModel.setColumnLabels(translatedTableColumns());
        userLabel.setText(t("status.user") + ": " + Objects.toString(context.login(), "-"));
        connectionLabel.setText(t("status.online"));
        countLabel.setText(t("status.items") + ": " + allRows.size());
        int selectedSortDirection = Math.max(0, sortDirection.getSelectedIndex());
        sortDirection.setModel(new DefaultComboBoxModel<>(new String[] {
            t("sort.ascending"),
            t("sort.descending")
        }));
        sortDirection.setSelectedIndex(selectedSortDirection > 0 ? 1 : 0);
    }

    private String[] translatedTableColumns() {
        return new String[] {
            t("table.column.key"),
            t("table.column.id"),
            t("table.column.name"),
            t("table.column.x"),
            t("table.column.y"),
            t("table.column.genre"),
            t("table.column.oscars"),
            t("table.column.rating"),
            t("table.column.director"),
            t("table.column.weight"),
            t("table.column.country"),
            t("table.column.passportId"),
            t("table.column.locationX"),
            t("table.column.locationY"),
            t("table.column.locationName"),
            t("table.column.owner"),
            t("table.column.created")
        };
    }

    private static final class OwnershipRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean selected,
            boolean focus,
            int row,
            int column
        ) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            MovieTableModel model = (MovieTableModel) table.getModel();
            MovieRow movie = model.rowAt(table.convertRowIndexToModel(row));
            if (!selected) {
                component.setBackground(movie.editable() ? Color.WHITE : new Color(229, 231, 235));
                component.setForeground(movie.editable() ? Color.BLACK : new Color(75, 85, 99));
            }
            return component;
        }
    }

    private static final class SectionPanel extends JPanel {
        private final String title;

        private SectionPanel(String title) {
            this.title = title;
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(28, 0, 0, 0)
            ));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(HEADER);
            g.fillRect(0, 0, getWidth(), 28);
            g.setColor(Color.WHITE);
            g.setFont(getFont().deriveFont(Font.BOLD, 13f));
            g.drawString(title, 12, 19);
            g.dispose();
        }
    }

    private static final class LanguageItem {
        private final String label;
        private final Locale locale;

        private LanguageItem(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
        }

        private Locale locale() {
            return locale;
        }

        private static LanguageItem[] items() {
            return new LanguageItem[] {
                new LanguageItem("English (NZ)", Locale.forLanguageTag("en-NZ")),
                new LanguageItem("Русский", Locale.forLanguageTag("ru-RU")),
                new LanguageItem("Nederlands", Locale.forLanguageTag("nl-NL")),
                new LanguageItem("Lietuviu", Locale.forLanguageTag("lt-LT"))
            };
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof LanguageItem item && locale.equals(item.locale);
        }

        @Override
        public int hashCode() {
            return locale.hashCode();
        }
    }

    private static final class Labels {
        private static ResourceBundle bundle(Locale locale) {
            return ResourceBundle.getBundle("org.gui.Labels", locale);
        }
    }

    private static final class MovieTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
            "key", "id", "name", "x", "y", "genre", "oscars", "rating",
            "director", "weight", "country", "passportId", "locationX", "locationY",
            "locationName", "owner", "created"
        };
        private String[] columnLabels = COLUMNS;
        private List<MovieRow> rows = new ArrayList<>();
        private Locale locale = Locale.forLanguageTag("en-NZ");

        private void setRows(List<MovieRow> rows) {
            this.rows = new ArrayList<>(rows);
            fireTableDataChanged();
        }

        private List<MovieRow> rows() {
            return rows;
        }

        private void setColumnLabels(String[] columnLabels) {
            this.columnLabels = columnLabels.clone();
            fireTableStructureChanged();
        }

        private void setLocale(Locale locale) {
            this.locale = locale;
        }

        private MovieRow rowAt(int row) {
            return rows.get(row);
        }

        private void select(JTable table, MovieRow row) {
            int index = rows.indexOf(row);
            if (index >= 0) {
                table.setRowSelectionInterval(index, index);
            }
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnLabels[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rows.get(rowIndex).displayValue(COLUMNS[columnIndex], locale);
        }
    }

    private record MovieRow(
        String key,
        int id,
        String name,
        int x,
        int y,
        int oscars,
        String genre,
        String rating,
        String owner,
        boolean editable,
        String created,
        String director,
        String weight,
        String country,
        String passportId,
        String locationX,
        String locationY,
        String locationName
    ) {
        private static List<MovieRow> parse(String response) {
            if (response == null || response.isBlank()) {
                return List.of();
            }
            List<MovieRow> rows = new ArrayList<>();
            for (String line : response.split("\\R")) {
                String[] parts = line.split("\t", -1);
                if (parts.length < 18) {
                    continue;
                }
                rows.add(new MovieRow(
                    unescape(parts[0]),
                    Integer.parseInt(parts[1]),
                    unescape(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    unescape(parts[6]),
                    unescape(parts[7]),
                    unescape(parts[8]),
                    Boolean.parseBoolean(parts[9]),
                    unescape(parts[10]),
                    unescape(parts[11]),
                    unescape(parts[12]),
                    unescape(parts[13]),
                    unescape(parts[14]),
                    unescape(parts[15]),
                    unescape(parts[16]),
                    unescape(parts[17])
                ));
            }
            return rows;
        }

        private static String unescape(String value) {
            return value.replace("\\t", "\t").replace("\\n", "\n").replace("\\\\", "\\");
        }

        private String value(String column) {
            return switch (column) {
                case "key" -> key;
                case "id" -> Integer.toString(id);
                case "name" -> name;
                case "x" -> Integer.toString(x);
                case "y" -> Integer.toString(y);
                case "genre" -> genre;
                case "oscars" -> Integer.toString(oscars);
                case "rating" -> rating;
                case "director" -> director;
                case "weight" -> weight;
                case "country" -> country;
                case "passportId" -> passportId;
                case "locationX" -> locationX;
                case "locationY" -> locationY;
                case "locationName" -> locationName;
                case "owner" -> owner;
                case "created" -> created;
                default -> "";
            };
        }

        private String displayValue(String column, Locale locale) {
            return switch (column) {
                case "weight", "locationY" -> formatDouble(value(column), locale);
                case "created" -> formatDate(value(column), locale);
                default -> value(column);
            };
        }

        private static String formatDouble(String value, Locale locale) {
            if (value == null || value.isBlank()) {
                return "";
            }
            try {
                NumberFormat format = NumberFormat.getNumberInstance(locale);
                format.setMaximumFractionDigits(3);
                return format.format(Double.parseDouble(value));
            } catch (NumberFormatException e) {
                return value;
            }
        }

        private static String formatDate(String value, Locale locale) {
            if (value == null || value.isBlank()) {
                return "";
            }
            try {
                LocalDateTime dateTime = LocalDateTime.parse(value);
                return DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                    .withLocale(locale)
                    .format(dateTime);
            } catch (DateTimeParseException e) {
                return value;
            }
        }

        private static Comparator<MovieRow> comparator(String column) {
            return switch (column) {
                case "id" -> Comparator.comparingInt(MovieRow::id);
                case "x" -> Comparator.comparingInt(MovieRow::x);
                case "y" -> Comparator.comparingInt(MovieRow::y);
                case "oscars" -> Comparator.comparingInt(MovieRow::oscars);
                case "weight" -> Comparator.comparingDouble(row -> parseDouble(row.weight()));
                case "locationX" -> Comparator.comparingLong(row -> parseLong(row.locationX()));
                case "locationY" -> Comparator.comparingDouble(row -> parseDouble(row.locationY()));
                default -> Comparator.comparing(row -> row.value(column), String.CASE_INSENSITIVE_ORDER);
            };
        }

        private static double parseDouble(String value) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return Double.NEGATIVE_INFINITY;
            }
        }

        private static long parseLong(String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return Long.MIN_VALUE;
            }
        }
    }

    private static final class VisualizationPanel extends JPanel {
        private List<MovieRow> rows = List.of();
        private MovieRow selected;
        private java.util.function.Consumer<MovieRow> listener = row -> {};
        private float alpha = 1.0f;

        private VisualizationPanel() {
            setBorder(BorderFactory.createLineBorder(BORDER));
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    for (MovieRow row : rows) {
                        if (circle(row).contains(event.getPoint())) {
                            listener.accept(row);
                            return;
                        }
                    }
                }
            });
        }

        private void setRows(List<MovieRow> rows) {
            this.rows = rows;
            alpha = 0.0f;
            Timer timer = new Timer(20, null);
            timer.addActionListener(event -> {
                alpha = Math.min(1.0f, alpha + 0.12f);
                repaint();
                if (alpha >= 1.0f) {
                    timer.stop();
                }
            });
            timer.start();
        }

        private void setSelected(MovieRow selected) {
            this.selected = selected;
            repaint();
        }

        private void addMovieClickListener(java.util.function.Consumer<MovieRow> listener) {
            this.listener = listener;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            for (MovieRow row : rows) {
                java.awt.geom.Ellipse2D circle = circle(row);
                g.setClip(circle);
                paintFlag(g, row.country(), circle);
                g.setClip(null);
                g.setStroke(new BasicStroke(row.equals(selected) ? 4f : 1.5f));
                g.setColor(row.equals(selected) ? new Color(37, 99, 235) : ownerColor(row.owner()));
                g.draw(circle);
                g.setColor(Color.BLACK);
                g.setFont(g.getFont().deriveFont(11f));
                g.drawString(row.name(), (int) circle.getCenterX() - 18, (int) circle.getMaxY() + 14);
            }
            g.dispose();
        }

        private java.awt.geom.Ellipse2D circle(MovieRow row) {
            int size = Math.max(34, Math.min(82, row.oscars() * 8 + 30));
            int x = 30 + Math.floorMod(row.x(), Math.max(1, getWidth() - size - 60));
            int y = 30 + Math.floorMod(row.y(), Math.max(1, getHeight() - size - 70));
            return new java.awt.geom.Ellipse2D.Double(x, y, size, size);
        }

        private Color ownerColor(String owner) {
            int hash = owner == null ? 0 : owner.hashCode();
            float hue = Math.floorMod(hash, 360) / 360f;
            return Color.getHSBColor(hue, 0.68f, 0.72f);
        }

        private void paintFlag(Graphics2D g, String country, java.awt.geom.Ellipse2D circle) {
            int x = (int) circle.getX();
            int y = (int) circle.getY();
            int w = (int) circle.getWidth();
            int h = (int) circle.getHeight();
            if ("ITALY".equals(country)) {
                g.setColor(new Color(0, 146, 70));
                g.fillRect(x, y, w / 3, h);
                g.setColor(Color.WHITE);
                g.fillRect(x + w / 3, y, w / 3, h);
                g.setColor(new Color(206, 43, 55));
                g.fillRect(x + 2 * w / 3, y, w, h);
            } else if ("CHINA".equals(country)) {
                g.setColor(new Color(222, 41, 16));
                g.fillRect(x, y, w, h);
                g.setColor(new Color(255, 222, 0));
                g.fillPolygon(star(x + w * 0.28, y + h * 0.32, w * 0.13, w * 0.055));
            } else {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, w, h);
                g.setColor(new Color(188, 0, 45));
                g.fillOval(x + w / 2 - w / 5, y + h / 2 - h / 5, 2 * w / 5, 2 * h / 5);
            }
        }

        private Polygon star(double centerX, double centerY, double outerRadius, double innerRadius) {
            Polygon polygon = new Polygon();
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                double radius = i % 2 == 0 ? outerRadius : innerRadius;
                polygon.addPoint(
                    (int) Math.round(centerX + Math.cos(angle) * radius),
                    (int) Math.round(centerY + Math.sin(angle) * radius)
                );
            }
            return polygon;
        }
    }

    private static final class MovieEditorDialog extends javax.swing.JDialog {
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
        private Movie movie;
        private boolean saved;

        private final Function<String, String> translator;

        private MovieEditorDialog(JFrame owner, MovieRow row, Function<String, String> translator) {
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
            save.addActionListener(event -> {
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
            });
            delete.setEnabled(false);
            cancel.addActionListener(event -> setVisible(false));
            if (row != null) {
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
            pack();
            setSize(520, 420);
            setLocationRelativeTo(owner);
        }

        private void addField(JPanel panel, String label, JComponent field) {
            JLabel cell = new JLabel(translator.apply(label));
            cell.setOpaque(true);
            cell.setBackground(SOFT);
            cell.setBorder(BorderFactory.createLineBorder(BORDER));
            panel.add(cell);
            panel.add(field);
        }

        private boolean saved() {
            return saved;
        }

        private String key() {
            return key.getText().trim();
        }

        private Movie movie() {
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
}
