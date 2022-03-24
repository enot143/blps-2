package coursera.exceptions;

import org.springframework.http.HttpStatus;

public class TestException extends Throwable {
    String message;
    public TestException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
