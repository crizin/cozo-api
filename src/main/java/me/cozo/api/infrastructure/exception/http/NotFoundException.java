package me.cozo.api.infrastructure.exception.http;

import me.cozo.api.infrastructure.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends ServiceException {
}
