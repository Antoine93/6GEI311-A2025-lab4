import java.util.ArrayList;
import java.util.List;

import core.content.*;
import core.entities.*;
import core.exporter.*;

/**
 * MainConsole
 * Tests console pour démonstration des patterns (Composite, Strategy, Visitor)
 * Pour lancer l'application GUI, utilisez Main.java
 */
public class MainConsole {
    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("   Test du Systeme de Gestion de Tickets - PARTIE 2");
        System.out.println("   Demonstration des patrons Composite et Strategy");
        System.out.println("==============================================================\n");

        // Creation des utilisateurs
        User user1 = new User(1, "Utilisateur1", "utilisateur1@uqac.ca", "Developpeur");
        User user2 = new User(2, "Utilisateur2", "utilisateur2@uqac.ca", "Testeur");
        Admin admin = new Admin(100, "Utilisateur3", "utilisateur3@uqac.ca");

        System.out.println("--------------------------------------------------------------");
        System.out.println("TEST 1 : Ticket avec description TEXTUELLE simple");
        System.out.println("--------------------------------------------------------------\n");

        TextContent simpleText = new TextContent(
            "L'application crash lorsqu'on clique sur le bouton de connexion apres 3 tentatives echouees."
        );
        Ticket ticket1 = user1.createTicket("Bug critique - Crash a la connexion",
                                            simpleText,
                                            "Haute");

        System.out.println("\n-> Consultation dans la plateforme :");
        user1.viewTicket(ticket1);

        System.out.println("-> Export PDF :");
        String pdf1 = ticket1.exportToPDF();
        System.out.println(pdf1);

        System.out.println("\n--------------------------------------------------------------");
        System.out.println("TEST 2 : Ticket avec IMAGE et legende");
        System.out.println("--------------------------------------------------------------\n");

        ImageContent screenshot = new ImageContent(
            "/captures/erreur_interface_2024.png",
            "Message d'erreur affiche lors du crash"
        );
        Ticket ticket2 = new Ticket(2002, "Amelioration UI", screenshot, "Moyenne");

        System.out.println("-> Consultation dans la plateforme :");
        ticket2.displayDescription();

        System.out.println("-> Export PDF :");
        String pdf2 = ticket2.exportToPDF();
        System.out.println(pdf2);

        System.out.println("\n--------------------------------------------------------------");
        System.out.println("TEST 3 : Ticket avec VIDEO");
        System.out.println("--------------------------------------------------------------\n");

        VideoContent screencast = new VideoContent(
            "/videos/demo_bug_reproduction.mp4",
            125  // 2 minutes 5 secondes
        );
        Ticket ticket3 = new Ticket(2003, "Bug - Reproduction en video", screencast, "Haute");

        System.out.println("-> Consultation dans la plateforme :");
        ticket3.displayDescription();

        System.out.println("-> Export PDF :");
        String pdf3 = ticket3.exportToPDF();
        System.out.println(pdf3);

        System.out.println("\n--------------------------------------------------------------");
        System.out.println("TEST 4 : Ticket avec DESCRIPTION COMPOSITE (Patron Composite)");
        System.out.println("         Texte + Image + Video combines");
        System.out.println("--------------------------------------------------------------\n");

        CompositeContent richDescription = new CompositeContent();

        richDescription.add(new TextContent(
            "Probleme majeur detecte lors de l'authentification multi-facteurs. " +
            "Le systeme ne valide pas correctement le code 2FA apres 3 tentatives."
        ));

        richDescription.add(new ImageContent(
            "/captures/2fa_error_screen.png",
            "Ecran d'erreur 2FA"
        ));

        richDescription.add(new TextContent(
            "Etapes de reproduction : 1) Se connecter avec identifiants valides " +
            "2) Entrer un code 2FA incorrect 3 fois 3) Observer l'erreur"
        ));

        richDescription.add(new VideoContent(
            "/videos/2fa_bug_complete.mp4",
            180  // 3 minutes
        ));

        Ticket ticket4 = new Ticket(2004, "Bug 2FA - Description complete", richDescription, "Critique");

        System.out.println("-> Consultation dans la plateforme :");
        ticket4.displayDescription();

        System.out.println("-> Export PDF :");
        String pdf4 = ticket4.exportToPDF();
        System.out.println(pdf4);

