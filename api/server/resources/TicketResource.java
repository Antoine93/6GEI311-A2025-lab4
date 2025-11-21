package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import api.server.models.*;
import api.server.services.ApplicationState;
import core.entities.User;

import java.io.IOException;
import java.util.List;

/**
 * TicketResource - Handler pour tous les endpoints tickets
 *
 * CRUD Tickets:
 * GET    /api/v1/tickets             [Auth requis]
 * POST   /api/v1/tickets             [Auth requis]
 * GET    /api/v1/tickets/{id}        [Auth requis]
 * PUT    /api/v1/tickets/{id}        [Auth requis + permissions]
 * DELETE /api/v1/tickets/{id}        [Admin seulement]
 *
 * Comments:
 * GET  /api/v1/tickets/{id}/comments [Auth requis]
 * POST /api/v1/tickets/{id}/comments [Auth requis]
 *
 * Status:
 * GET   /api/v1/tickets/{id}/status  [Auth requis]
 * PATCH /api/v1/tickets/{id}/status  [Admin/Dev seulement]
 *
 * Assignment:
 * PATCH /api/v1/tickets/{id}/assignment [Admin/Dev seulement]
 *
 * Export:
 * GET /api/v1/tickets/{id}/export/pdf [Auth requis]
 */
public class TicketResource extends BaseResource {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // Support CORS preflight
        if ("OPTIONS".equals(method)) {
            handleOptionsRequest(exchange);
            return;
        }

        try {
            // Router vers le bon handler
            if (path.contains("/comments")) {
                handleCommentsEndpoints(exchange, method, path);
            } else if (path.contains("/status")) {
                handleStatusEndpoints(exchange, method, path);
            } else if (path.contains("/assignment")) {
                handleAssignmentEndpoint(exchange, method, path);
            } else if (path.contains("/export/pdf")) {
                handleExportPdfEndpoint(exchange, method, path);
            } else {
                handleTicketCRUD(exchange, method, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "INTERNAL_ERROR", e.getMessage());
        }
    }

    /**
     * CRUD basique des tickets
     */
    private void handleTicketCRUD(HttpExchange exchange, String method, String path) throws IOException {
        switch (method) {
            case "GET":
                if (path.endsWith("/tickets")) {
                    handleGetAllTickets(exchange);
                } else {
                    handleGetTicketById(exchange, path);
                }
                break;
            case "POST":
                handleCreateTicket(exchange);
                break;
            case "PUT":
                handleUpdateTicket(exchange, path);
                break;
            case "DELETE":
                handleDeleteTicket(exchange, path);
                break;
            default:
                sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED", "Méthode non autorisée");
        }
    }

    /**
     * GET /tickets
     * Authentification requise
     * Admin/Dev voient tous les tickets, les autres seulement les leurs
     */
    private void handleGetAllTickets(HttpExchange exchange) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return; // Erreur 401 déjà envoyée

        List<TicketDTO> tickets = appState.getAllTicketsDTO();

        // Filtrer selon les permissions
        if (!hasFullAccess(user)) {
            // Utilisateur normal: seulement ses propres tickets
            tickets = tickets.stream()
                .filter(t -> t.getCreatedByName().equals(user.getName()))
                .toList();
        }

