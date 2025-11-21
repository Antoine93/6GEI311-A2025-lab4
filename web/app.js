/**
 * Application principale - Logique de l'interface web
 */

// État global
let tickets = [];
let selectedTicketId = null;
let allUsers = [];

// Éléments DOM
const loginPage = document.getElementById('login-page');
const mainPage = document.getElementById('main-page');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const currentUserName = document.getElementById('current-user-name');
const logoutBtn = document.getElementById('logout-btn');
const refreshBtn = document.getElementById('refresh-btn');
const newTicketBtn = document.getElementById('new-ticket-btn');
const ticketsList = document.getElementById('tickets-list');
const ticketDetailsSection = document.getElementById('ticket-details-section');
const ticketDetails = document.getElementById('ticket-details');
const statusFilter = document.getElementById('status-filter');

// Modals
const newTicketModal = document.getElementById('new-ticket-modal');
const commentModal = document.getElementById('comment-modal');
const statusModal = document.getElementById('status-modal');
const assignModal = document.getElementById('assign-modal');

// Boutons actions
const addCommentBtn = document.getElementById('add-comment-btn');
const changeStatusBtn = document.getElementById('change-status-btn');
const assignTicketBtn = document.getElementById('assign-ticket-btn');
const exportPdfBtn = document.getElementById('export-pdf-btn');
const deleteTicketBtn = document.getElementById('delete-ticket-btn');

// ============================================================================
// INITIALISATION
// ============================================================================

document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
    setupEventListeners();
});

function initializeApp() {
    // Vérifier si l'utilisateur est déjà connecté
    if (api.isAuthenticated()) {
        showMainPage();
        loadTickets();
        loadUsers();
    } else {
        showLoginPage();
    }
}

function setupEventListeners() {
    // Login
    loginForm.addEventListener('submit', handleLogin);
    logoutBtn.addEventListener('click', handleLogout);

    // Toolbar
    refreshBtn.addEventListener('click', () => loadTickets());
    newTicketBtn.addEventListener('click', () => showModal(newTicketModal));
    statusFilter.addEventListener('change', () => loadTickets());

    // Forms
    document.getElementById('new-ticket-form').addEventListener('submit', handleCreateTicket);
    document.getElementById('comment-form').addEventListener('submit', handleAddComment);
    document.getElementById('status-form').addEventListener('submit', handleChangeStatus);
    document.getElementById('assign-form').addEventListener('submit', handleAssignTicket);

    // Boutons actions ticket
    addCommentBtn.addEventListener('click', () => showModal(commentModal));
    changeStatusBtn.addEventListener('click', showStatusModal);
    assignTicketBtn.addEventListener('click', showAssignModal);
    exportPdfBtn.addEventListener('click', handleExportPdf);
    deleteTicketBtn.addEventListener('click', handleDeleteTicket);

    // Fermeture des modals
    setupModalClosing();
}

// ============================================================================
// AUTHENTIFICATION
// ============================================================================

async function handleLogin(e) {
    e.preventDefault();
    const userID = parseInt(document.getElementById('user-id').value);

    try {
        await api.login(userID);
        loginError.style.display = 'none';
        showMainPage();
        await loadTickets();
        await loadUsers();
    } catch (error) {
        loginError.textContent = 'Erreur de connexion: ' + error.message;
        loginError.style.display = 'block';
    }
}

async function handleLogout() {
    try {
        await api.logout();
    } catch (error) {
        console.error('Erreur lors de la déconnexion:', error);
    }
    showLoginPage();
}

function showLoginPage() {
    loginPage.style.display = 'flex';
    mainPage.style.display = 'none';
}

function showMainPage() {
    loginPage.style.display = 'none';
    mainPage.style.display = 'block';
    currentUserName.textContent = `${api.currentUser.name} (${api.currentUser.role})`;

    // Afficher le bouton supprimer seulement pour les admins
    if (api.currentUser.isAdmin) {
        deleteTicketBtn.style.display = 'inline-flex';
    }
}

// ============================================================================
// CHARGEMENT DES DONNÉES
// ============================================================================

async function loadTickets() {
    try {
        const filters = {};
        if (statusFilter.value) {
            filters.status = statusFilter.value;
        }

        tickets = await api.getAllTickets(filters);
        displayTickets();
    } catch (error) {
        console.error('Erreur lors du chargement des tickets:', error);
        showError('Impossible de charger les tickets');
    }
}

async function loadUsers() {
    try {
        allUsers = await api.getAllUsers();
    } catch (error) {
        console.error('Erreur lors du chargement des utilisateurs:', error);
    }
}

// ============================================================================
// AFFICHAGE
// ============================================================================

function displayTickets() {
    ticketsList.innerHTML = '';

    if (tickets.length === 0) {
        ticketsList.innerHTML = '<p style="text-align: center; color: var(--text-secondary); padding: 40px;">Aucun ticket à afficher</p>';
        return;
    }

    tickets.forEach(ticket => {
        const card = createTicketCard(ticket);
        ticketsList.appendChild(card);
    });
}

