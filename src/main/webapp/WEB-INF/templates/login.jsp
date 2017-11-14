<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Login</title>
        <script type="text/javascript" src="/resources/js/test.js"></script>

        <link rel="stylesheet" type="text/css" href="/resources/css/login.css" />

    </head>
    <body>
        <div class="platform-login">
            <div class="platform-login-inner">

                <div class="platform-login-header"></div>
                <c:if test = "${it.error != null}">
                    <div class="platform-login-error">${it.error}</div>
                </c:if>
                <form name="form" id="login-form" action="/auth/login" method="POST" class="platform-login-form">
                    <input type="text" name="username" value="" placeholder="Username" id="username" />
                    <input type="password" name="password" placeholder="Password" id="password" />
                    <input type="hidden" name="returnTo" id="returnTo" value='${it.returnTo}' />
                    <input type="hidden" name="fb_access_token" id="fb_access_token" />
                    <input type="submit" id="login" value="Login" class="btn btn-primary" />
                </form>
                <%--<div class="platform-login-divider">OR</div>--%>
                <%--<div class="fb-login-button platform-social-login"--%>
                     <%--data-width="300"--%>
                     <%--data-max-rows="1"--%>
                     <%--data-size="large"--%>
                     <%--data-button-type="continue_with"--%>
                     <%--data-show-faces="false"--%>
                     <%--data-auto-logout-link="false"--%>
                     <%--data-use-continue-as="false"--%>
                     <%--data-scope="public_profile,email"--%>
                     <%--data-onlogin="submitFacebookLogin();"></div>--%>

            </div>
        </div>
    </body>
</html>