package ca.bernstein.models.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * A representation of an error response that can occur
 */
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonIgnore
    public String getAsUrlEncodedFormParams() {
        StringBuilder builder = new StringBuilder();

        builder.append("error=");

        if (!StringUtils.isEmpty(error)) {
            builder.append(error);
        }

        builder.append("&error_description=");

        if (!StringUtils.isEmpty(errorDescription)) {
            builder.append(errorDescription);
        }

        return builder.toString();
    }
}
