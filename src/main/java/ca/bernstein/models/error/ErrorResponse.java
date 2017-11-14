package ca.bernstein.models.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * A representation of an error response that can occur
 */
@ToString
public class ErrorResponse implements Serializable {

    /**
     * The type of error that occurred
     */
    @Getter @Setter private String error;

    /**
     * A friendly message describing the error
     */
    @JsonProperty("error_description")
    @Getter @Setter private String errorDescription;

    public ErrorResponse() {}

    public ErrorResponse(ErrorType.AbstractError errorType, String... arguments) {
        this.error = errorType.getError();
        this.errorDescription = String.format(errorType.getMessage(), arguments);
    }

}
