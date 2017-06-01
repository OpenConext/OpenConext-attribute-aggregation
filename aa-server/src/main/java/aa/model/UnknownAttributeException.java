package aa.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UnknownAttributeException extends RuntimeException {
    public UnknownAttributeException(String message) {
        super(message);
    }
}
