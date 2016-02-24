/**
 * The contents of this file are subject to the license and copyright detailed in the LICENSE and NOTICE files at the
 * root of the source tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.servlet;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.dspace.app.webui.util.Authenticate;
import org.dspace.app.webui.util.JSPManager;
import org.dspace.authenticate.AuthenticationManager;
import org.dspace.authenticate.AuthenticationMethod;
import org.dspace.authenticate.oauth.OauthEduLivreService;
import org.dspace.authenticate.oauth.OauthEduLivreServiceImpl;
import org.dspace.authenticate.oauth.Token;
import org.dspace.authenticate.oauth.TokenInvalidExeption;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;

/**
 * Oauth 2.0 authentication with dspace. Tests if the user has the cookie with token, negative case redirect to API,
 * positive case use the authentication method.
 *
 * @author  <a href="mailto:marcos@cognitivabrasil.com.br">Marcos Nunes</a>
 * @version $Revision$
 */
public class OauthServlet extends DSpaceServlet {

    /**
     * log4j logger
     */
    private static final Logger log = Logger.getLogger(OauthServlet.class);

    /**
     * The address to login API.
     */
    private static final String apiLoginPage;

    /**
     * Apenas uma constante que será utilizada pelo servlet e pelo método de autenticação.
     */
    private static final String updatedToken;

    static {
        apiLoginPage = ConfigurationManager
                .getProperty("authentication-oauth", "api.login.url");

        updatedToken = ConfigurationManager
                .getProperty("authentication-oauth", "token.updated.name");

    }

    @Override
    protected void doDSGet(Context context,
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException, SQLException, AuthorizeException {

        log.debug("OauthServlet");

        Token token = null;
        OauthEduLivreService oauthService = new OauthEduLivreServiceImpl();
        try {
            token = oauthService.getToken(request);
        } catch (TokenInvalidExeption i) {
            log.error("The token is corrupted. Redirecting to API page...",i);
            response.sendRedirect(apiLoginPage);
        }
        if (token == null) {
            log.debug("The token isn't present. Redirecting to API page...");
            response.sendRedirect(apiLoginPage);
        } else {
            log.debug("Token founded! Continue with the authentication.");


            // Locate the eperson
            int status = AuthenticationManager.authenticate(context, null, null, null, request);
            Token newToken = (Token) request.getAttribute(updatedToken);

            if(newToken != null){
                //if the token was updated in the authentication method, update the cookie with the new token.
                log.debug("Updating the cookie with the new token...");
                oauthService.updateCookie(token, request, response);
                request.removeAttribute(updatedToken);
            }

            if (status == AuthenticationMethod.SUCCESS) {
                try {
                    // the AuthenticationManager updates the last_active field of the
                    // eperson that logged in. We need to commit the context, to store
                    // the updated field in the database.
                    context.commit();
                } catch (SQLException ex) {
                    // We can log the SQLException, but we should not interrupt the
                    // users interaction here.
                    log.error("Failed to write an updated last_active field of an "
                            + "EPerson into the databse.", ex);
                }

                // Logged in OK.
                Authenticate.loggedIn(context, request, context.getCurrentUser());

                log.info(LogManager.getHeader(context, "login", "type=oauth"));

                // resume previous request
                Authenticate.resumeInterruptedRequest(request, response);

                return;
            } else if (status == AuthenticationMethod.NO_SUCH_USER) {
                String jsp = "/login/no-single-sign-out.jsp";
                JSPManager.showJSP(request, response, jsp);
            } else if (status == AuthenticationMethod.BAD_CREDENTIALS) {
                //if haven't the token, redirect to API.
                response.sendRedirect(apiLoginPage);
            }

            // If we reach here, supplied email/password was duff.
            log.info(LogManager.getHeader(context, "failed_login", "result=" + String.valueOf(status)));
        }
    }
}
