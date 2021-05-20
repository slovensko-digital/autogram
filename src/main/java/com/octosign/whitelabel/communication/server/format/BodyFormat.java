package com.octosign.whitelabel.communication.server.format;

/**
 * Format used in the HTTP exchange request or response body
 */
public interface BodyFormat {

    /**
     * Convert string body to required object
     *
     * @param <T>           Object that is expected to be represented by the body
     * @param input         String body
     * @param inputClass    Class of the expected body
     * @return Object from the body
     */
    public <T> T from(String input, Class<T> inputClass);

    /**
     * Convert object to string body
     *
     * @param <T>   Object that is should be represented in the body
     * @param input Object T that should be represented as string
     * @return String for the body
     */
    public <T> String to(T input);

    /**
     * MIME type this body format produces and accepts
     *
     * @return MIME type, e.g. application/json
     */
    public String getMimeType();

}
