package myconext.exceptions;

public abstract class AbstractBaseException extends RuntimeException {

    public AbstractBaseException(String message) {
        super(message);
    }

    protected boolean suppressStackTrace() {
        return false;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this.suppressStackTrace() ? this : super.fillInStackTrace();
    }
}
