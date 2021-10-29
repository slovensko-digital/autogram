package com.octosign.whitelabel.cli.command;

import com.octosign.whitelabel.error_handling.IntegrationException;
import javafx.application.Application.Parameters;

import java.net.URI;
import java.net.URISyntaxException;

public class CommandFactory {

    /**
     * Create command from JavaFX Application Parameters
     *
     * @param parameters
     * @return Command specified in the parameters or null if none
     * @throws IntegrationException
     */
    public static Command fromParameters(Parameters parameters) throws IntegrationException {
        var named = parameters.getNamed();
        var positional = parameters.getUnnamed();
        var urlParam = named.get("url");

        try {
            if (urlParam != null) {
                var url = new URI(urlParam);
                return fromUrl(url.getHost(), url);
            } else {
                return fromParameters(positional.get(0), parameters);
            }
        } catch (URISyntaxException e) {
            throw new IntegrationException(String.format("Invalid URL - RFC 2396 violation: %s", e));
        } catch (IllegalArgumentException e) {
            throw new IntegrationException(String.format("Invalid parameters: %s", e));
        }
    }

    /**
     * Create command using its name and CLI parameters
     *
     * @param name       Name, e.g. "listen"
     * @param parameters Launch URL
     * @return Command if it exists or null if not
     */
    public static Command fromParameters(String name, Parameters parameters) {
        return switch (name) {
            case ListenCommand.NAME -> new ListenCommand(parameters);
            default -> throw new IllegalArgumentException(String.format("Invalid command: %s", name));
        };
    }

    /**
     * Create command using its name and launch URL
     *
     * @param name Name, e.g. "listen"
     * @param url  Launch URL
     * @return Command if it exists or null if not
     */
    public static Command fromUrl(String name, URI url) {
        return switch (name) {
            case ListenCommand.NAME -> new ListenCommand(url);
            default -> throw new IllegalArgumentException(String.format("Invalid command: %s", name));
        };
    }

}
