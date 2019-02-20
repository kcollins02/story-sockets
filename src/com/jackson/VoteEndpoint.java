package com.jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.jackson.objects.User;

@ServerEndpoint(
		value="/vote/{username}/{scrummaster}", 
		decoders = MessageDecoder.class, 
		encoders = MessageEncoder.class)

public class VoteEndpoint {
  
    private Session session;
    private static Set<VoteEndpoint> VoteEndpoints 
      = new CopyOnWriteArraySet<>();
    private static HashMap<String, User> users = new HashMap<>();
    private static String scrumMaster;
    private static String gameState = "end";
 
    @OnOpen
    public void onOpen(
      Session session, 
      @PathParam("username") String username,
      @PathParam("scrummaster") Boolean sm) throws IOException {
  
    	if(sm && scrumMaster != null) {
    		session.close(new CloseReason(CloseCodes.VIOLATED_POLICY, "Scrum Master already appointed"));
    		return;
    	}
    	
        this.session = session;
        VoteEndpoints.add(this);
        users.put(session.getId(), new User(username, sm));
        
        if(sm) {
        	scrumMaster = username;
        }
 
        Message message = new Message();
        message.setFrom(users.get(session.getId()).getUsername());
        message.setContent("connect");
        message.setGameState(gameState);
        message.setUsers(users.values().toArray(new User[users.size()]));
        
        try {
			broadcast(message);
		} catch (EncodeException e) {
			e.printStackTrace();
		}
    }
 
    @OnMessage
    public void onMessage(Session session, Message message) 
      throws IOException {
    	
    	User user = users.get(session.getId());
    	String content = message.getContent();
  
        message.setFrom(user.getUsername());
        
        if(user.getIsScrumMaster()) {
            message.setContent(content);
            
            switch (content) {
	        	case "Start Round":
	        		gameState = "start";
	        		
	        		for (String key : users.keySet())  {
	        			users.get(key).setVote("?");
	        		}
	        		
	        		break;
	        	case "End Round":
	        		gameState = "end";
	        		break;
            }

            message.setGameState(gameState);
    		message.setUsers(users.values().toArray(new User[users.size()]));
            
        } else {
        	user.onContent(content);
            message.setContent("vote");
        }
        
        try {
			broadcast(message);
		} catch (EncodeException e) {
			e.printStackTrace();
		}
    }
 
    @OnClose
    public void onClose(Session session) throws IOException {
  
        VoteEndpoints.remove(this);
        
        if(scrumMaster == users.get(session.getId()).getUsername()) {
        	scrumMaster = null;
        }
        
        Message message = new Message();
        message.setFrom(users.get(session.getId()).getUsername());
        message.setContent("disconnect");
        
        users.remove(session.getId());
        
        try {
			broadcast(message);
		} catch (EncodeException e) {
			e.printStackTrace();
		}
    }
 
    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }
 
    private static void broadcast(Message message) 
      throws IOException, EncodeException {
  
        VoteEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().
                      sendObject(message);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