        System.out.println("\n--------------------------------------------------------------");
        System.out.println("TEST 5 : Modification dynamique de la description");
        System.out.println("--------------------------------------------------------------\n");

        System.out.println("Creation d'un ticket avec texte simple...");
        Ticket ticket5 = user2.createTicket("Documentation manquante",
                                           new TextContent("Il manque des exemples dans la documentation de l'API"),
                                           "Basse");

        ticket5.displayDescription();

        System.out.println("Ajout d'images pour enrichir la description...");
        CompositeContent enrichedDesc = new CompositeContent();
        enrichedDesc.add(new TextContent("Il manque des exemples dans la documentation de l'API"));
        enrichedDesc.add(new ImageContent("/doc/api_missing_example1.png", "Section 3.2 incomplete"));
        enrichedDesc.add(new ImageContent("/doc/api_missing_example2.png", "Section 4.1 incomplete"));

        ticket5.setDescription(enrichedDesc);
        System.out.println("\n[OK] Description enrichie avec succes!\n");

        ticket5.displayDescription();

        System.out.println("\n--------------------------------------------------------------");
        System.out.println("TEST 6 : Gestion administrative des tickets");
        System.out.println("--------------------------------------------------------------\n");

        admin.assignTicket(ticket4, user1.getUserID());
        ticket4.addComment("Investigation en cours - logs analyses");
        ticket4.addComment("Bug reproduit en environnement de test");
        ticket4.addComment("Correctif applique - en attente de validation");

        // Mise a jour du statut avec validation des transitions
        ticket4.updateStatus(TicketStatus.VALIDATION);

        // Affichage de l'historique des commentaires
        ticket4.displayComments();

        System.out.println("\n--------------------------------------------------------------");
        System.out.println("TEST 7 : Validation des transitions de statut");
        System.out.println("--------------------------------------------------------------\n");

        System.out.println("Statut actuel du ticket #" + ticket4.getTicketID() + " : " + ticket4.getStatus());
        System.out.println("Transitions autorisees : " + ticket4.getStatus().getAvailableTransitions());

        // Transition valide : VALIDATION -> TERMINE
        ticket4.updateStatus(TicketStatus.TERMINE);
        System.out.println("[OK] Transition validee avec succes\n");

        // Test de transition invalide (commente pour eviter l'exception)
        System.out.println("Tentative de transition invalide (TERMINE -> OUVERT)...");
        try {
            ticket4.updateStatus(TicketStatus.OUVERT);
        } catch (IllegalStateException e) {
            System.out.println("[ERREUR] Transition bloquee : " + e.getMessage());
        }

        System.out.println("\n--------------------------------------------------------------");
        System.out.println("TEST 8 : Admin herite de User - Creation de ticket par Admin");
        System.out.println("--------------------------------------------------------------\n");

        // Admin peut maintenant creer des tickets (herite de User)
        Ticket adminTicket = admin.createTicket(
            "Ticket cree par Admin",
            new TextContent("Les administrateurs peuvent maintenant creer des tickets directement"),
            "Haute"
        );

        System.out.println("[OK] Admin (heritant de User) peut creer des tickets");
        System.out.println("Ticket cree : #" + adminTicket.getTicketID() + " - Statut: " + adminTicket.getStatus());

        System.out.println("\n--------------------------------------------------------------");
        System.out.println("TEST 9 : Vue d'ensemble - Consultation de tous les tickets");
        System.out.println("--------------------------------------------------------------\n");

        List<Ticket> allTickets = new ArrayList<>();
        allTickets.add(ticket1);
        allTickets.add(ticket2);
        allTickets.add(ticket3);
        allTickets.add(ticket4);
        allTickets.add(ticket5);
        allTickets.add(adminTicket);

        admin.viewAllTickets(allTickets);

        System.out.println("\n==============================================================");
        System.out.println("              Tests termines avec succes!");
        System.out.println("");
        System.out.println("  Ameliorations implementees :");
        System.out.println("  - Enum TicketStatus avec validation de transitions");
        System.out.println("  - Stockage et historique des commentaires");
        System.out.println("  - Admin herite de User (conformite au diagramme)");
        System.out.println("==============================================================");
    }
}
