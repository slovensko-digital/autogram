package com.octosign.whitelabel.cli.command;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toMap;

import com.octosign.whitelabel.error_handling.UserException;
import static com.octosign.whitelabel.ui.Utils.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import javafx.application.Application.Parameters;

public class CommandFactory {

    /**
     * Create command from JavaFX Application Parameters
     *
     * @param parameters
     * @return Command specified in the parameters or null if none
     */
    public static Command fromParameters(Parameters parameters) {
        var named = parameters.getNamed();
        var positional = parameters.getUnnamed();
        var urlParam = named.get("url");

        try {
            String commandName;
            Map<String, String> params;
            if (urlParam != null) {
                var url = new URIBuilder(urlParam);
                commandName = url.getHost();
                params = getUrlQueryParameters(url.getQueryParams());
            } else {
                commandName = positional.get(0);
                params = named;
            }

            return switch (commandName) {
                case ListenCommand.NAME -> new ListenCommand(params);
                default -> throw new IllegalArgumentException("Invalid command: " + commandName);
            };
        } catch (URISyntaxException e) {
            throw new UserException("error.launchFailed.header", "error.launchFailed.invalidUrl.description", e);
        } catch (Exception e) {
            throw new UserException("error.launchFailed.header", "error.launchFailed.invalidParams.description", e);
        }
    }

    private static Map<String, String> getUrlQueryParameters(List<NameValuePair> queryParams) {
        if (isNullOrEmpty(queryParams)) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(queryParams.stream()
                    .collect(toMap(NameValuePair::getName, NameValuePair::getValue)));
        }
    }

}
