package io.keystash.models.authentication.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * A User Info object containing authentication claims about an end-user
 * <p>
 *     There are standard claims specified in OpenID Connect that might be present in an instance of this class
 *     @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.5.1">
 *         http://openid.net/specs/openid-connect-core-1_0.html#rfc.section.5.1</a>
 * </p>
 * <p>
 *     Additionally, there might be non-standard claims that are unique to this implementation of OpenID Connect
 * </p>
 */
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {

    /**
     * A non standard claim specifying the internal user_id of the end-user
     */
    @JsonProperty("user_id")
    @Getter @Setter private Integer userId;

    /**
     * Subject value; unique identifier for the end-user
     */
    @JsonProperty("sub")
    @Getter @Setter private String sub;

    /**
     * The full name of the end-user, including titles and suffixes
     */
    @JsonProperty("name")
    @Getter @Setter private String name;

    /**
     * The given name(s) or first name(s) of the end-user
     */
    @JsonProperty("given_name")
    @Getter @Setter private String givenName;

    /**
     * The surname(s) or last name(s) of the end-user
     */
    @JsonProperty("family_name")
    @Getter @Setter private String familyName;

    /**
     * The middle name(s) of the end-user
     */
    @JsonProperty("middle_name")
    @Getter @Setter private String middleName;

    /**
     * The nickname of casual name of the end-user
     */
    @JsonProperty("nickname")
    @Getter @Setter private String nickname;

    /**
     * The username or shorthand name that the end-user is referred to
     */
    @JsonProperty("preferred_username")
    @Getter @Setter private String preferredUsername;

    /**
     * The URL of the end-user's profile page
     */
    @JsonProperty("profile")
    @Getter @Setter private String profile;

    /**
     * The URL of the end-user's profile picture - The URL should refer to an image file
     */
    @JsonProperty("picture")
    @Getter @Setter private String picture;

    /**
     * The URL of the end-user's web page or blog
     */
    @JsonProperty("website")
    @Getter @Setter private String website;

    /**
     * The preferred email address of the end-user
     */
    @JsonProperty("email")
    @Getter @Setter private String email;

    /**
     * Whether or not the end-user's email address has been verified. True if verified, false otherwise
     */
    @JsonProperty("email_verified")
    @Getter @Setter private Boolean emailVerified;

    /**
     * The gender of the end-user
     * <p>Note: Although male and female are defined by the spec, this is not a binary value</p>
     */
    @JsonProperty("gender")
    @Getter @Setter private String gender;

    /**
     * The birthday of the end-user, represented in the format YYYY-MM-DD
     */
    @JsonProperty("birthdate")
    @Getter private String birthdate;

    /**
     * The timezone of the end-user
     */
    @JsonProperty("zoneinfo")
    @Getter @Setter private String zoneinfo;

    /**
     * The language and two letter country code, separated by a dash, of the end-user
     */
    @JsonProperty("locale")
    @Getter @Setter private String locale;

    /**
     * The phone number of the end-user
     */
    @JsonProperty("phone_number")
    @Getter @Setter private String phoneNumber;

    /**
     * Whether or not the end-user's phone number has been verified. True if verified, false otherwise
     */
    @JsonProperty("phone_number_verified")
    @Getter @Setter private Boolean phoneNumberVerified;

    /**
     * The preferred postal address of the end-user
     */
    @JsonProperty("address")
    @Getter @Setter private Address address;

    /**
     * The last time the end-user's info was updated, represented as a number of seconds since 1970-01-01T0:0:0Z UTC
     */
    @JsonProperty("updated_at")
    @Getter @Setter private Long updatedAt;

    /**
     * Set the birthday of the end-user
     * @param date the date to set
     */
    public void setBirthdate(Date date) {
        LocalDate localDate = date.toInstant().atOffset(ZoneOffset.UTC).toLocalDate();
        birthdate = localDate.format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * An object representing the address claim for an end-user
     */
    @ToString
    @EqualsAndHashCode
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Address {

        /**
         * The full street address of the end-user, including number, street name, etc
         */
        @JsonProperty("street_address")
        @Getter @Setter private String streetAddress;

        /**
         * The city or locality of the end-user
         */
        @JsonProperty("locality")
        @Getter @Setter private String locality;

        /**
         * The state, province, or region of the end-user
         */
        @JsonProperty("region")
        @Getter @Setter private String region;

        /**
         * The Zip or postal code of the end-user
         */
        @JsonProperty("postal_code")
        @Getter @Setter private String postalCode;

        /**
         * The country of the end-user
         */
        @JsonProperty("country")
        @Getter @Setter private String country;

        public String formatted() {
            StringBuilder builder = new StringBuilder();

            if (!StringUtils.isEmpty(streetAddress)) {
                builder.append(streetAddress).append("\n");
            }

            if (!StringUtils.isEmpty(locality)) {
                builder.append(locality).append(" ");
            }

            if (!StringUtils.isEmpty(region)) {
                builder.append(region).append(" ");
            }

            if (!StringUtils.isEmpty(country)) {
                builder.append(country);
            }

            if (!StringUtils.isEmpty(postalCode)) {
                builder.append("\n").append(postalCode);
            }

            return builder.length() > 0 ? builder.toString() : null;
        }

    }
}
