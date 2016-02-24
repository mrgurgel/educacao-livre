/**
 * The contents of this file are subject to the license and copyright detailed in the LICENSE and NOTICE files at the
 * root of the source tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authenticate;

import com.mashape.unirest.http.exceptions.UnirestException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.dspace.authenticate.oauth.OauthEduLivreService;
import org.dspace.authenticate.oauth.OauthEduLivreServiceImpl;
import org.dspace.authenticate.oauth.Token;
import org.dspace.authenticate.oauth.TokenInvalidExeption;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;

/**
 * Implicit authentication method that gets credentials from the oauth 2.0 API. If the token is not present, the user
 * will be redirect to the API login. The email address present in the token is taken as the authenticated user name.
 * <p>
 * See the <code>AuthenticationMethod</code> interface for more details.
 * <p>
 * <b>Configuration:</b>
 *
 * <pre>
 * api.login.url =
 * <em>
 * The API login url.
 * </em>
 *
 * api.url =
 * <em>
 * The url to the oauth 2.0 API. Used to validade and renew the token.
 * </em>
 *
 * application.client.id =
 * <em>
 * The client id from Portal Educação livre.
 * </em>
 *
 * application.client.secret =
 * <em>
 * The client secret value from Portal Educação Livre client application.
 * </em>
 *
 * autoregister =
 * <em>
 * &quot;true&quot; if E-Person is created automatically for unknown new users.
 * </em>
 * groups =
 * <em>
 * comma-delimited list of special groups to add user to if authenticated.
 * </em>
 * </pre>
 *
 * <p>
 * The <code>autoregister</code> configuration parameter determines what the <code>canSelfRegister()</code> method
 * returns. It also allows an EPerson record to be created automatically when the presented token is acceptable but
 * there is no corresponding EPerson.
 *
 * @author <a href="mailto:marcos@cognitivabrasil.com.br">Marcos Nunes</a>
 * @version $Revision$
 */
public class OauthAuthentication implements AuthenticationMethod {

    /**
     * log4j category
     */
    private static final Logger log = Logger.getLogger(OauthAuthentication.class);

    private final OauthEduLivreService oauthService = new OauthEduLivreServiceImpl();
    private static final String language;

    static {
        language = ConfigurationManager
                .getProperty("authentication-oauth", "login.defaultlanguage");
    }

    /**
     * Predicate, can new user automatically create EPerson. Checks configuration value. You'll probably want this to be
     * true to take advantage of a integrate autentication infrastructure with many more users than are already known by
     * DSpace.
     */
    @Override
    public boolean canSelfRegister(Context context, HttpServletRequest request,
            String username) throws SQLException {
        return ConfigurationManager
                .getBooleanProperty("authentication-oauth", "autoregister");
    }

    /**
     * Nothing extra to initialize.
     */
    @Override
    public void initEPerson(Context context, HttpServletRequest request,
            EPerson eperson) throws SQLException {
    }

    /**
     * We don't use EPerson password so there is no reason to change it.
     */
    @Override
    public boolean allowSetPassword(Context context,
            HttpServletRequest request, String username) throws SQLException {
        return false;
    }

    /**
     * Returns true, this is an implicit method.
     */
    @Override
    public boolean isImplicit() {
        return true;
    }

    /**
     * Return special groups defined in authentication-oauth.cfg by the login.specialgroup key.
     *
     * @param context
     * @param request The object that contains the token.
     *
     * @return An int array of group IDs
     *
     */
    @Override
    public int[] getSpecialGroups(Context context, HttpServletRequest request)
            throws SQLException {
        try {
            Token token = oauthService.getToken(request);
            if (token != null && oauthService.checkToken(token.getAccessToken())) {

                String groupName = ConfigurationManager.getProperty("authentication-oauth", "login.specialgroup");

                if ((groupName != null) && (!groupName.trim().equals(""))) {
                    Group specialGroup = Group.findByName(context, groupName);
                    if (specialGroup == null) {
                        // Oops - the group isn't there.
                        log.warn(LogManager.getHeader(context,
                                "password_specialgroup",
                                "Group defined in modules/authentication-oauth.cfg login.specialgroup does not exist"));
                        return new int[0];
                    } else {
                        return new int[]{specialGroup.getID()};
                    }
                }
            }
        } catch (SQLException | TokenInvalidExeption e) {
            log.error("Error getting special groups", e);
            // The user is not a password user, so we don't need to worry about them
        }
        return new int[0];
    }