        sendJsonResponse(exchange, 200, tickets);
        System.out.println("[TICKETS] Liste de " + tickets.size() + " tickets récupérée pour " + user.getName());
    }

    /**
     * GET /tickets/{id}
     * Authentification requise
     */
    private void handleGetTicketById(HttpExchange exchange, String path) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        Integer ticketId = extractIdFromPath(path);

        if (ticketId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID ticket invalide");
            return;
        }

        TicketDTO ticket = appState.getTicketDTOById(ticketId);

        if (ticket == null) {
            sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket #" + ticketId + " introuvable");
            return;
        }

        // Vérifier que l'utilisateur a accès à ce ticket
        if (!hasFullAccess(user) && !ticket.getCreatedByName().equals(user.getName())) {
            sendErrorResponse(exchange, 403, "FORBIDDEN",
                "Vous n'avez pas accès à ce ticket");
            return;
        }

        sendJsonResponse(exchange, 200, ticket);
        System.out.println("[TICKETS] Ticket #" + ticketId + " récupéré par " + user.getName());
    }

    /**
     * POST /tickets
     * Authentification requise (tous les utilisateurs peuvent créer)
     */
    private void handleCreateTicket(HttpExchange exchange) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        String requestBody = readRequestBody(exchange);
        CreateTicketRequest request = gson.fromJson(requestBody, CreateTicketRequest.class);

        if (request == null || request.getTitle() == null || request.getTitle().isEmpty()) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "Le titre du ticket ne peut pas être vide");
            return;
        }

        // Créer le ticket avec l'utilisateur authentifié
        TicketDTO createdTicket = appState.createTicket(request, user);

        sendJsonResponse(exchange, 201, createdTicket);
        System.out.println("[TICKETS] Ticket #" + createdTicket.getTicketID() + " créé par " + user.getName() + ": " + createdTicket.getTitle());
    }

    /**
     * PUT /tickets/{id}
     * Authentification requise + permissions (créateur ou Admin/Dev)
     */
    private void handleUpdateTicket(HttpExchange exchange, String path) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        Integer ticketId = extractIdFromPath(path);

        if (ticketId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID ticket invalide");
            return;
        }

        // Vérifier que le ticket existe et récupérer le créateur
        core.entities.Ticket ticket = appState.findTicketById(ticketId);
        if (ticket == null) {
            sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket #" + ticketId + " introuvable");
            return;
        }

        // Vérifier les permissions
        if (!canEditTicket(user, ticket.getCreatedByUserID())) {
            sendErrorResponse(exchange, 403, "FORBIDDEN",
                "Vous n'êtes pas autorisé à modifier ce ticket");
            return;
        }

        String requestBody = readRequestBody(exchange);
        UpdateTicketRequest request = gson.fromJson(requestBody, UpdateTicketRequest.class);

        TicketDTO updatedTicket = appState.updateTicket(ticketId, request);

        sendJsonResponse(exchange, 200, updatedTicket);
        System.out.println("[TICKETS] Ticket #" + ticketId + " modifié par " + user.getName());
    }

    /**
     * DELETE /tickets/{id}
     * Admin seulement
     */
    private void handleDeleteTicket(HttpExchange exchange, String path) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        // Vérifier que l'utilisateur est admin
        if (!requireAdmin(exchange, user)) return;

        Integer ticketId = extractIdFromPath(path);

        if (ticketId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID ticket invalide");
            return;
        }

        boolean deleted = appState.deleteTicket(ticketId);

        if (!deleted) {
            sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket #" + ticketId + " introuvable");
            return;
        }

        sendNoContent(exchange);
        System.out.println("[TICKETS] Ticket #" + ticketId + " supprimé par " + user.getName());
    }

    /**
     * Gestion des commentaires
     * Authentification requise
     */
    private void handleCommentsEndpoints(HttpExchange exchange, String method, String path) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        Integer ticketId = extractIdFromPath(path.replace("/comments", ""));

        if (ticketId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID ticket invalide");
            return;
        }

        if ("GET".equals(method)) {
            List<String> comments = appState.getTicketComments(ticketId);
            sendJsonResponse(exchange, 200, comments);
            System.out.println("[COMMENTS] " + comments.size() + " commentaires récupérés pour ticket #" + ticketId);
        } else if ("POST".equals(method)) {
            String requestBody = readRequestBody(exchange);
            CommentRequest request = gson.fromJson(requestBody, CommentRequest.class);

            if (request == null || request.getText() == null || request.getText().isEmpty()) {
                sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "Le commentaire ne peut pas être vide");
                return;
            }

            String comment = appState.addComment(ticketId, request.getText());

            if (comment == null) {
                sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket #" + ticketId + " introuvable");
                return;
            }

            sendJsonResponse(exchange, 201, comment);
            System.out.println("[COMMENTS] Commentaire ajouté au ticket #" + ticketId + " par " + user.getName());
        } else {
            sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED", "Méthode non autorisée");
        }
    }

    /**
     * Gestion des statuts
     * GET: Authentification requise
     * PATCH: Admin/Dev seulement
     */
    private void handleStatusEndpoints(HttpExchange exchange, String method, String path) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        Integer ticketId = extractIdFromPath(path.replace("/status", ""));

        if (ticketId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID ticket invalide");
            return;
        }

        if ("GET".equals(method)) {
            List<String> transitions = appState.getAvailableTransitions(ticketId);

            if (transitions == null) {
                sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket #" + ticketId + " introuvable");
                return;
            }

            sendJsonResponse(exchange, 200, transitions);
        } else if ("POST".equals(method)) {
            // Seuls Admin et Développeur peuvent changer les statuts
            if (!hasFullAccess(user)) {
                sendErrorResponse(exchange, 403, "FORBIDDEN",
                    "Seuls les administrateurs et développeurs peuvent changer les statuts");
                return;
            }

            String requestBody = readRequestBody(exchange);
            StatusUpdateDTO request = gson.fromJson(requestBody, StatusUpdateDTO.class);

            if (request == null || request.getNewStatus() == null) {
                sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "Nouveau statut manquant");
                return;
            }

            try {
                TicketDTO updatedTicket = appState.changeTicketStatus(ticketId, request.getNewStatus());

                if (updatedTicket == null) {
                    sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket #" + ticketId + " introuvable");
                    return;
                }

                sendJsonResponse(exchange, 200, updatedTicket);
                System.out.println("[STATUS] Statut du ticket #" + ticketId + " changé vers: " + request.getNewStatus() + " par " + user.getName());
            } catch (IllegalStateException e) {
                sendErrorResponse(exchange, 400, "INVALID_TRANSITION", e.getMessage());
            }
        } else {
            sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED", "Méthode non autorisée");
        }
    }

    /**
     * Assignation de ticket
     * Admin/Dev seulement
     */
    private void handleAssignmentEndpoint(HttpExchange exchange, String method, String path) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        // Seuls Admin et Développeur peuvent assigner
        if (!hasFullAccess(user)) {
            sendErrorResponse(exchange, 403, "FORBIDDEN",
                "Seuls les administrateurs et développeurs peuvent assigner des tickets");
            return;
        }

        if (!"POST".equals(method)) {
            sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED", "Méthode non autorisée");
            return;
        }

        Integer ticketId = extractIdFromPath(path.replace("/assignment", ""));

        if (ticketId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID ticket invalide");
            return;
        }

        String requestBody = readRequestBody(exchange);
        AssignmentDTO request = gson.fromJson(requestBody, AssignmentDTO.class);

        if (request == null || request.getUserID() <= 0) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID utilisateur invalide");
            return;
        }

        TicketDTO updatedTicket = appState.assignTicket(ticketId, request.getUserID());

        if (updatedTicket == null) {
            sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket ou utilisateur introuvable");
            return;
        }

        sendJsonResponse(exchange, 200, updatedTicket);
        System.out.println("[ASSIGNMENT] Ticket #" + ticketId + " assigné à l'utilisateur #" + request.getUserID() + " par " + user.getName());
    }

    /**
     * Export PDF
     * Authentification requise
     */
    private void handleExportPdfEndpoint(HttpExchange exchange, String method, String path) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        if (!"GET".equals(method)) {
            sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED", "Méthode non autorisée");
            return;
        }

        Integer ticketId = extractIdFromPath(path.replace("/export/pdf", ""));

        if (ticketId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID ticket invalide");
            return;
        }

        String pdfContent = appState.exportTicketToPDF(ticketId);

        if (pdfContent == null) {
            sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket #" + ticketId + " introuvable");
            return;
        }

        sendTextResponse(exchange, 200, pdfContent);
        System.out.println("[EXPORT] Ticket #" + ticketId + " exporté en PDF par " + user.getName());
    }
}
