package com.octosign.whitelabel.cli.command;

import java.util.Map;

/**
 * Command specified on the CLI
 *
 * The application can be launched using the CLI:
 *
 * 1. From custom protocol - by using --url parameter
 * 2. Using traditional CLI interface with command, parameters, flags...
 *
 * EXAMPLE:
 * autogram://listen?protocol=https&host=localhost&port=37200&origin=*&key=abc12&nonce=16&language=sk
 * where autogram:// is any protocol configured for this application
 * Used when the application is launched using the custom protocol
 */
public abstract class Command {

    /**
     * Named parameters used with the command
     */
    protected Map<String, String> parameters;

    Command(Map<String, String> parameters) {
        this.parameters = parameters;
    }

}
