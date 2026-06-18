package org.gui;

import java.util.Locale;
import java.util.ResourceBundle;

final class Labels {
    private Labels() {}

    static ResourceBundle bundle(Locale locale) {
        return ResourceBundle.getBundle("org.gui.Labels", locale);
    }
}
