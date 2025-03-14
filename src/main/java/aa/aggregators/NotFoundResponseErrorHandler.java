package aa.aggregators;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class NotFoundResponseErrorHandler extends DefaultResponseErrorHandler {
    @Override
    protected boolean hasError(HttpStatus statusCode) {
        return super.hasError(statusCode) && statusCode.value() != 404;
    }
}
