package andreblanke.maven.plugins;

import java.util.Locale;

enum MissingSymbolAction {

    ERROR,
    INCLUDE,
    IGNORE,
    WARN;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
