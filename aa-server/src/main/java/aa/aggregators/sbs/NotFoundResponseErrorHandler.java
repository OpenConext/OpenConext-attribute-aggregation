package aa.aggregators.sbs;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;

class NotFoundResponseErrorHandler extends DefaultResponseErrorHandler {
    @Override
    protected boolean hasError(HttpStatus statusCode) {
        return super.hasError(statusCode) && statusCode.value() != 404;
    }
}
