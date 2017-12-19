import uuidv4 from 'uuid/v4';
import sha256 from 'sha256';

const ACCESS_TOKEN_KEY = "at";
const ID_TOKEN_KEY = "idt";

class Keystash {

    constructor(config) {
        this.authorizationUrl = config.authorizationUrl;
        this.clientId = config.clientId;
        this.redirectUrl = config.redirectUrl;
        this.discoveryUrl = config.discoveryUrl;
    }

    isAuthenticated() {
        return !!localStorage.getItem(ID_TOKEN_KEY);
    }

    isAuthorized() {
        return !!localStorage.getItem(ACCESS_TOKEN_KEY);
    }

    authenticationIsExpired() {
        if (this.isAuthenticated()) {
            let idToken = localStorage.getItem(ID_TOKEN_KEY),
                jwtIdTokenParts = idToken.split(".");

            if (jwtIdTokenParts.length !== 3) {
                return true;
            }

            let idTokenClaims = JSON.parse(atob(jwtIdTokenParts[1]));

            return (idTokenClaims["exp"] * 1000) < new Date();
        }

        return true;
    }

    authorizationIsExpired() {
        if (this.isAuthenticated()) {
            let accessToken = localStorage.getItem(ACCESS_TOKEN_KEY),
                accessTokenParts = accessToken.split(".");

            if (accessTokenParts.length !== 3) {
                return true;
            }

            let accessTokenClaims = JSON.parse(atob(accessTokenParts[1]));

            return (accessTokenClaims["exp"] * 1000) < new Date();
        }

        return true;
    }

    login() {
        window.location.href = this._getUrlForLogin();
    }

    logout() {

    }

    refreshAuth() {
        // TODO this is for a silent refresh where we need new tokens without leaving the page
        // TODO before doing a refresh, we should check that we are still logged in to the auth server
    }

    processAuth() {
        let uriFragment = window.location.hash && window.location.hash.substring(1),
            fragmentPieces = uriFragment.split('&'),
            uriQuery = window.location.search && window.location.search.substring(1),
            queryParamsArray = uriQuery.split('&');

        let authResponse = {};
        for (let i = 0; i < fragmentPieces.length; i++) {
            let piece = fragmentPieces[i].split('=');
            if (piece.length === 2) {
                authResponse[piece[0]] = piece[1];
            }
        }

        for (let i = 0; i < queryParamsArray.length; i++) {
            let piece = queryParamsArray[i].split('=');
            if (piece.length === 2) {
                authResponse[piece[0]] = piece[1];
            }
        }

        let accessToken = authResponse["access_token"],
            idToken = authResponse["id_token"],
            sessionState = authResponse["session_state"],
            error = authResponse["error"],
            errorDescription = authResponse["error_description"];

        const resolveAuthProcessing = (errorMsg) => {
            if (accessToken && !errorMsg) {
                localStorage.setItem("at", decodeURIComponent(accessToken));
            }

            if (idToken && !errorMsg) {
                localStorage.setItem("idt", decodeURIComponent(idToken));
            }

            if (sessionState && !errorMsg) {
                localStorage.setItem("ss", decodeURIComponent(sessionState));
            }

            if (authResponse['refresh'] && authResponse['refresh'] !== 'false' && !errorMsg) {
                window.top.location.reload();
            } else {
                window.top.location.href = "/index.html" + (errorMsg ? "?error=" + decodeURIComponent(errorMsg) : "");
            }
        };

        if (error || errorDescription || (!accessToken && !idToken)) {
            resolveAuthProcessing(errorDescription);
        } else {
            let ths = this;
            fetch(this.discoveryUrl)
                .then(res => res.json())
                .then(serverConfig => ths._verifyAuth(accessToken, idToken, serverConfig))
                .then(verified => resolveAuthProcessing(!verified && "The sign on could not be verified. Please contact the server administrator."));
        }
    }

    _getUrlForLogin() {
        let url = this.authorizationUrl;

        if (url.substring(url.length - 1) !== "/") {
            url += "/?";
        }

        url += "client_id=" + encodeURIComponent(this.clientId) +
            "&redirect_uri=" + encodeURIComponent(this.redirectUrl) +
            "&response_type=token%20id_token" +
            "&scope=openid" +
            "&state=" + encodeURIComponent(this._generateUuid()) +
            "&nonce=" + encodeURIComponent(this._generateUuid());

        return url;
    }

    _generateUuid() {
        return uuidv4();
    }

    _verifyAuth(accessToken, idToken, serverConfig) {
        let isAccessTokenValid = this._verifyAccessToken(accessToken, idToken),
            isIdTokenValid = this._verifyIdToken(idToken, serverConfig);

        return isAccessTokenValid && isIdTokenValid;
    }

    _verifyIdToken(idToken, serverConfig) {
        let jwtIdTokenParts = idToken.split(".");

        if (jwtIdTokenParts.length !== 3) {
            return false;
        }

        // Validate the claims
        let idTokenClaims = JSON.parse(atob(jwtIdTokenParts[1])),
            idTokenJose = JSON.parse(atob(jwtIdTokenParts[0]));

        // Issuer of token is issuer specified in discovery
        if (idTokenClaims["iss"] !== serverConfig["issuer"]) {
            return false;
        }

        // The client ID is a valid audience
        if (idTokenClaims["aud"] !== this.clientId) {
            return false;
        }

        // The Signing algorithm of the token is supported by the server
        if (serverConfig["id_token_encryption_alg_values_supported"].indexOf(idTokenJose["alg"]) === -1) {
            return false;
        }

        // The exp is in the future
        if ((idTokenClaims["exp"] * 1000) < new Date()) {
            return false;
        }

        // TODO Nonce

        return true;
    }

    _verifyAccessToken(accessToken, idToken) {
        let jwtAccessTokenParts = accessToken.split("."),
            jwtIdTokenParts = idToken.split(".");

        // Valid JWT must have 3 parts
        if (jwtAccessTokenParts.length !== 3) {
            return false;
        }

        if (jwtIdTokenParts.length !== 3) {
            return false;
        }

        // Validate token against at_hash in idToken
        let idTokenClaims = JSON.parse(atob(jwtIdTokenParts[1])),
            accessTokenClaims = JSON.parse(atob(jwtAccessTokenParts[1]));

        let hashedAccessToken = sha256(accessToken, { asBytes: "true"}),
            atHashLeftHalf = hashedAccessToken.splice(0, hashedAccessToken.length / 2),
            encodedAtHash = btoa(String.fromCharCode.apply(null, new Uint8Array(atHashLeftHalf)));

        if (idTokenClaims["at_hash"] !== encodedAtHash) {
            return false;
        }

        // The exp is in the future
        if ((accessTokenClaims["exp"] * 1000) < new Date()) {
            return false;
        }

        return true;
    }
}

const keystash = new Keystash({
    clientId: "my-client",
    redirectUrl: "http://localhost:7000?authorizing=true",
    authorizationUrl: "http://localhost:8080/auth/oauth/authorize",
    discoveryUrl: "http://localhost:8080/auth/.well-known/openid-configuration"
});

export default keystash;