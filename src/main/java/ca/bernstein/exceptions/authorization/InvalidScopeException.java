package ca.bernstein.exceptions.authorization;

public class InvalidScopeException extends AuthorizationException {

    private final String scope;

    public InvalidScopeException(String message, String scope) {
        super(message);
        this.scope = scope;
    }

    public InvalidScopeException(String message, Throwable cause, String scope) {
        super(message, cause);
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }
}
