package aa.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

  @Autowired
  private Environment environment;

  private final ErrorAttributes errorAttributes;

  @Autowired
  public ErrorController(ErrorAttributes errorAttributes) {
    Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
    this.errorAttributes = errorAttributes;
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }

  @RequestMapping("/error")
  public ResponseEntity<Map<String, Object>> error(HttpServletRequest aRequest) {
    RequestAttributes requestAttributes = new ServletRequestAttributes(aRequest);
    Map<String, Object> result = this.errorAttributes.getErrorAttributes(requestAttributes, false);

    Throwable error = this.errorAttributes.getError(requestAttributes);
    if (error instanceof MethodArgumentNotValidException) {
      BindingResult bindingResult = ((MethodArgumentNotValidException) error).getBindingResult();
      if (bindingResult.hasErrors()) {
        Map<String, String> details = bindingResult.getAllErrors().stream().filter(e -> e instanceof FieldError)
            .map(e -> (FieldError) e).collect(toMap(FieldError::getField, FieldError::getDefaultMessage));
        result.put("details", details);
      }
    }
    if (result.containsKey("details")) {
      result.remove("exception");
      result.remove("message");
    }
    result.put("profiles", String.join(", ", environment.getActiveProfiles()));
    HttpStatus statusCode = INTERNAL_SERVER_ERROR;
    if (error != null) {
      ResponseStatus annotation = AnnotationUtils.getAnnotation(error.getClass(), ResponseStatus.class);
      //https://github.com/spring-projects/spring-boot/issues/3057
      statusCode = annotation != null ? annotation.value() : statusCode;
    }
    return new ResponseEntity<>(result, statusCode);
  }

}
