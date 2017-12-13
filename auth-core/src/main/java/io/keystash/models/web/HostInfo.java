package io.keystash.models.web;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class HostInfo {

    @Getter @Setter private String hostName;
    @Getter @Setter private String baseUrl;

}
