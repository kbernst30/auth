import uuidv4 from 'uuid/v4';
import shajs from 'sha.js';

const ACCESS_TOKEN_KEY = "at";
const ID_TOKEN_KEY = "idt";

class Keystash {

    constructor(config) {
        this.authorizationUrl = config.authorizationUrl;
        this.clientId = config.clientId;
        this.redirectUrl = config.redirectUrl;
    }

    isAuthenticated() {
        return !!localStorage.getItem(ID_TOKEN_KEY);
    }

    isAuthorized() {
        this._verifyAuth(localStorage.getItem(ACCESS_TOKEN_KEY), localStorage.getItem(ID_TOKEN_KEY));
        return !!localStorage.getItem(ACCESS_TOKEN_KEY);
    }

    authenticationIsExpired() {
        // TODO implement
        return false;
    }

    authorizationIsExpired() {
        // todo implement
        return false;
    }

    login() {
        window.location.href = this._getUrlForLogin();
    }

    logout() {

    }

    refreshAuth() {
        // TODO before doing a refresh, we need to ensure that we are still logged in to the auth server
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
            error = authResponse["error"],
            errorDescription = authResponse["error_description"];

        if (error || errorDescription || (!accessToken && !idToken)) {
            // TODO figure out how we want to display the error
            return false;
        }

        return this._verifyAuth(accessToken, idToken);
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

    _verifyAuth(accessToken, idToken) {
        let isAccessTokenValid = this._verifyAccessToken(accessToken, idToken);
    }

    _verifyIdToken(idToken) {

    }

    _verifyAccessToken(accessToken, idToken) {
        // TODO finish this
        let jwtAccessTokenParts = accessToken.split("."),
            jwtIdTokenParts = idToken.split(".");

        // Valid JWT must have 3 parts
        if (jwtAccessTokenParts.length !== 3) {
            return false;
        }

        if (jwtIdTokenParts.length !== 3) {
            return false;
        }

        let idTokenClaims = JSON.parse(atob(jwtIdTokenParts[1]));

        // TODO check algorithm in id token
        let hashedAccessToken = shajs('sha256').update(accessToken).digest('hex'),
            atHashLeftHalf = hashedAccessToken.substring(0, hashedAccessToken.length / 2);
        console.log(hashedAccessToken, atHashLeftHalf, btoa(atHashLeftHalf), idTokenClaims["at_hash"]);
    }
}

const keystash = new Keystash({
    clientId: "my-client",
    redirectUrl: "http://localhost:7000/processAuth.html",
    authorizationUrl: "http://localhost:8080/auth/oauth/authorize"
});

export default keystash;