package myconext.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import myconext.exceptions.ExpiredAuthenticationException;
import myconext.exceptions.MigrationDuplicateUserEmailException;

import java.util.Arrays;
import java.util.List;

public class CustomThresholdFilter extends ThresholdFilter {

    private List<Class> ignoreClasses = Arrays.asList(
            MigrationDuplicateUserEmailException.class,
            ExpiredAuthenticationException.class
    );

    @Override
    public FilterReply decide(ILoggingEvent event) {
        FilterReply decide = super.decide(event);
        if (decide.equals(FilterReply.NEUTRAL)) {
            IThrowableProxy throwableProxy = event.getThrowableProxy();
            if (throwableProxy == null) {
                return FilterReply.NEUTRAL;
            }

            if (!(throwableProxy instanceof ThrowableProxy)) {
                return FilterReply.NEUTRAL;
            }

            ThrowableProxy throwableProxyImpl = (ThrowableProxy) throwableProxy;
            Throwable throwable = throwableProxyImpl.getThrowable();
            if (ignoreClasses.contains(throwable.getClass())) {
                return FilterReply.DENY;
            }
        }
        return decide;
    }
}
