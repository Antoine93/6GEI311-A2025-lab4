package api.server;

import com.sun.net.httpserver.HttpServer;
import api.server.resources.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * TicketAPIServer - Serveur HTTP REST pour le système de gestion de tickets
 *
 * Utilise com.sun.net.httpserver.HttpServer (inclus dans le JDK)
 * Écoute sur le port 8080
 *
 * Endpoints implémentés :
 * - /api/v1/auth/*      - Authentification
 * - /api/v1/users/*     - Gestion des utilisateurs
 * - /api/v1/tickets/*   - Gestion des tickets
 */
public class TicketAPIServer {

    private static final int PORT = 8080;
    private static final String BASE_PATH = "/api/v1";

    public static void main(String[] args) {
        try {
            // Créer le serveur HTTP
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            System.out.println("==================================================");
            System.out.println("  Serveur API REST - Système de Gestion de Tickets");
            System.out.println("  Port: " + PORT);
            System.out.println("  URL de base: http://localhost:" + PORT + BASE_PATH);
            System.out.println("==================================================\n");

            // Enregistrer les handlers (resources)
            registerHandlers(server);

            // Configurer l'executor pour gérer les requêtes
            server.setExecutor(Executors.newFixedThreadPool(10));

            // Démarrer le serveur
            server.start();

            System.out.println("[OK] Serveur démarré avec succès!");
            System.out.println("\n\u001B[1m\u001B[32m📚 Documentation interactive (Swagger UI):\u001B[0m");
            System.out.println("  \u001B[36mhttp://localhost:" + PORT + "/docs\u001B[0m");
            System.out.println("\nEndpoints disponibles:");
            System.out.println("  GET    " + BASE_PATH);
            System.out.println("  POST   " + BASE_PATH + "/auth/login");
            System.out.println("  GET    " + BASE_PATH + "/auth/session");
            System.out.println("  POST   " + BASE_PATH + "/auth/logout");
            System.out.println("  GET    " + BASE_PATH + "/users");
            System.out.println("  GET    " + BASE_PATH + "/users/{id}");
            System.out.println("  GET    " + BASE_PATH + "/tickets");
            System.out.println("  POST   " + BASE_PATH + "/tickets");
            System.out.println("  GET    " + BASE_PATH + "/tickets/{id}");
            System.out.println("  PUT    " + BASE_PATH + "/tickets/{id}");
            System.out.println("  DELETE " + BASE_PATH + "/tickets/{id}");
            System.out.println("  GET    " + BASE_PATH + "/tickets/{id}/comments");
            System.out.println("  POST   " + BASE_PATH + "/tickets/{id}/comments");
            System.out.println("  PATCH  " + BASE_PATH + "/tickets/{id}/status");
            System.out.println("  GET    " + BASE_PATH + "/tickets/{id}/status");
            System.out.println("  PATCH  " + BASE_PATH + "/tickets/{id}/assignment");
            System.out.println("  GET    " + BASE_PATH + "/tickets/{id}/export/pdf");
            System.out.println("\nAppuyez sur Ctrl+C pour arrêter le serveur...\n");

        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de démarrer le serveur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enregistre tous les handlers (resources) du serveur
     */
    private static void registerHandlers(HttpServer server) {
        // Documentation Swagger UI
        server.createContext("/docs", new SwaggerUIResource());
        server.createContext("/openapi.yaml", new OpenApiResource());

        // Page d'accueil de l'API
        server.createContext(BASE_PATH, new ApiHomeResource());

        // Authentification
        server.createContext(BASE_PATH + "/auth/login", new AuthResource());
        server.createContext(BASE_PATH + "/auth/session", new AuthResource());
        server.createContext(BASE_PATH + "/auth/logout", new AuthResource());

        // Utilisateurs
        server.createContext(BASE_PATH + "/users", new UserResource());

        // Tickets (CRUD)
        server.createContext(BASE_PATH + "/tickets", new TicketResource());

        System.out.println("[INFO] Handlers enregistrés avec succès");
    }
}
