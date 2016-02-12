/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dspace.authenticate.oauth;

/**
 *
 * @author <a href="mailto:marcos@cognitivabrasil.com.br">Marcos Nunes</a>
 */
public class TokenInvalidExeption extends Exception {

    /**
     * Creates a new instance of <code>TokenValidateExpeption</code> without detail message.
     */
    public TokenInvalidExeption() {
    }

    /**
     * Constructs an instance of <code>TokenValidateExpeption</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public TokenInvalidExeption(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>TokenValidateExpeption</code> with the specified detail message and cause.
     *
     * @param msg the detail message.
     * @param cause the cause (which is saved for later retrieval by the Exception.getCause() method). (A null value is
     * permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public TokenInvalidExeption(String msg, Throwable cause) {
        super(msg, cause);
    }
}
