package de.pascalwagler.airq.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class HighlightingCompositeConverterCustom extends ForegroundCompositeConverterBase<ILoggingEvent> {
    public static final String BRIGHT_WHITE_FG = "97";

    public static final String BRIGHT_BLACK_BG = "100";
    public static final String BRIGHT_RED_BG = "101";
    public static final String BRIGHT_YELLOW_BG = "103";
    public static final String BRIGHT_BLUE_BG = "104";

    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        switch (level.toInt()) {
            case Level.ERROR_INT:
                return BRIGHT_RED_BG + ";" + BRIGHT_WHITE_FG;
            case Level.WARN_INT:
                return BRIGHT_YELLOW_BG + ";" + BRIGHT_WHITE_FG;
            case Level.INFO_INT:
                return BRIGHT_BLUE_BG + ";" + BRIGHT_WHITE_FG;
            default:
                return BRIGHT_BLACK_BG + ";" + BRIGHT_WHITE_FG;
        }
    }
}
