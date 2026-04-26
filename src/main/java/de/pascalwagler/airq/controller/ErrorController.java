package de.pascalwagler.airq.controller;

import de.pascalwagler.airq.model.internal.ErrorInformation;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.javalin.rendering.template.TemplateUtil.model;

@Slf4j
@RequiredArgsConstructor
public class ErrorController {

    private static final DateTimeFormatter ISO8601_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public void onException(Exception exception, Context ctx) {
        try {
            log.error("Uncaught exception", exception);

            ErrorInformation errorInformation = ErrorInformation.builder()
                    .url(ctx.url())
                    .time(ZonedDateTime.now().format(ISO8601_FORMATTER))
                    .clazz(exception.getClass().getCanonicalName())
                    .message(exception.getMessage())
                    .causeClazz(exception.getCause() != null ? exception.getCause().getClass().getCanonicalName() : null)
                    .causeMessage(exception.getCause() != null ? exception.getCause().getMessage() : null)
                    .stackTrace(stackTraceToString(exception))
                    .build();

            ctx.render("/error.html", model("errorInformation", errorInformation))
                    .status(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) { // Prevent infinite recursion
            log.error("An Exception occurred while showing the error page.", ex);
            ctx.html("An Exception occurred while showing the error page. Please see the log file for more information.");
        }
    }

    private String stackTraceToString(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
