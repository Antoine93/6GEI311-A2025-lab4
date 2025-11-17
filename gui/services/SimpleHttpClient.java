package gui.services;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * SimpleHttpClient - Client HTTP simple utilisant java.net.HttpURLConnection
 *
 * Gère les requêtes GET, POST, PUT, PATCH, DELETE vers l'API REST
 * Supporte JSON et gestion des tokens d'authentification
 */
public class SimpleHttpClient {

    private String baseUrl;
    private String authToken;

    public SimpleHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.authToken = null;
    }

    /**
     * Définit le token d'authentification pour les requêtes suivantes
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    /**
     * Retire le token d'authentification
     */
    public void clearAuthToken() {
        this.authToken = null;
    }

    /**
     * Envoie une requête GET
     * @param endpoint Endpoint relatif (ex: "/tickets")
     * @return HttpResponse contenant status, body, headers
     * @throws IOException si erreur réseau
     */
    public HttpResponse get(String endpoint) throws IOException {
        return request("GET", endpoint, null);
    }

    /**
     * Envoie une requête POST avec body JSON
     * @param endpoint Endpoint relatif
     * @param jsonBody Corps de la requête en JSON
     * @return HttpResponse
     * @throws IOException si erreur réseau
     */
    public HttpResponse post(String endpoint, String jsonBody) throws IOException {
        return request("POST", endpoint, jsonBody);
    }

    /**
     * Envoie une requête PUT avec body JSON
     */
    public HttpResponse put(String endpoint, String jsonBody) throws IOException {
        return request("PUT", endpoint, jsonBody);
    }

    /**
     * Envoie une requête PATCH avec body JSON
     */
    public HttpResponse patch(String endpoint, String jsonBody) throws IOException {
        return request("PATCH", endpoint, jsonBody);
    }

    /**
     * Envoie une requête DELETE
     */
    public HttpResponse delete(String endpoint) throws IOException {
        return request("DELETE", endpoint, null);
    }

    /**
     * Méthode générique pour envoyer une requête HTTP
     */
    private HttpResponse request(String method, String endpoint, String jsonBody) throws IOException {
        String url = baseUrl + endpoint;
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000); // 5 secondes
            conn.setReadTimeout(10000); // 10 secondes

            // Headers
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");

            // Authentification
            if (authToken != null && !authToken.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            // Body (pour POST, PUT, PATCH)
            if (jsonBody != null && !jsonBody.isEmpty()) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            // Récupérer la réponse
            int statusCode = conn.getResponseCode();
            String responseBody;

            if (statusCode >= 200 && statusCode < 300) {
                // Success
                responseBody = readStream(conn.getInputStream());
            } else {
                // Error
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    responseBody = readStream(errorStream);
                } else {
                    responseBody = "";
                }
            }

            return new HttpResponse(statusCode, responseBody);

        } catch (ConnectException e) {
            throw new IOException("Impossible de se connecter au serveur : " + baseUrl + ". Vérifiez que le serveur est démarré.", e);
        } catch (SocketTimeoutException e) {
            throw new IOException("Timeout lors de la connexion au serveur : " + baseUrl, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Lit un InputStream et retourne une String
     */
    private String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * Classe interne pour encapsuler une réponse HTTP
     */
    public static class HttpResponse {
        private int statusCode;
        private String body;

        public HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }

        public boolean isClientError() {
            return statusCode >= 400 && statusCode < 500;
        }

        public boolean isServerError() {
            return statusCode >= 500;
        }

        @Override
        public String toString() {
            return "HttpResponse{statusCode=" + statusCode + ", body='" + body + "'}";
        }
    }
}
