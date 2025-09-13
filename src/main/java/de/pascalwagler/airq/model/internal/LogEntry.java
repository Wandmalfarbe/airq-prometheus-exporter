package de.pascalwagler.airq.model.internal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogEntry {
    long timestamp;
    String timestampFormatted;
    String logger;
    String level;
    String message;
    String stackTrace;
}
