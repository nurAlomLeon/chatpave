/*
 * Located in: src/chatpave/net/ApiClient.java
 */
package chatpave.net;

import chatpave.auth.AuthManager;
import chatpave.models.Conversation;
import chatpave.models.ChatMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Handles all HTTP requests to the Django REST API.
 * This is the FINAL corrected version.
 */
public class ApiClient {

    private static final String API_BASE_URL = "http://meta.mcqsolver.com/api/v1/";
    public static String apiError = null;
    public static StringBuffer debugLog = new StringBuffer();

    // --- login and register methods ---
    public static boolean register(String username, String email, String password) {
        // ... code from previous answer ...
        apiError = null;
        HttpConnection conn = null;
        try {
            String payload = "{\"username\":\"" + username + "\", \"email\":\"" + email + "\", \"password\":\"" + password + "\"}";
            conn = (HttpConnection) Connector.open(API_BASE_URL + "auth/register/");
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(payload.length()));

            OutputStream os = conn.openOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();

            if (conn.getResponseCode() == HttpConnection.HTTP_CREATED) {
                InputStream is = conn.openInputStream();
                StringBuffer responseBody = new StringBuffer();
                int ch;
                while ((ch = is.read()) != -1) {
                    responseBody.append((char) ch);
                }
                is.close();
                
                String jsonResponse = responseBody.toString();
                String token = parseToken(jsonResponse);
                String userIdStr = extractValue(jsonResponse, "\"user_id\":");

                if (token != null && userIdStr.length() > 0) {
                    int userId = Integer.parseInt(userIdStr);
                    AuthManager.saveSession(token, userId);
                    return true;
                }
            }
        } catch (Exception e) {
            apiError = "Reg Err: " + e.toString();
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (IOException e) {}
        }
        return false;
    }

    public static boolean login(String username, String password) {
        // ... code from previous answer ...
        apiError = null;
        HttpConnection conn = null;
        try {
            String payload = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";
            conn = (HttpConnection) Connector.open(API_BASE_URL + "auth/login/");
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(payload.length()));
            
            OutputStream os = conn.openOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpConnection.HTTP_OK) {
                InputStream is = conn.openInputStream();
                StringBuffer responseBody = new StringBuffer();
                int ch;
                while ((ch = is.read()) != -1) {
                    responseBody.append((char) ch);
                }
                is.close();
                
                String jsonResponse = responseBody.toString();
                String token = parseToken(jsonResponse);
                String userIdStr = extractValue(jsonResponse, "\"user_id\":");

                if (token != null && userIdStr.length() > 0) {
                    int userId = Integer.parseInt(userIdStr);
                    AuthManager.saveSession(token, userId);
                    return true;
                } else {
                    apiError = "Parse Error: Invalid server response.";
                    return false;
                }
            } else {
                apiError = "Server Error: " + responseCode;
                return false;
            }
        } catch (Exception e) {
            apiError = "Exception: " + e.toString();
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (IOException e) {}
        }
    }
    
    public static Vector getConversations() {
        // ... code from previous answer ...
        apiError = null;
        HttpConnection conn = null;
        InputStream is = null;
        
        debugLog = new StringBuffer();
        debugLog.append("Fetching conversations...\n");
        
        try {
            conn = (HttpConnection) Connector.open(API_BASE_URL + "messages/");
            conn.setRequestMethod(HttpConnection.GET);
            conn.setRequestProperty("Authorization", "Token " + AuthManager.getAuthToken());

            int responseCode = conn.getResponseCode();
            debugLog.append("Response code: " + responseCode + "\n");

            if (responseCode == HttpConnection.HTTP_OK) {
                is = conn.openInputStream();
                StringBuffer responseBody = new StringBuffer();
                int ch;
                while ((ch = is.read()) != -1) {
                    responseBody.append((char) ch);
                }
                is.close();
                
                debugLog.append("Received JSON:\n" + responseBody.toString() + "\n\n");
                
                return parseConversationList(responseBody.toString());
            } else {
                apiError = "GetConvo Err: " + responseCode;
            }
        } catch (Exception e) {
            apiError = "GetConvo Ex: " + e.toString();
            debugLog.append("EXCEPTION: " + e.toString() + "\n");
            e.printStackTrace();
        } finally {
            try { if (is != null) is.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return new Vector();
    }
    
    public static Hashtable getChatHistory(int partnerId, int page) {
        // ... code from previous answer ...
        apiError = null;
        HttpConnection conn = null;
        try {
            String url = API_BASE_URL + "messages/" + partnerId + "/?page=" + page;
            conn = (HttpConnection) Connector.open(url);
            conn.setRequestMethod(HttpConnection.GET);
            conn.setRequestProperty("Authorization", "Token " + AuthManager.getAuthToken());

            if (conn.getResponseCode() == HttpConnection.HTTP_OK) {
                InputStream is = conn.openInputStream();
                StringBuffer responseBody = new StringBuffer();
                int ch;
                while ((ch = is.read()) != -1) {
                    responseBody.append((char) ch);
                }
                is.close();
                
                Hashtable result = new Hashtable();
                String json = responseBody.toString();
                
                String onlineKey = "\"is_online\":";
                int onlineIndex = json.indexOf(onlineKey);
                if (onlineIndex != -1) {
                    int start = onlineIndex + onlineKey.length();
                    int end = json.indexOf(',', start);
                    if (end == -1) end = json.indexOf('}', start);
                    String onlineStr = json.substring(start, end).trim();
                    result.put("is_online", new Boolean(onlineStr.equals("true")));
                }
                
                String messagesKey = "\"messages\":";
                int messagesIndex = json.indexOf(messagesKey);
                if (messagesIndex != -1) {
                    int arrayStart = json.indexOf('[', messagesIndex);
                    int arrayEnd = json.lastIndexOf(']');
                    if (arrayStart != -1 && arrayEnd != -1) {
                        String messagesJson = json.substring(arrayStart, arrayEnd + 1);
                        result.put("messages", parseChatHistory(messagesJson));
                    }
                }
                
                return result;
            } else {
                 apiError = "GetChat Err: " + conn.getResponseCode();
            }
        } catch (Exception e) {
            apiError = "GetChat Ex: " + e.toString();
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return null;
    }

    public static boolean sendMessage(int recipientId, String content) {
        // ... code from previous answer ...
        apiError = null;
        HttpConnection conn = null;
        try {
            content = content.replace('"', ' ');
            String payload = "{\"content\":\"" + content + "\"}";
            String url = API_BASE_URL + "messages/" + recipientId + "/"; 
            
            conn = (HttpConnection) Connector.open(url);
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Authorization", "Token " + AuthManager.getAuthToken());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(payload.getBytes("UTF-8").length));

            OutputStream os = conn.openOutputStream();
            os.write(payload.getBytes("UTF-8"));
            os.flush();
            os.close();

            return conn.getResponseCode() == HttpConnection.HTTP_CREATED;
        } catch (Exception e) {
            apiError = "SendMsg Ex: " + e.toString();
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (IOException e) {}
        }
    }

    // --- JSON Parsing and Helper Methods ---
    private static Vector parseChatHistory(String json) {
        Vector messages = new Vector();
        if (json == null || json.length() < 2 || json.equals("[]")) return messages;

        String[] objects = split(json.substring(1, json.length() - 1), "},{");
        int myId = AuthManager.getCurrentUserId();

        for (int i = 0; i < objects.length; i++) {
            String objectJson = objects[i];
            
            if (!objectJson.startsWith("{")) {
                objectJson = "{" + objectJson;
            }
            if (!objectJson.endsWith("}")) {
                objectJson = objectJson + "}";
            }

            try {
                String content = extractValue(objectJson, "\"content\":\"");
                String timestamp = extractValue(objectJson, "\"timestamp\":\"");
                String senderObject = extractValue(objectJson, "\"sender\":");
                int senderId = Integer.parseInt(extractValue(senderObject, "\"id\":"));
                
                boolean isSentByMe = (senderId == myId);
                messages.addElement(new ChatMessage(content, isSentByMe, timestamp));
            } catch (Exception e) {
                 System.out.println("Parse Chat Ex: " + e.getMessage());
            }
        }
        return messages;
    }
    private static Vector parseConversationList(String json) {
        Vector conversations = new Vector();
        if (json == null || json.length() < 2 || json.equals("[]")) return conversations;
        
        String[] objects = split(json.substring(1, json.length() - 1), "},{");
        debugLog.append("Splitting JSON into " + objects.length + " objects.\n");

        for (int i = 0; i < objects.length; i++) {
            String objectJson = objects[i];

            if (!objectJson.startsWith("{")) {
                objectJson = "{" + objectJson;
            }
            if (!objectJson.endsWith("}")) {
                objectJson = objectJson + "}";
            }

            try {
                String partnerObject = extractValue(objectJson, "\"partner\":");
                int partnerId = Integer.parseInt(extractValue(partnerObject, "\"id\":"));
                String partnerUsername = extractValue(partnerObject, "\"username\":\"");
                
                String lastMessageObject = extractValue(objectJson, "\"last_message\":");
                String lastMessage = extractValue(lastMessageObject, "\"content\":\"");
                String timestamp = extractValue(lastMessageObject, "\"timestamp\":\"");
                
                int unreadCount = Integer.parseInt(extractValue(objectJson, "\"unread_count\":"));

                conversations.addElement(new Conversation(partnerId, partnerUsername, lastMessage, timestamp, unreadCount));
            } catch (Exception e) {
                debugLog.append("Parse FAIL on obj " + i + ": " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }
        
        debugLog.append("Successfully parsed " + conversations.size() + " conversations.\n");
        return conversations;
    }
    
    private static String extractValue(String source, String key) {
        try {
            int keyIndex = source.indexOf(key);
            if (keyIndex == -1) return "";
            
            int valueStart = keyIndex + key.length();
            
            while (valueStart < source.length()) {
                char c = source.charAt(valueStart);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    valueStart++;
                } else {
                    break;
                }
            }

            if (valueStart >= source.length()) return "";

            char firstChar = source.charAt(valueStart);
            if (firstChar == '"') {
                // **FIX**: Find the closing quote and extract the text *between* the quotes.
                int valueEnd = source.indexOf('"', valueStart + 1);
                if (valueEnd == -1) return "";
                return source.substring(valueStart + 1, valueEnd);
            } else if (firstChar == '{') {
                int braceCount = 1;
                int valueEnd = valueStart + 1;
                while (valueEnd < source.length() && braceCount > 0) {
                    char c = source.charAt(valueEnd);
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                    valueEnd++;
                }
                return source.substring(valueStart, valueEnd);
            } else {
                int valueEnd = source.indexOf(',', valueStart);
                if (valueEnd == -1) {
                    valueEnd = source.indexOf('}', valueStart);
                }
                if (valueEnd == -1) {
                   valueEnd = source.length();
                }
                return source.substring(valueStart, valueEnd).trim();
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    private static String[] split(String original, String separator) {
        Vector nodes = new Vector();
        String str = original;
        int index = str.indexOf(separator);
        while(index >= 0) {
            nodes.addElement(str.substring(0, index));
            str = str.substring(index + separator.length());
            index = str.indexOf(separator);
        }
        nodes.addElement(str);
        String[] result = new String[nodes.size()];
        nodes.copyInto(result);
        return result;
    }
    
    private static String parseToken(String json) {
        if (json == null) return null;
        String tokenKey = "\"token\":\"";
        int startIndex = json.indexOf(tokenKey);
        if (startIndex == -1) return null;
        startIndex += tokenKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;
        return json.substring(startIndex, endIndex);
    }
}