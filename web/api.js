/**
 * API Client - Communication avec l'API REST
 * Encapsule tous les appels HTTP vers le serveur
 */

const API_BASE_URL = 'http://localhost:8080/api/v1';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('authToken');
        this.currentUser = JSON.parse(localStorage.getItem('currentUser'));
    }

    /**
     * Définit le token d'authentification
     */
    setToken(token) {
        this.token = token;
        localStorage.setItem('authToken', token);
    }

    /**
     * Définit l'utilisateur actuel
     */
    setCurrentUser(user) {
        this.currentUser = user;
        localStorage.setItem('currentUser', JSON.stringify(user));
    }

    /**
     * Déconnexion
     */
    clearAuth() {
        this.token = null;
        this.currentUser = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
    }

    /**
     * Vérifie si l'utilisateur est authentifié
     */
    isAuthenticated() {
        return this.token !== null && this.token !== '';
    }

    /**
     * Effectue une requête HTTP
     */
    async request(method, endpoint, body = null, requireAuth = true) {
        const headers = {
            'Content-Type': 'application/json',
        };

        if (requireAuth && this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        const options = {
            method,
            headers,
        };

        if (body) {
            options.body = JSON.stringify(body);
        }

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

            // No Content (204)
            if (response.status === 204) {
                return null;
            }

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || `Erreur HTTP ${response.status}`);
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    // ========================================================================
    // AUTHENTIFICATION
    // ========================================================================

    /**
     * POST /auth/login
     */
    async login(userID) {
        const response = await this.request('POST', '/auth/login', { userID }, false);
        this.setToken(response.token);
        this.setCurrentUser(response.user);
        return response.user;
    }

    /**
     * POST /auth/logout
     */
    async logout() {
        try {
            await this.request('POST', '/auth/logout');
        } finally {
            this.clearAuth();
        }
    }

    /**
     * GET /auth/session
     */
    async getSession() {
        return await this.request('GET', '/auth/session');
    }

    // ========================================================================
    // UTILISATEURS
    // ========================================================================

    /**
     * GET /users
     */
    async getAllUsers() {
        return await this.request('GET', '/users');
    }

    /**
     * GET /users/{id}
     */
    async getUserById(userID) {
        return await this.request('GET', `/users/${userID}`);
    }

    // ========================================================================
    // TICKETS
    // ========================================================================

    /**
     * GET /tickets
     */
    async getAllTickets(filters = {}) {
        let endpoint = '/tickets';
        const params = new URLSearchParams();

        if (filters.status) params.append('status', filters.status);
        if (filters.priority) params.append('priority', filters.priority);
        if (filters.assignedTo) params.append('assignedTo', filters.assignedTo);

        const queryString = params.toString();
        if (queryString) {
            endpoint += '?' + queryString;
        }

        return await this.request('GET', endpoint);
    }

    /**
     * GET /tickets/{id}
     */
    async getTicketById(ticketID) {
        return await this.request('GET', `/tickets/${ticketID}`);
    }

    /**
     * POST /tickets
     */
    async createTicket(title, priority, descriptionContent) {
        return await this.request('POST', '/tickets', {
            title,
            priority,
            descriptionContent
        });
    }

    /**
     * PUT /tickets/{id}
     */
    async updateTicket(ticketID, title, priority, descriptionContent = null) {
        const body = {};
        if (title) body.title = title;
        if (priority) body.priority = priority;
        if (descriptionContent) body.descriptionContent = descriptionContent;

        return await this.request('PUT', `/tickets/${ticketID}`, body);
    }

    /**
     * DELETE /tickets/{id}
     */
    async deleteTicket(ticketID) {
        return await this.request('DELETE', `/tickets/${ticketID}`);
    }

    // ========================================================================
    // COMMENTAIRES
    // ========================================================================

    /**
     * GET /tickets/{id}/comments
     */
    async getTicketComments(ticketID) {
        return await this.request('GET', `/tickets/${ticketID}/comments`);
    }

    /**
     * POST /tickets/{id}/comments
     */
    async addComment(ticketID, text) {
        return await this.request('POST', `/tickets/${ticketID}/comments`, { text });
    }

    // ========================================================================
    // STATUTS
    // ========================================================================

    /**
     * GET /tickets/{id}/status
     */
    async getAvailableTransitions(ticketID) {
        return await this.request('GET', `/tickets/${ticketID}/status`);
    }

    /**
     * POST /tickets/{id}/status
     */
    async changeTicketStatus(ticketID, newStatus) {
        return await this.request('POST', `/tickets/${ticketID}/status`, { newStatus });
    }

    // ========================================================================
    // ASSIGNATION
    // ========================================================================

    /**
     * POST /tickets/{id}/assignment
     */
    async assignTicket(ticketID, userID) {
        return await this.request('POST', `/tickets/${ticketID}/assignment`, { userID });
    }

    // ========================================================================
    // EXPORT
    // ========================================================================

    /**
     * GET /tickets/{id}/export/pdf
     */
    async exportTicketToPDF(ticketID) {
        // Note: Cette méthode retourne du texte, pas du JSON
        const headers = {
            'Authorization': `Bearer ${this.token}`
        };

        const response = await fetch(`${API_BASE_URL}/tickets/${ticketID}/export/pdf`, {
            method: 'GET',
            headers
        });

        if (!response.ok) {
            throw new Error(`Erreur HTTP ${response.status}`);
        }

        return await response.text();
    }
}

// Instance globale
const api = new ApiClient();
