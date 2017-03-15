package unicon.matthews.dataloader.io.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Deserializes an ISO Date Time which may contain fractions of a second but no time zone, and converts it to an Instant
 * based on UTC.
 * <p>
 * Similar to <a href="https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_LOCAL_TIME">DateTimeFormatter.ISO_LOCAL_TIME</a>.
 * There is no standard for Date Time with fraction of second values (0-6 fraction digits after decimal).</p>
 */
public class IsoDateTimeWithOptionalFractionOfSecondDeserializer extends JsonDeserializer<Instant> {

    DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 6, true).toFormatter();

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return LocalDateTime.parse(parser.getText(), formatter).atZone(ZoneOffset.UTC).toInstant();
    }
}
