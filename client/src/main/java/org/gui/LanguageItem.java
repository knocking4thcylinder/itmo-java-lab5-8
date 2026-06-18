package org.gui;

import java.util.Locale;

final class LanguageItem {
    private final String label;
    private final Locale locale;

    LanguageItem(String label, Locale locale) {
        this.label = label;
        this.locale = locale;
    }

    Locale locale() {
        return locale;
    }

    static LanguageItem[] items() {
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
