package de.pascalwagler.airq.model.internal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorInformation {
    private String url;
    private String time;
    private String clazz;
    private String message;
    private String causeClazz;
    private String causeMessage;
    private String stackTrace;
}
