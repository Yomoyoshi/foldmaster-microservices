package com.foldmaster.common.util;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final long DEFAULT_PAGE_SIZE = 20;

}