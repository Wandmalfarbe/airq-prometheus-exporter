package de.pascalwagler.airq.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.read.CyclicBufferAppender;
import de.pascalwagler.airq.model.internal.LogEntry;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogService {

    private LogService() {
    }

    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[[0-9;]*m");
    private static final Pattern LOG_PATTERN =
            Pattern.compile("^(?<severity>\\S+)\\s+(?<logger>\\S+)\\s+(?<date>\\S+)\\s+(?<message>.+)$");


    private static String removeAnsiCodes(String abcLog) {
        if (abcLog == null) return null;
        return ANSI_PATTERN.matcher(abcLog).replaceAll("");
    }

    public static List<LogEntry> getLogs() {
        final DateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        CyclicBufferAppender<ILoggingEvent> appender = (CyclicBufferAppender<ILoggingEvent>) rootLogger.getAppender("CyclicBuffer");

        List<LogEntry> logs = new ArrayList<>();
        for (int i = 0; i < appender.getLength(); i++) {
            ILoggingEvent loggingEvent = appender.get(i);

            long timestamp = loggingEvent.getTimeStamp();
            String timestampFormatted = iso8601DateFormat.format(new Date(loggingEvent.getTimeStamp()));
            String level = loggingEvent.getLevel().levelStr;
            String message = loggingEvent.getFormattedMessage();
            IThrowableProxy throwableProxy = loggingEvent.getThrowableProxy();
            String stackTrace = null;
            if (throwableProxy != null) {
                stackTrace = ThrowableProxyUtil.asString(throwableProxy);
            }

            logs.add(LogEntry.builder()
                    .timestamp(timestamp)
                    .timestampFormatted(timestampFormatted)
                    .logger(loggingEvent.getLoggerName())
                    .level(level)
                    .message(message)
                    .stackTrace(stackTrace)
                    .build());
        }
        logs.sort(Comparator.comparingLong(LogEntry::getTimestamp).reversed());
        return logs;
    }

    public static List<LogEntry> mapAirQLogs(String[] airQLogs) {
        List<LogEntry> logs = Arrays.stream(airQLogs).map(airQLog -> {

                    String logWithoutEscapeSeq = removeAnsiCodes(airQLog);
                    Matcher m = LOG_PATTERN.matcher(logWithoutEscapeSeq);

                    String severity = null;
                    String logger = null;
                    String date = null;
                    String message;
                    if (m.matches()) {
                        severity = m.group("severity");
                        logger = m.group("logger");
                        date = m.group("date");
                        message = m.group("message");
                    } else {
                        message = logWithoutEscapeSeq;
                    }

                    return LogEntry.builder()
                            .timestampFormatted(date)
                            .logger(logger)
                            .level(severity)
                            .message(message)
                            .build();
                })
                .collect(Collectors.toList());
        Collections.reverse(logs);
        return logs;
    }
}
