package myconext.cron;

@FunctionalInterface
public interface Executable {

        void execute() throws Throwable;

}