function createTicketCard(ticket) {
    const card = document.createElement('div');
    card.className = 'ticket-card';
    card.setAttribute('data-ticket-id', ticket.ticketID);
    if (selectedTicketId === ticket.ticketID) {
        card.classList.add('selected');
    }

    card.innerHTML = `
        <div class="ticket-card-header">
            <span class="ticket-id">#${ticket.ticketID}</span>
            <span class="badge badge-status">${ticket.status}</span>
        </div>
        <div class="ticket-title">${escapeHtml(ticket.title)}</div>
        <div class="ticket-meta">
            <span class="badge badge-priority ${ticket.priority}">${ticket.priority}</span>
            <span>👤 ${escapeHtml(ticket.createdByName)}</span>
            ${ticket.assignedToName && ticket.assignedToName !== 'Non assigné'
                ? `<span>➡️ ${escapeHtml(ticket.assignedToName)}</span>`
                : ''}
        </div>
    `;

    card.addEventListener('click', () => selectTicket(ticket.ticketID));
    return card;
}

async function selectTicket(ticketID) {
    selectedTicketId = ticketID;

    // Mettre à jour l'affichage de sélection
    document.querySelectorAll('.ticket-card').forEach(card => {
        card.classList.remove('selected');
    });

    // Sélectionner la carte correspondante
    const selectedCard = document.querySelector(`.ticket-card[data-ticket-id="${ticketID}"]`);
    if (selectedCard) {
        selectedCard.classList.add('selected');
    }

    // Charger et afficher les détails
    try {
        const ticket = await api.getTicketById(ticketID);
        const comments = await api.getTicketComments(ticketID);
        displayTicketDetails(ticket, comments);
        ticketDetailsSection.style.display = 'block';
    } catch (error) {
        console.error('Erreur lors du chargement du ticket:', error);
        showError('Impossible de charger les détails du ticket');
    }
}

function displayTicketDetails(ticket, comments) {
    let html = `
        <div class="detail-row">
            <span class="label">ID:</span>
            <span class="value">#${ticket.ticketID}</span>
        </div>
        <div class="detail-row">
            <span class="label">Titre:</span>
            <span class="value">${escapeHtml(ticket.title)}</span>
        </div>
        <div class="detail-row">
            <span class="label">Statut:</span>
            <span class="value"><span class="badge badge-status">${ticket.status}</span></span>
        </div>
        <div class="detail-row">
            <span class="label">Priorité:</span>
            <span class="value"><span class="badge badge-priority ${ticket.priority}">${ticket.priority}</span></span>
        </div>
        <div class="detail-row">
            <span class="label">Créé par:</span>
            <span class="value">${escapeHtml(ticket.createdByName)}</span>
        </div>
        <div class="detail-row">
            <span class="label">Assigné à:</span>
            <span class="value">${escapeHtml(ticket.assignedToName || 'Non assigné')}</span>
        </div>
        <div class="detail-row">
            <span class="label">Date de création:</span>
            <span class="value">${ticket.creationDate}</span>
        </div>
        <div class="detail-row">
            <span class="label">Description:</span>
            <div class="value" style="white-space: pre-wrap; margin-top: 8px;">${escapeHtml(ticket.description)}</div>
        </div>
    `;

    if (comments && comments.length > 0) {
        html += `
            <div class="comments-section">
                <h3>💬 Commentaires (${comments.length})</h3>
                ${comments.map((comment, idx) => `
                    <div class="comment-item">
                        <strong>[${idx + 1}]</strong> ${escapeHtml(comment)}
                    </div>
                `).join('')}
            </div>
        `;
    }

    ticketDetails.innerHTML = html;
}

// ============================================================================
// ACTIONS SUR LES TICKETS
// ============================================================================

async function handleCreateTicket(e) {
    e.preventDefault();

    const title = document.getElementById('ticket-title').value;
    const priority = document.getElementById('ticket-priority').value;
    const description = document.getElementById('ticket-description').value;

    const descriptionContent = [{
        type: 'TEXT',
        data: description,
        metadata: null
    }];

    try {
        await api.createTicket(title, priority, descriptionContent);
        closeModal(newTicketModal);
        document.getElementById('new-ticket-form').reset();
        await loadTickets();
        showSuccess('Ticket créé avec succès!');
    } catch (error) {
        console.error('Erreur lors de la création du ticket:', error);
        showError('Impossible de créer le ticket: ' + error.message);
    }
}

