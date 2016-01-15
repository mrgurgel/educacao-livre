/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dspace.authenticate.oauth;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dspace.core.ConfigurationManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:marcos@cognitivabrasil.com.br">Marcos Nunes</a>
 */
public class OauthEduLivreServiceImpl implements OauthEduLivreService {

    private static final String cookieName;
    private static final String oauthAddress;
    private static final String clientId;
    private static final String clientSecret;

    private static final Logger log = LoggerFactory.getLogger(OauthEduLivreServiceImpl.class);

    static {
        cookieName = ConfigurationManager
                .getProperty("authentication-oauth", "api.cookie.name");

        oauthAddress = ConfigurationManager
                .getProperty("authentication-oauth", "api.url");

        clientId = ConfigurationManager
                .getProperty("authentication-oauth", "application.client.id");

        clientSecret = ConfigurationManager
                .getProperty("authentication-oauth", "application.client.secret");

    }

    @Override
    public Token getToken(HttpServletRequest request) throws TokenInvalidExeption {
        try {
            Cookie cookie = getCookie(request);
            if (cookie == null) {
                return null;
            }
            Token token = new Token(cookie.getValue());
            return token;
        } catch (IndexOutOfBoundsException e) {
            throw new TokenInvalidExeption("O token não está no formato correto!");
        }
    }

    @Override
    public void updateCookie(Token t, HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = getCookie(request);
        cookie.setValue(t.encrypt());
        response.addCookie(cookie);
    }

    /**
     * Get cookies from request and search by token named {@value #cookieName}.
     *
     * @param request
     * @return Return the cookie or null if it is not exist.
     */
    private Cookie getCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            //se não tiver nenhum cookie returna null
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }
        //se não tiver nenhum token nos cookies existentes retorna null
        return null;
    }

    @Override
    public boolean checkToken(String accessToken) {
        log.debug("Verifing the token...");
        try {
            HttpResponse<String> response = Unirest.get(oauthAddress + "/resource")
                    .header("accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .asString();
            if (response.getStatus() == 200) {
                log.debug("The token is valid!");
                return true;
            } else {
                log.debug("The token is invalid!");
                return false;
            }
        } catch (UnirestException e) {
            return false;
        }
    }

    @Override
    public void renewToken(Token token) throws UnirestException, TokenInvalidExeption {
        log.debug("Updating the token..");
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("client_id", clientId);
        jsonBody.put("client_secret", clientSecret);
        jsonBody.put("refresh_token", token.getRefreshToken());
        jsonBody.put("grant_type", "refresh_token");

        String body = jsonBody.toString();

        HttpResponse<JsonNode> response = Unirest.post(oauthAddress)
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .body(body)
                .asJson();

        if (response.getStatus() != 200) {
            throw new TokenInvalidExeption("Error updating the token. Body: " + response.getBody());
        }
        JSONObject myObj = response.getBody().getObject();
        String accessToken = myObj.getString("access_token");
        token.setAccessToken(accessToken);
        log.debug("Token was updated!");
    }
}
