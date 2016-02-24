/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dspace.authenticate.oauth;

import com.mashape.unirest.http.exceptions.UnirestException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dspace.eperson.EPerson;

/**
 *
 * @author Marcos Freitas Nunes <marcos@cognitivabrasil.com.br>
 */
public interface OauthEduLivreService {

    /**
     * Get cookies from request and search by token named {@value #COOKIE_NAME}.
     *
     * @param request
     * @return Return the token or null if it is not exist.
     */
    public Token getToken(HttpServletRequest request) throws TokenInvalidExeption;

    /**
     * Verifies if the token is valid or not. A GET request is sending to Oauth server.
     *
     * @param accessToken A String with the accessToken.
     * @return <code>true</code> if the token is valid and <code>falso</code> otherwise.
     */
    public boolean checkToken(String accessToken);

    /**
     * Resquest to the server a new token. A POST request is sending to Oauth server.
     *
     * @param token The object Token.
     * @throws UnirestException
     * @throws TokenInvalidExeption
     */
    public void renewToken(Token token) throws UnirestException, TokenInvalidExeption;

    /**
     * Update the cookie with the new token.
     *
     * @param t The token with the new values.
     * @param request To get the cookie
     * @param response To insert the new cookie
     */
    void updateCookie(Token t, HttpServletRequest request, HttpServletResponse response);

    /**
     * Update the user profile using the rest api provided by Educação Livre. Do the harvest and update the informations
     * in the {@link  EPerson}.
     *
     * @param token The object token containg the access token and the email.
     * @param eperson The object that will be updated.
     * @throws UnirestException
     * @throws TokenInvalidExeption
     */
    public void updateProfile(Token token, EPerson eperson) throws UnirestException, TokenInvalidExeption;

}
