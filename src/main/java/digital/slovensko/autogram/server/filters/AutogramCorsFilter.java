package digital.slovensko.autogram.server.filters;


import java.io.IOException;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * Add CORS HTTP headers and check HTTP method.
 * 
 * Access-Control-Allow-*...
 */
public class AutogramCorsFilter extends Filter {
    private final String allowedMethod;

    public AutogramCorsFilter(String allowedMethod) {
        this.allowedMethod = allowedMethod;
    }

    @Override
    public String description() {
        return "Add CORS headers and check HTTP method";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods",
                String.join(allowedMethod, "OPTIONS"));
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers",
                "Content-Type, Authorization");

        // Allow preflight requests
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().close();
            return;
        }

        // Check HTTP request method
        if (!exchange.getRequestMethod().equalsIgnoreCase(allowedMethod)) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        chain.doFilter(exchange);
    }

}