    /**
     * Oauth authentication. The token is obtained from the <code>ServletRequest</code> object.
     * <ul>
     * <li>If the token is valid, and corresponds to an existing EPerson, and the user is allowed to login, return
     * success.</li>
     * <li>If the user is matched but is not allowed to login, it fails.</li>
     * <li>If the token is valid, but there is no corresponding EPerson, the
     * <code>"authentication.oauth.autoregister"</code> configuration parameter is checked (via
     * <code>canSelfRegister()</code>)
     * <ul>
     * <li>If it's true, a new EPerson record is created for the token, and the result is success.</li>
     * <li>If it's false, return that the user was unknown.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param context
     * @param username It isn't used in this case.
     * @param password It isn't used in this case.
     * @param realm It isn't used in this case.
     * @param request The object that contains the token.
     * @return One of: SUCCESS, BAD_CREDENTIALS, NO_SUCH_USER, BAD_ARGS
     * @throws SQLException
     */
    @Override
    public int authenticate(Context context, String username, String password,
            String realm, HttpServletRequest request) throws SQLException {

        try {
            Token token = oauthService.getToken(request);
            if (token == null) {
                log.debug("Token inexistente!");
                return BAD_CREDENTIALS;
            }
            boolean checked = oauthService.checkToken(token.getAccessToken());
            if (!checked) {
                oauthService.renewToken(token);
                //define an attribute in request to inform that cookie was updated.
                request.setAttribute("updatedToken", token);
            }
            log.debug("token decoded: " + token);

            // And it's valid - try and get an e-person
            String email = token.getEmail();
            EPerson eperson = null;
            if (email != null) {
                try {
                    eperson = EPerson.findByEmail(context, email);
                } catch (AuthorizeException e) {
                    log.debug("Failed to authorize looking up EPerson", e);
                }

            }
            if (eperson == null) {
                // Token is valid, but no record.
                if (email != null && canSelfRegister(context, request, null)) {
                    // Register the new user automatically
                    log.info(LogManager.getHeader(context, "autoregister",
                            "from=oauth, email=" + email));

                    // TEMPORARILY turn off authorisation
                    context.turnOffAuthorisationSystem();
                    eperson = EPerson.create(context);
                    eperson.setEmail(email);
                    eperson.setCanLogIn(true);
                    eperson.setLanguage(language);
                    AuthenticationManager.initEPerson(context, request, eperson);
                    //gets the profile datas from the Educação Livre API.
                    oauthService.updateProfile(token, eperson);

                    eperson.update();
                    context.commit();
                    context.restoreAuthSystemState();
                    context.setCurrentUser(eperson);
//                        setSpecialGroupsFlag(request, email);
                    return SUCCESS;
                } else {
                    // No auto-registration for valid token
                    log.warn(LogManager.getHeader(context, "authenticate",
                            "type=token_but_no_record, cannot auto-register"));
                    return NO_SUCH_USER;
                }
            } // make sure this is a login account
            else if (!eperson.canLogIn()) {
                log.warn(LogManager.getHeader(context, "authenticate", "type=oauth, email=" + email
                        + ", canLogIn=false, rejecting."));
                return BAD_ARGS;
            } else {
                log.info(LogManager.getHeader(context, "login", "type=oauth"));
                oauthService.updateProfile(token, eperson);
                eperson.update();
                context.commit();
                context.setCurrentUser(eperson);
                return SUCCESS;
            }

        } catch (TokenInvalidExeption ex) {
            log.error("The token is invalid!.", ex);
            return BAD_CREDENTIALS;
        } catch (UnirestException ex) {
            log.error("An error occurred while updating the token.", ex);
            return BAD_CREDENTIALS;
        } catch (AuthorizeException ce) {
            log.warn(LogManager.getHeader(context, "authorize_exception", ""), ce);
            return BAD_ARGS;
        }

    }

    /**
     * Returns URL of oauth-login servlet.
     *
     * @param context DSpace context, will be modified (EPerson set) upon success.
     *
     * @param request The HTTP request that started this operation, or null if not applicable.
     *
     * @param response The HTTP response from the servlet method.
     *
     * @return fully-qualified URL
     */
    @Override
    public String loginPageURL(Context context, HttpServletRequest request,
            HttpServletResponse response) {
        log.debug("\n loginPageURL oauth");
        log.debug("\nVariáveis do arquivo de configuração: ");
        log.debug("    groupName:" + ConfigurationManager.getProperty("authentication-oauth", "login.specialgroup"));
        log.debug("    autoregister:" + ConfigurationManager.getBooleanProperty("authentication-oauth", "autoregister"));
        return response.encodeRedirectURL(request.getContextPath() + "/oauth-login");
    }

    /**
     * Returns message key for title of the "login" page, to use in a menu showing the choice of multiple login methods.
     *
     * @param context DSpace context, will be modified (EPerson set) upon success.
     *
     * @return Message key to look up in i18n message catalog.
     */
    public String loginPageTitle(Context context) {
        return "org.dspace.eperson.OauthAuthentication.title";
    }
}
