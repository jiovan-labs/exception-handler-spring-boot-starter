/*
 * Copyright (c) 2023 Jiovan Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiovan.spring.starter.exceptionhandler.util;


import java.security.GeneralSecurityException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.jiovan.plexus.common.toolkit.exception.BusinessValidationException;
import com.jiovan.plexus.common.toolkit.exception.IgnorableException;
import com.jiovan.plexus.common.toolkit.exception.LawProhibitedException;
import com.jiovan.plexus.common.toolkit.exception.MaliciousAccessException;
import com.jiovan.plexus.common.toolkit.exception.ServerBusyException;
import com.jiovan.plexus.common.toolkit.exception.ServerMaintenanceException;
import com.jiovan.plexus.common.toolkit.exception.ServiceConfigException;
import com.jiovan.plexus.common.toolkit.exception.ServiceUnavailableException;
import com.jiovan.plexus.common.toolkit.exception.ServiceVersionException;
import com.jiovan.plexus.spring.toolkit.util.ClassUtilsWraps;
import com.jiovan.plexus.spring.toolkit.util.ErrorControllerWraps;
import com.jiovan.plexus.spring.toolkit.util.WebUtilsWraps;
import com.jiovan.spring.starter.exceptionhandler.filter.FilterExceptionHandlerFilter;


/**
 * Utilities for {@link org.springframework.boot.webmvc.error.ErrorController}
 *
 * @author David Hsing
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted", "UnusedReturnValue"})
public abstract class ErrorControllerUtils {
    /**
     * Returns the HTTP status appropriate for the error cause carried by the given request
     *
     * @param request the current servlet request used to read the recorded throwable attribute
     * @param status the HTTP status originally associated with the error, may be {@code null}
     * @param cause the cause of the error, may be {@code null} to read it from the request
     *
     * @return the resolved {@link org.springframework.http.HttpStatusCode} for the error
     */
    @SuppressWarnings("deprecation")
    public static HttpStatusCode determineErrorStatus(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause) {
        Throwable rootCause;
        if (cause == null) {
            rootCause = WebUtilsWraps.getRequestAttributeAs(request, FilterExceptionHandlerFilter.THROWABLE_ATTRIBUTE, Throwable.class);
        } else {
            rootCause = NestedExceptionUtils.getMostSpecificCause(cause);
        }
        if (rootCause != null) {
            if (rootCause instanceof IgnorableException) {
                return HttpStatus.OK;
            } else if (rootCause instanceof BindException || rootCause instanceof ValidationException || rootCause instanceof BusinessValidationException) {
                return HttpStatus.BAD_REQUEST;
            } else if (rootCause instanceof GeneralSecurityException || ClassUtilsWraps.isAssignableValue("org.springframework.security.core.AuthenticationException", rootCause)) {    // $NON-NLS-1$
                return HttpStatus.FORBIDDEN;
            } else if (rootCause instanceof NoResourceFoundException) {
                return HttpStatus.NOT_FOUND;
            } else if (rootCause instanceof MaliciousAccessException) {
                return HttpStatus.I_AM_A_TEAPOT;
            } else if (rootCause instanceof ServerBusyException || ClassUtilsWraps.isAssignableValue("com.jiovan.spring.starter.ratelimit.exception.RateLimitedException", rootCause)) {    // $NON-NLS-1$
                return HttpStatus.TOO_MANY_REQUESTS;
            } else if (rootCause instanceof LawProhibitedException) {
                return HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS;
            } else if (rootCause instanceof ServerMaintenanceException || rootCause instanceof ServiceUnavailableException) {
                return HttpStatus.SERVICE_UNAVAILABLE;
            } else if (rootCause instanceof ServiceVersionException) {
                return HttpStatus.HTTP_VERSION_NOT_SUPPORTED;
            } else if (rootCause instanceof ServiceConfigException) {
                return HttpStatus.VARIANT_ALSO_NEGOTIATES;
            } else if (ClassUtilsWraps.isAssignableValue("com.jiovan.spring.starter.licensevalidation.exception.AbstractLicenseValidatorException", rootCause) || ClassUtilsWraps.isAssignableValue("global.namespace.truelicense.api.LicenseManagementException", rootCause)) {    // $NON-NLS-1$ // $NON-NLS-2$
                return HttpStatus.PAYMENT_REQUIRED;
            }
        }
        return (status != null) ? status : ErrorControllerWraps.getErrorStatus(request);
    }
}
