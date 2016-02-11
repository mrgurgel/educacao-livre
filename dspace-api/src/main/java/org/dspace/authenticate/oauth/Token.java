/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dspace.authenticate.oauth;

import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Marcos Freitas Nunes <marcos@cognitivabrasil.com.br>
 */
public class Token {

    private String accessToken;

    private final String refreshToken;

    private final String email;

    /**
     * Receive the token and decrypt it, turning into a {@link Token} object.
     *
     * @param token A string containing lexical representation of xsd:base64Binary. Token must be 80 character plus
     * email.
     */
    public Token(String token) {
        byte[] byteToken = DatatypeConverter.parseBase64Binary(token);
        String decryptToken = new String(byteToken);
        String token1 = decryptToken.substring(0, 20);
        String token2 = decryptToken.substring(20, 40);
        String token3 = decryptToken.substring(40, 60);
        String token4 = decryptToken.substring(60, 80);
        //estava dando erro no email, pois vinha um caracter não ASCII no final do token.
        String tokenEmail = decryptToken.substring(80).replaceAll("[^ -~]", "").trim();

        this.accessToken = token1.concat(token3);
        this.refreshToken = token2.concat(token4);
        this.email = tokenEmail;
    }

    /**
     * Encrypt the object token in a string. According to Portal Educação Livre pattern.
     * @return A string containing a lexical representation of xsd:base64Binary.
     */
    public String encrypt() {

        String token1 = accessToken.substring(0,20);
        String token2 = accessToken.substring(20);
        String token3 = refreshToken.substring(0,20);
        String token4 = refreshToken.substring(20);

        String token = token1+token3+token2+token4+email;

        return DatatypeConverter.printBase64Binary(token.getBytes());
    }

    /**
     * Get the value of email
     *
     * @return the value of email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the value of refreshToken
     *
     * @return the value of refreshToken
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Get the value of accessToken
     *
     * @return the value of accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Set the value of accessToken
     *
     * @param accessToken new value of accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "Token{" + "accessToken=" + accessToken + ", refreshToken=" + refreshToken + ", email=" + email + '}';
    }

}
