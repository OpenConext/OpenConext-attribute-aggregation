package aa.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class SchemaNotFoundException extends RuntimeException {

  public SchemaNotFoundException(String message) {
    super(message);
  }
}
