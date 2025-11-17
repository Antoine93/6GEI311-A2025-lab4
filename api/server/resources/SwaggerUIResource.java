package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 * SwaggerUIResource - Sert la page Swagger UI pour l'interface interactive /docs
 * Utilise Swagger UI via CDN (pas besoin de télécharger les fichiers)
 */
public class SwaggerUIResource extends BaseResource {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // Support CORS preflight
        if ("OPTIONS".equals(method)) {
            handleOptionsRequest(exchange);
            return;
        }

        if ("GET".equals(method)) {
            handleGetSwaggerUI(exchange);
        } else {
            sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED",
                "Méthode non supportée: " + method);
        }
    }

    /**
     * GET /docs - Retourne la page HTML Swagger UI
     */
    private void handleGetSwaggerUI(HttpExchange exchange) throws IOException {
        String html = generateSwaggerUIHtml();

        byte[] bytes = html.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, bytes.length);

        try (var os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Génère le HTML pour Swagger UI
     * Utilise le CDN officiel de Swagger UI (version 5.10.5)
     */
    private String generateSwaggerUIHtml() {
        return """
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ticket Management System API - Documentation</title>
    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.10.5/swagger-ui.css">
    <style>
        body {
            margin: 0;
            padding: 0;
        }
        .topbar {
            display: none;
        }
    </style>
</head>
<body>
    <div id="swagger-ui"></div>

    <script src="https://unpkg.com/swagger-ui-dist@5.10.5/swagger-ui-bundle.js"></script>
    <script src="https://unpkg.com/swagger-ui-dist@5.10.5/swagger-ui-standalone-preset.js"></script>
    <script>
        window.onload = function() {
            const ui = SwaggerUIBundle({
                url: "http://localhost:8080/openapi.yaml",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                    SwaggerUIBundle.presets.apis,
                    SwaggerUIStandalonePreset
                ],
                plugins: [
                    SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout",
                defaultModelsExpandDepth: 1,
                defaultModelExpandDepth: 1,
                displayRequestDuration: true,
                filter: true,
                tryItOutEnabled: true
            });
            window.ui = ui;
        };
    </script>
</body>
</html>
""";
    }

}
