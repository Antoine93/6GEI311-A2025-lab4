package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * OpenApiResource - Sert le fichier OpenAPI YAML
 * Endpoint: GET /openapi.yaml
 */
public class OpenApiResource extends BaseResource {

    private static final String OPENAPI_FILE_PATH = "api/openapi/tickets-api.yaml";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // Support CORS preflight
        if ("OPTIONS".equals(method)) {
            handleOptionsRequest(exchange);
            return;
        }

        if ("GET".equals(method)) {
            handleGetOpenApiSpec(exchange);
        } else {
            sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED",
                "Méthode non supportée: " + method);
        }
    }

    /**
     * GET /openapi.yaml - Retourne le fichier de spécification OpenAPI
     */
    private void handleGetOpenApiSpec(HttpExchange exchange) throws IOException {
        try {
            // Lire le fichier YAML
            String yamlContent = Files.readString(Paths.get(OPENAPI_FILE_PATH));

            // Envoyer la réponse YAML
            byte[] bytes = yamlContent.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/yaml; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);

            try (var os = exchange.getResponseBody()) {
                os.write(bytes);
            }

        } catch (IOException e) {
            sendErrorResponse(exchange, 500, "INTERNAL_ERROR",
                "Impossible de lire le fichier OpenAPI: " + e.getMessage());
        }
    }

}
