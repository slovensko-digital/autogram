package com.octosign.whitelabel.cli.command;

import java.net.URI;

import javafx.application.Application.Parameters;

/**
 * Command specified on the CLI
 *
 * The application can be launched using the CLI:
 *
 * 1. From custom protocol - by using --url parameter
 * 2. Using traditional CLI interface with command, parameters, flags...
 */
public abstract class Command {

    /**
     * URL available if the command was created from URL
     */
    protected URI url;

    /**
     * Parameters available if the command was not created from URL
     */
    protected Parameters parameters;

    Command(URI url) {
        this.url = url;
    }

    Command(Parameters parameters) {
        this.parameters = parameters;
    }

}
