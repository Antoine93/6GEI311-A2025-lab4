package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import api.server.models.*;
import api.server.services.ApplicationState;

import java.io.IOException;
import java.util.List;

/**
 * TicketResource - Handler pour tous les endpoints tickets
 *
 * CRUD Tickets:
 * GET    /api/v1/tickets
 * POST   /api/v1/tickets
 * GET    /api/v1/tickets/{id}
 * PUT    /api/v1/tickets/{id}
 * DELETE /api/v1/tickets/{id}
 *
 * Comments:
 * GET  /api/v1/tickets/{id}/comments
 * POST /api/v1/tickets/{id}/comments
 *
 * Status:
 * GET   /api/v1/tickets/{id}/status
 * PATCH /api/v1/tickets/{id}/status
 *
 * Assignment:
 * PATCH /api/v1/tickets/{id}/assignment
 *
 * Export:
 * GET /api/v1/tickets/{id}/export/pdf
 */
public class TicketResource extends BaseResource {

    private final ApplicationState appState = ApplicationState.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

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
     */
    private void handleGetAllTickets(HttpExchange exchange) throws IOException {
        List<TicketDTO> tickets = appState.getAllTicketsDTO();
        sendJsonResponse(exchange, 200, tickets);
        System.out.println("[TICKETS] Liste de " + tickets.size() + " tickets récupérée");
    }

    /**
     * GET /tickets/{id}
     */
    private void handleGetTicketById(HttpExchange exchange, String path) throws IOException {
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

        sendJsonResponse(exchange, 200, ticket);
        System.out.println("[TICKETS] Ticket #" + ticketId + " récupéré");
    }

    /**
     * POST /tickets
     */
    private void handleCreateTicket(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        CreateTicketRequest request = gson.fromJson(requestBody, CreateTicketRequest.class);

        if (request == null || request.getTitle() == null || request.getTitle().isEmpty()) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "Le titre du ticket ne peut pas être vide");
            return;
        }

        // Créer le ticket via ApplicationState
        TicketDTO createdTicket = appState.createTicket(request);

        sendJsonResponse(exchange, 201, createdTicket);
        System.out.println("[TICKETS] Ticket #" + createdTicket.getTicketID() + " créé: " + createdTicket.getTitle());
    }

    /**
     * PUT /tickets/{id}
     */
    private void handleUpdateTicket(HttpExchange exchange, String path) throws IOException {
        Integer ticketId = extractIdFromPath(path);

        if (ticketId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID ticket invalide");
            return;
        }

        String requestBody = readRequestBody(exchange);
        UpdateTicketRequest request = gson.fromJson(requestBody, UpdateTicketRequest.class);

        TicketDTO updatedTicket = appState.updateTicket(ticketId, request);

        if (updatedTicket == null) {
            sendErrorResponse(exchange, 404, "NOT_FOUND", "Ticket #" + ticketId + " introuvable");
            return;
        }

        sendJsonResponse(exchange, 200, updatedTicket);
        System.out.println("[TICKETS] Ticket #" + ticketId + " modifié");
    }

    /**
     * DELETE /tickets/{id}
     */
    private void handleDeleteTicket(HttpExchange exchange, String path) throws IOException {
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
        System.out.println("[TICKETS] Ticket #" + ticketId + " supprimé");
    }

    /**
     * Gestion des commentaires
     */
    private void handleCommentsEndpoints(HttpExchange exchange, String method, String path) throws IOException {
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
            System.out.println("[COMMENTS] Commentaire ajouté au ticket #" + ticketId);
        } else {
            sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED", "Méthode non autorisée");
        }
    }

    /**
     * Gestion des statuts
     */
    private void handleStatusEndpoints(HttpExchange exchange, String method, String path) throws IOException {
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
        } else if ("PATCH".equals(method)) {
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
                System.out.println("[STATUS] Statut du ticket #" + ticketId + " changé vers: " + request.getNewStatus());
            } catch (IllegalStateException e) {
                sendErrorResponse(exchange, 400, "INVALID_TRANSITION", e.getMessage());
            }
        } else {
            sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED", "Méthode non autorisée");
        }
    }

    /**
     * Assignation de ticket
     */
    private void handleAssignmentEndpoint(HttpExchange exchange, String method, String path) throws IOException {
        if (!"PATCH".equals(method)) {
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
        System.out.println("[ASSIGNMENT] Ticket #" + ticketId + " assigné à l'utilisateur #" + request.getUserID());
    }

    /**
     * Export PDF
     */
    private void handleExportPdfEndpoint(HttpExchange exchange, String method, String path) throws IOException {
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
        System.out.println("[EXPORT] Ticket #" + ticketId + " exporté en PDF");
    }
}
