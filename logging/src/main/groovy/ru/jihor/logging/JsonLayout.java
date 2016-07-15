package ru.jihor.logging;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Setter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 *
 *
 * @author jihor (jihor@ya.ru)
 * Created on 2016-07-15
 */

@Setter
public class JsonLayout extends LayoutBase<ILoggingEvent> {
    protected boolean includeMDC = false;
    protected boolean includeMessage = true;
    protected boolean includeException = true;

    private final ThrowableProxyConverter throwableProxyConverter = new ThrowableProxyConverter();
    private static final ObjectWriter objectWriter = new ObjectMapper().writer();
    private static final Predicate<String> NON_EMPTY_STRING = ((Predicate<String>) String::isEmpty).negate();

    public JsonLayout() {
        super();
        throwableProxyConverter.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        throwableProxyConverter.stop();
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        try {
            return objectWriter.writeValueAsString(eventToMap(event));
        } catch (IOException e) {
            return String.format("{\"Message\":\"Exception occurred on creating log for message [%s], exception was [%s]\"}",
                                 throwableProxyConverter.convert(event),
                                 e.getMessage());
        }
    }

    protected Map eventToMap(ILoggingEvent event) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (includeMDC) event.getMDCPropertyMap().forEach(map::put);
        if (includeMessage) map.put("Message", event.getFormattedMessage());
        Optional.ofNullable(includeException ? throwableProxyConverter.convert(event) : null)
                .filter(NON_EMPTY_STRING)
                .ifPresent(e -> map.put("Exception", e));

        return map;
    }
}