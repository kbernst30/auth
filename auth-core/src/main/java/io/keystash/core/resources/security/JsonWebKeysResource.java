package io.keystash.core.resources.security;

import io.keystash.common.models.jose.*;
import io.keystash.core.factories.jose.JwkFactory;
import io.keystash.core.services.jose.KeyManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resource for discovery of public RSA keys, represented as a JSON Web Key Set
 * <p>
 *     Clients will need public keys to verify authenticity of ID tokens that are issued by this authorization server
 * </p>
 * <p>
 *     This is ONLY for discovery of public keys. No Symmetric keys will be published from this endpoint as secrets
 *     should not be publicly accessible.
 * </p>
 */
@Path("/jwks")
public class JsonWebKeysResource {

    private final JwkFactory jwkFactory;
    private final KeyManager keyManager;

    @Inject
    public JsonWebKeysResource(JwkFactory jwkFactory, KeyManager keyManager) {
        this.jwkFactory = jwkFactory;
        this.keyManager = keyManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJsonWebKeys() {
        SigningKey activeKey = keyManager.getActiveKey();
        Set<SigningKey> passiveKeys = keyManager.getPassiveKeys();

        Set<JsonWebKey> jsonWebKeys = passiveKeys.stream()
                .map(jwkFactory::createJsonWebKey)
                .collect(Collectors.toSet());

        if (activeKey != null) {
            jsonWebKeys.add(jwkFactory.createJsonWebKey(activeKey));
        }

        JwkSet jwkSet = new JwkSet();
        jwkSet.setKeys(jsonWebKeys.stream().filter(key -> key.getKeyType() == JwaKeyType.RSA).collect(Collectors.toSet()));
        return Response.ok(jwkSet).build();
    }

}
