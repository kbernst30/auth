package io.keystash.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthorizationUtils {

    /**
     * Returns a set of strings representing the scopes found in the string parameter
     * @param scope a comma separated string of scopes
     * @return a set of scope strings
     */
    public static Set<String> getScopes(String scope) {
        Set<String> requestedScopes = null;
        if (!StringUtils.isEmpty(scope)) {
            requestedScopes = Stream.of(scope.split(",")).collect(Collectors.toSet());
        }

        return requestedScopes;
    }

}
