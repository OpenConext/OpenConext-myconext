package myconext.mongo;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.Instant;
import java.util.Date;

@ReadingConverter
public class DateConverter implements Converter<String, Date> {

    @Override
    public Date convert(String s) {
        return Date.from(Instant.parse(s));
    }
}
