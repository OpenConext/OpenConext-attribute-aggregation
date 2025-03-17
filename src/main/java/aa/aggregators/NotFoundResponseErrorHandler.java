package aa.aggregators;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.DefaultResponseErrorHandler;

public class NotFoundResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected boolean hasError(HttpStatusCode statusCode) {
        return super.hasError(statusCode) && statusCode.value() != 404;
    }
}
