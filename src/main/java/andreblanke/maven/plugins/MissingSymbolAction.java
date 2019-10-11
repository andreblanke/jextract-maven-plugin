package andreblanke.maven.plugins;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

enum MissingSymbolAction {

    ERROR,
    INCLUDE,
    IGNORE,
    WARN;

    @NotNull
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
