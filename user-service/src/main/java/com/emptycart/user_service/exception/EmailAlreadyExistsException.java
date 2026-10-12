package com.emptycart.user_service.exception;

import com.emptycart.platform.commons.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email);
    }
}