async function handleAddComment(e) {
    e.preventDefault();

    if (!selectedTicketId) return;

    const text = document.getElementById('comment-text').value;

    try {
        await api.addComment(selectedTicketId, text);
        closeModal(commentModal);
        document.getElementById('comment-form').reset();
        await selectTicket(selectedTicketId); // Recharger les détails
        showSuccess('Commentaire ajouté avec succès!');
    } catch (error) {
        console.error('Erreur lors de l\'ajout du commentaire:', error);
        showError('Impossible d\'ajouter le commentaire: ' + error.message);
    }
}

async function showStatusModal() {
    if (!selectedTicketId) return;

    try {
        const transitions = await api.getAvailableTransitions(selectedTicketId);
        const select = document.getElementById('new-status');
        select.innerHTML = transitions.map(status =>
            `<option value="${status}">${status}</option>`
        ).join('');
        showModal(statusModal);
    } catch (error) {
        console.error('Erreur lors de la récupération des transitions:', error);
        showError('Impossible de charger les transitions disponibles');
    }
}

async function handleChangeStatus(e) {
    e.preventDefault();

    if (!selectedTicketId) return;

    const newStatus = document.getElementById('new-status').value;

    try {
        await api.changeTicketStatus(selectedTicketId, newStatus);
        closeModal(statusModal);
        await selectTicket(selectedTicketId); // Recharger les détails
        await loadTickets(); // Recharger la liste
        showSuccess('Statut modifié avec succès!');
    } catch (error) {
        console.error('Erreur lors du changement de statut:', error);
        showError('Impossible de changer le statut: ' + error.message);
    }
}

async function showAssignModal() {
    if (!selectedTicketId) return;

    const select = document.getElementById('assign-user');
    select.innerHTML = allUsers.map(user =>
        `<option value="${user.userID}">${user.name} (${user.role})</option>`
    ).join('');
    showModal(assignModal);
}

async function handleAssignTicket(e) {
    e.preventDefault();

    if (!selectedTicketId) return;

    const userID = parseInt(document.getElementById('assign-user').value);

    try {
        await api.assignTicket(selectedTicketId, userID);
        closeModal(assignModal);
        await selectTicket(selectedTicketId); // Recharger les détails
        await loadTickets(); // Recharger la liste
        showSuccess('Ticket assigné avec succès!');
    } catch (error) {
        console.error('Erreur lors de l\'assignation:', error);
        showError('Impossible d\'assigner le ticket: ' + error.message);
    }
}

async function handleExportPdf() {
    if (!selectedTicketId) return;

    try {
        const pdfContent = await api.exportTicketToPDF(selectedTicketId);

        // Créer un blob et télécharger
        const blob = new Blob([pdfContent], { type: 'text/plain' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `ticket_${selectedTicketId}_export.txt`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

        showSuccess('Export PDF réussi!');
    } catch (error) {
        console.error('Erreur lors de l\'export:', error);
        showError('Impossible d\'exporter le ticket: ' + error.message);
    }
}

async function handleDeleteTicket() {
    if (!selectedTicketId) return;

    if (!confirm(`Êtes-vous sûr de vouloir supprimer le ticket #${selectedTicketId} ?`)) {
        return;
    }

    try {
        await api.deleteTicket(selectedTicketId);
        selectedTicketId = null;
        ticketDetailsSection.style.display = 'none';
        await loadTickets();
        showSuccess('Ticket supprimé avec succès!');
    } catch (error) {
        console.error('Erreur lors de la suppression:', error);
        showError('Impossible de supprimer le ticket: ' + error.message);
    }
}

// ============================================================================
// MODALS
// ============================================================================

function showModal(modal) {
    modal.style.display = 'block';
}

function closeModal(modal) {
    modal.style.display = 'none';
}

function setupModalClosing() {
    // Fermer avec le X
    document.querySelectorAll('.close').forEach(closeBtn => {
        closeBtn.addEventListener('click', function() {
            const modal = this.closest('.modal');
            closeModal(modal);
        });
    });

    // Fermer avec le bouton Annuler
    document.querySelectorAll('.close-modal').forEach(btn => {
        btn.addEventListener('click', function() {
            const modal = this.closest('.modal');
            closeModal(modal);
        });
    });

    // Fermer en cliquant à l'extérieur
    window.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal')) {
            closeModal(event.target);
        }
    });
}

// ============================================================================
// UTILITAIRES
// ============================================================================

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showError(message) {
    // Créer une notification temporaire
    const notification = document.createElement('div');
    notification.className = 'error-message';
    notification.textContent = message;
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.zIndex = '10000';
    notification.style.maxWidth = '400px';
    document.body.appendChild(notification);

    setTimeout(() => {
        document.body.removeChild(notification);
    }, 5000);
}

function showSuccess(message) {
    const notification = document.createElement('div');
    notification.className = 'success-message';
    notification.textContent = message;
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.zIndex = '10000';
    notification.style.maxWidth = '400px';
    document.body.appendChild(notification);

    setTimeout(() => {
        document.body.removeChild(notification);
    }, 3000);
}
