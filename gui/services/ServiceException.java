package gui.services;

/**
 * ServiceException - Exception levée lors d'erreurs de communication avec le service
 *
 * Encapsule les erreurs HTTP, réseau, et de parsing JSON
 */
public class ServiceException extends Exception {

    private int httpStatusCode;
    private String errorCode;

    public ServiceException(String message) {
        super(message);
        this.httpStatusCode = -1;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = -1;
    }

    public ServiceException(int httpStatusCode, String errorCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    public ServiceException(int httpStatusCode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    /**
     * Retourne le code HTTP (400, 401, 403, 404, 500) ou -1 si non applicable
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Retourne le code d'erreur métier (VALIDATION_ERROR, UNAUTHORIZED, etc.)
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Vérifie si l'erreur est une erreur d'authentification
     */
    public boolean isAuthenticationError() {
        return httpStatusCode == 401;
    }

    /**
     * Vérifie si l'erreur est une erreur de permissions
     */
    public boolean isPermissionError() {
        return httpStatusCode == 403;
    }

    /**
     * Vérifie si l'erreur est une erreur de validation
     */
    public boolean isValidationError() {
        return httpStatusCode == 400;
    }

    /**
     * Vérifie si l'erreur est une erreur de ressource non trouvée
     */
    public boolean isNotFoundError() {
        return httpStatusCode == 404;
    }

    /**
     * Vérifie si l'erreur est une erreur serveur
     */
    public boolean isServerError() {
        return httpStatusCode >= 500;
    }

    @Override
    public String toString() {
        if (httpStatusCode > 0) {
            return "ServiceException{" +
                    "httpStatusCode=" + httpStatusCode +
                    ", errorCode='" + errorCode + '\'' +
                    ", message='" + getMessage() + '\'' +
                    '}';
        }
        return "ServiceException{message='" + getMessage() + "'}";
    }
}
