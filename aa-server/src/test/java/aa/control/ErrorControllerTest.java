package aa.control;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ErrorControllerTest {

  private ErrorController subject;
  private ErrorAttributes errorAttributes;

  @Before
  public void before() {
    this.errorAttributes = mock(ErrorAttributes.class);

    Map<String, Object> result = new HashMap<>();
    result.put("exception", "exception");
    result.put("message", "message");

    when(errorAttributes.getErrorAttributes(any(), anyBoolean())).thenReturn(result);

    this.subject = new ErrorController(errorAttributes);

    MockEnvironment environment = new MockEnvironment();
    environment.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "dev, aa-test");
    ReflectionTestUtils.setField(this.subject, "environment", environment);
  }

  @Test
  public void testErrorWithBadInput() throws Exception {
    HttpServletRequest request = new MockHttpServletRequest();

    BindingResult bindingResult = new MapBindingResult(new HashMap<>(), "serviceProvider");
    bindingResult.addError(new FieldError("serviceProvider", "entityId", "required"));

    when(errorAttributes.getError(any())).thenReturn(new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult));

    assertResponse(request, INTERNAL_SERVER_ERROR, "{entityId=required}");
  }

  private void assertResponse(HttpServletRequest request, HttpStatus httpStatus, String expectedBodyResponse) {
    ResponseEntity<Map<String, Object>> response = subject.error(request);

    assertEquals(httpStatus, response.getStatusCode());

    Map<String, Object> body = response.getBody();
    //there were details, so we don't expect the 'exception' and 'message' still in here
    assertFalse(body.containsKey("exception"));
    assertFalse(body.containsKey("message"));

    assertEquals("dev, aa-test", body.get("profiles"));

    assertEquals(expectedBodyResponse, body.get("details").toString());
  }

}