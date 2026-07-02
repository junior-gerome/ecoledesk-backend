package com.school.attendance.adapter.out.school;

import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

final class SchoolApiRequestAuth {
    private SchoolApiRequestAuth() {
    }

    static void applyBearerToken(HttpHeaders headers) {
        String authorization = currentAuthorizationHeader();
        if (authorization != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
    }

    private static String currentAuthorizationHeader() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }
        return servletAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
}
