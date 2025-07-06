/*
 * Located in: src/chatpave/auth/AuthManager.java
 */
package chatpave.auth;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Manages user authentication state.
 * This version correctly saves and retrieves the user's ID along with the token.
 */
public class AuthManager {

    private static final String RECORD_STORE_NAME = "ChatpaveAuth";
    private static String authToken = null;
    private static int currentUserId = -1; // -1 means no user is logged in
    private static final long EXPIRATION_TIME = 30L * 24L * 60L * 60L * 1000L; // 30 days

    /**
     * Saves the user's session (token, user ID, and timestamp) to storage.
     * @param token The auth token.
     * @param userId The ID of the logged-in user.
     */
    public static void saveSession(String token, int userId) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            
            dos.writeUTF(token);
            dos.writeInt(userId); // Save the user's ID
            dos.writeLong(System.currentTimeMillis());
            
            byte[] data = baos.toByteArray();

            if (rs.getNumRecords() > 0) {
                rs.setRecord(1, data, 0, data.length);
            } else {
                rs.addRecord(data, 0, data.length);
            }
            authToken = token;
            currentUserId = userId;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try { rs.closeRecordStore(); } catch (Exception e) {}
            }
        }
    }

    /**
     * Loads the session from storage and checks if it has expired.
     * @return true if a valid session was loaded, false otherwise.
     */
    public static boolean loadSession() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
            if (rs.getNumRecords() > 0) {
                byte[] data = rs.getRecord(1);
                if (data != null && data.length > 0) {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
                    String token = dis.readUTF();
                    int userId = dis.readInt(); // Load the user's ID
                    long timestamp = dis.readLong();
                    
                    if (System.currentTimeMillis() - timestamp < EXPIRATION_TIME) {
                        authToken = token;
                        currentUserId = userId; // Set the current user ID
                        return true;
                    } else {
                        logout();
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try { rs.closeRecordStore(); } catch (Exception e) {}
            }
        }
        return false;
    }

    public static void logout() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(RECORD_STORE_NAME, true);
            if (rs.getNumRecords() > 0) {
                rs.deleteRecord(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            authToken = null;
            currentUserId = -1; // Reset the user ID on logout
            if (rs != null) {
                try { rs.closeRecordStore(); } catch (Exception e) {}
            }
        }
    }

    public static boolean isLoggedIn() {
        return currentUserId != -1 && authToken != null;
    }
    
    public static String getAuthToken() { return authToken; }
    
    /**
     * Gets the ID of the currently logged-in user.
     * This is the method that was missing.
     * @return The current user's ID, or -1 if not logged in.
     */
    public static int getCurrentUserId() { return currentUserId; }
}
