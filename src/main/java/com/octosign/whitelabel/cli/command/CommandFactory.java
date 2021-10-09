package com.octosign.whitelabel.cli.command;

import javafx.application.Application.Parameters;

import java.net.URI;
import java.net.URISyntaxException;

public class CommandFactory {

    /**
     * Create command from JavaFX Application Parameters
     *
     * @param parameters
     * @return Command specified in the parameters or null if none
     * @throws URISyntaxException
     */
    public static Command fromParameters(Parameters parameters) throws URISyntaxException {
        var named = parameters.getNamed();
        var positional = parameters.getUnnamed();
        var urlParam = named.get("url");

        if (urlParam != null) {
            var url = new URI(urlParam);

            return fromUrl(url.getHost(), url);
        }

        if (positional.size() == 0) {
            return null;
        }

        return fromParameters(positional.get(0), parameters);
    }

    /**
     * Create command using its name and CLI parameters
     *
     * @param name      Name, e.g. "listen"
     * @param parameters    Launch URL
     * @return Command if it exists or null if not
     */
    public static Command fromParameters(String name, Parameters parameters) {
        return switch (name) {
            case ListenCommand.NAME -> new ListenCommand(parameters);
            default -> null;
        };
    }

    /**
     * Create command using its name and launch URL
     *
     * @param name  Name, e.g. "listen"
     * @param url   Launch URL
     * @return Command if it exists or null if not
     */
    public static Command fromUrl(String name, URI url) {
        return switch (name) {
            case ListenCommand.NAME -> new ListenCommand(url);
            default -> null;
        };
    }

}
