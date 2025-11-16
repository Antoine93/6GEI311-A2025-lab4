package gui.models;

/**
 * UserDTO (Data Transfer Object)
 * Object de transfert pour afficher les informations utilisateur dans le View
 * Separe le View du Model metier
 */
public class UserDTO {
    private final int userID;
    private final String name;
    private final String role;
    private final boolean isAdmin;

    public UserDTO(int userID, String name, String role, boolean isAdmin) {
        this.userID = userID;
        this.name = name;
        this.role = role;
        this.isAdmin = isAdmin;
    }

    public int getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isDeveloper() {
        return "Developpeur".equals(role);
    }

    public boolean hasFullAccess() {
        return isAdmin || isDeveloper();
    }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}
