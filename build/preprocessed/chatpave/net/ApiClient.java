/*
 * Located in: src/chatpave/net/ApiClient.java
 */
package chatpave.net;

import chatpave.auth.AuthManager;
import chatpave.models.ChatMessage;
import chatpave.models.Conversation;
import chatpave.models.Post;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * Handles all HTTP requests to the Django REST API.
 * This is the complete version with all necessary methods.
 */
public class ApiClient {

    private static final String API_BASE_URL = "http://meta.mcqsolver.com/api/v1/";
    public static String apiError = null;
    public static StringBuffer debugLog = new StringBuffer();

    // --- Authentication ---

    public static boolean register(String username, String email, String password) {
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
                String jsonResponse = readInputStream(is);
                is.close();
                
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
                String jsonResponse = readInputStream(is);
                is.close();
                
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

    // --- News Feed ---
    
    public static Vector getNewsFeed(int page) {
        apiError = null;
        HttpConnection conn = null;
        try {
            String url = API_BASE_URL + "newsfeed/?page=" + page;
            conn = (HttpConnection) Connector.open(url);
            conn.setRequestMethod(HttpConnection.GET);
            conn.setRequestProperty("Authorization", "Token " + AuthManager.getAuthToken());

            if (conn.getResponseCode() == HttpConnection.HTTP_OK) {
                return parsePostList(readInputStream(conn.openInputStream()));
            } else {
                apiError = "GetFeed Err: " + conn.getResponseCode();
            }
        } catch (Exception e) {
            apiError = "GetFeed Ex: " + e.toString();
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return null;
    }
    public static boolean toggleLike(int postId) {
        apiError = null;
        HttpConnection conn = null;
        try {
            String url = API_BASE_URL + "posts/" + postId + "/like/";
            conn = (HttpConnection) Connector.open(url);
            conn.setRequestMethod(HttpConnection.POST);
            conn.setRequestProperty("Authorization", "Token " + AuthManager.getAuthToken());
            
            int responseCode = conn.getResponseCode();
            // Success can be 200 (unliked) or 201 (liked)
            return responseCode == HttpConnection.HTTP_OK || responseCode == HttpConnection.HTTP_CREATED;
        } catch (Exception e) {
            apiError = "Like Err: " + e.toString();
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }

    // --- Messaging ---
    
    public static Vector getConversations() {
        apiError = null;
        HttpConnection conn = null;
        try {
            conn = (HttpConnection) Connector.open(API_BASE_URL + "messages/");
            conn.setRequestMethod(HttpConnection.GET);
            conn.setRequestProperty("Authorization", "Token " + AuthManager.getAuthToken());

            if (conn.getResponseCode() == HttpConnection.HTTP_OK) {
                return parseConversationList(readInputStream(conn.openInputStream()));
            } else {
                apiError = "GetConvo Err: " + conn.getResponseCode();
            }
        } catch (Exception e) {
            apiError = "GetConvo Ex: " + e.toString();
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return null;
    }

    public static Hashtable getChatHistory(int partnerId, int page) {
        apiError = null;
        HttpConnection conn = null;
        try {
            String url = API_BASE_URL + "messages/" + partnerId + "/?page=" + page;
            conn = (HttpConnection) Connector.open(url);
            conn.setRequestMethod(HttpConnection.GET);
            conn.setRequestProperty("Authorization", "Token " + AuthManager.getAuthToken());

            if (conn.getResponseCode() == HttpConnection.HTTP_OK) {
                String json = readInputStream(conn.openInputStream());
                Hashtable result = new Hashtable();
                
                String onlineKey = "\"is_online\":";
                int onlineIndex = json.indexOf(onlineKey);
                if (onlineIndex != -1) {
                    String onlineStr = extractValue(json, onlineKey);
                    result.put("is_online", new Boolean(onlineStr.equals("true")));
                }
                
                String messagesKey = "\"messages\":";
                int messagesIndex = json.indexOf(messagesKey);
                if (messagesIndex != -1) {
                    String messagesJson = extractValue(json, messagesKey);
                    result.put("messages", parseChatHistory(messagesJson));
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

    /**
     * A robust parser for the news feed JSON response.
     * This version correctly handles the paginated structure from Django REST Framework.
     * @param json The JSON string from the API.
     * @return A Vector of Post objects.
     */
    private static Vector parsePostList(String json) {
        Vector posts = new Vector();
        if (json == null || json.length() < 2) return posts;
        String resultsJson = extractValue(json, "\"results\":");
        if(resultsJson.length() == 0) resultsJson = json;
        
        String[] objects = split(resultsJson.substring(1, resultsJson.length() - 1), "},{");
        for (int i = 0; i < objects.length; i++) {
            String objectJson = objects[i];
            if (!objectJson.startsWith("{")) objectJson = "{" + objectJson;
            if (!objectJson.endsWith("}")) objectJson = objectJson + "}";
            try {
                int postId = Integer.parseInt(extractValue(objectJson, "\"id\":"));
                String content = extractValue(objectJson, "\"content\":\"");
                String timestamp = extractValue(objectJson, "\"created_at\":\"");
                int likeCount = Integer.parseInt(extractValue(objectJson, "\"likes_count\":"));
                int commentCount = Integer.parseInt(extractValue(objectJson, "\"comments_count\":"));
                boolean isLiked = extractValue(objectJson, "\"is_liked\":").equals("true");
                String userObject = extractValue(objectJson, "\"user\":");
                String authorUsername = extractValue(userObject, "\"username\":\"");
                
                String imagesArray = extractValue(objectJson, "\"images\":");
                String imageUrl = null;
                if (imagesArray != null && imagesArray.length() > 2) {
                    String firstImageObject = split(imagesArray.substring(1, imagesArray.length() - 1), "},{")[0];
                    imageUrl = extractValue(firstImageObject, "\"image\":\"");
                }

                posts.addElement(new Post(postId, authorUsername, content, timestamp, likeCount, commentCount, isLiked, imageUrl));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return posts;
    }
    
    private static Vector parseConversationList(String json) {
        Vector conversations = new Vector();
        if (json == null || json.length() < 2) return conversations;
        String[] objects = split(json.substring(1, json.length() - 1), "},{");
        for (int i = 0; i < objects.length; i++) {
            String objectJson = objects[i];
            if (!objectJson.startsWith("{")) objectJson = "{" + objectJson;
            if (!objectJson.endsWith("}")) objectJson = objectJson + "}";
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
                e.printStackTrace();
            }
        }
        return conversations;
    }
    
    private static Vector parseChatHistory(String json) {
        Vector messages = new Vector();
        if (json == null || json.length() < 2) return messages;

        String[] objects = split(json.substring(1, json.length() - 1), "},{");
        int myId = AuthManager.getCurrentUserId();

        for (int i = 0; i < objects.length; i++) {
            String objectJson = objects[i];
            if (!objectJson.startsWith("{")) objectJson = "{" + objectJson;
            if (!objectJson.endsWith("}")) objectJson = objectJson + "}";
            try {
                String content = extractValue(objectJson, "\"content\":\"");
                String timestamp = extractValue(objectJson, "\"timestamp\":\"");
                String senderObject = extractValue(objectJson, "\"sender\":");
                int senderId = Integer.parseInt(extractValue(senderObject, "\"id\":"));
                
                boolean isSentByMe = (senderId == myId);
                messages.addElement(new ChatMessage(content, isSentByMe, timestamp));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return messages;
    }
    
    private static String extractValue(String source, String key) {
        try {
            int keyIndex = source.indexOf(key);
            if (keyIndex == -1) return "";
            int valueStart = keyIndex + key.length();
            while (valueStart < source.length() && source.charAt(valueStart) == ' ') {
                valueStart++;
            }
            if (valueStart >= source.length()) return "";
            char firstChar = source.charAt(valueStart);
            if (firstChar == '"') {
                int valueEnd = source.indexOf('"', valueStart + 1);
                if (valueEnd == -1) return "";
                return source.substring(valueStart + 1, valueEnd);
            } else if (firstChar == '{' || firstChar == '[') {
                char openBrace = firstChar;
                char closeBrace = (openBrace == '{') ? '}' : ']';
                int braceCount = 1;
                int valueEnd = valueStart + 1;
                while (valueEnd < source.length() && braceCount > 0) {
                    if (source.charAt(valueEnd) == openBrace) braceCount++;
                    else if (source.charAt(valueEnd) == closeBrace) braceCount--;
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
    
    
    private static String readInputStream(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        int ch;
        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }
        return sb.toString();
    }
}
