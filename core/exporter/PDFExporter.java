package core.exporter;

import core.content.*;

/**
 * PDFExporter - Implementation de la strategie d'export PDF
 * Simule la generation d'un document PDF en format texte
 */
public class PDFExporter implements Exporter {

    private static final String PDF_HEADER = "==================================================\n" +
                                              "     EXPORT PDF - TICKET DESCRIPTION\n" +
                                              "==================================================\n\n";
    private static final String PDF_FOOTER = "\n==================================================\n" +
                                              "     Fin du document PDF\n" +
                                              "==================================================";

    @Override
    public String export(Content content) {
        if (content == null) {
            return PDF_HEADER + "[Aucun contenu]\n" + PDF_FOOTER;
        }

        StringBuilder pdf = new StringBuilder();
        pdf.append(PDF_HEADER);
        pdf.append(content.accept(this));
        pdf.append(PDF_FOOTER);

        return pdf.toString();
    }

    @Override
    public String exportText(TextContent textContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("SECTION TEXTE\n");
        sb.append("--------------------------------------------------\n");
        sb.append(textContent.getText()).append("\n");
        sb.append("--------------------------------------------------\n\n");
        return sb.toString();
    }

    @Override
    public String exportImage(ImageContent imageContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("SECTION IMAGE\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Fichier : ").append(imageContent.getImagePath()).append("\n");

        if (!imageContent.getCaption().isEmpty()) {
            sb.append("Legende : ").append(imageContent.getCaption()).append("\n");
        }

        sb.append("[IMAGE PLACEHOLDER]\n");
        sb.append("--------------------------------------------------\n\n");

        return sb.toString();
    }

    @Override
    public String exportVideo(VideoContent videoContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("SECTION VIDEO\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Fichier : ").append(videoContent.getVideoPath()).append("\n");

        if (videoContent.getDuration() > 0) {
            int minutes = videoContent.getDuration() / 60;
            int seconds = videoContent.getDuration() % 60;
            sb.append("Duree   : ").append(String.format("%d min %02d sec", minutes, seconds)).append("\n");
        }

        sb.append("[VIDEO PLACEHOLDER]\n");
        sb.append("--------------------------------------------------\n\n");

        return sb.toString();
    }

    @Override
    public String exportComposite(CompositeContent compositeContent) {
        StringBuilder sb = new StringBuilder();

        if (compositeContent.isEmpty()) {
            sb.append("[Contenu composite vide]\n\n");
            return sb.toString();
        }

        sb.append("==================================================\n");
        sb.append("DESCRIPTION COMPOSITE - ")
          .append(compositeContent.size())
          .append(" element(s)\n");
        sb.append("==================================================\n\n");

        int index = 1;
        for (Content child : compositeContent.getChildren()) {
            sb.append("--- Element ").append(index++).append(" ---\n\n");
            sb.append(child.accept(this));
        }

        return sb.toString();
    }

    /**
     * Genere le nom de fichier PDF suggere
     * @param ticketId ID du ticket
     * @return Nom de fichier
     */
    public String generateFileName(int ticketId) {
        return "ticket_" + ticketId + "_description.pdf";
    }
}
