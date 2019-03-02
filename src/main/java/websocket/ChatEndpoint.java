package websocket;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import controllers.AbstractController;
import dtos.MessageDto;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chat/{userId}")
public class ChatEndpoint extends AbstractController {
    private static final BiMap<Long, ChatEndpoint> usersEndpoints = HashBiMap.create();
    private Session session;

    private static void sendMessage(final Long userId, final MessageDto messageDto) {
        final ChatEndpoint userEndpoint = usersEndpoints.get(userId);
        synchronized (userEndpoint) {
            try {
                userEndpoint.session.getBasicRemote().sendObject(messageDto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnOpen
    public void onOpen(final Session session, @PathParam("userId") final String userId) {
        this.session = session;
        usersEndpoints.put(Long.valueOf(userId), this);
    }

    @OnClose
    public void onClose(final Session session) {
        usersEndpoints.inverse().remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // TODO Do error handling here
    }
}
