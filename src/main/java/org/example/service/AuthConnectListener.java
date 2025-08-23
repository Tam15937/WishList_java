package org.example.service;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.util.Arrays;

public class AuthConnectListener implements ConnectListener {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    public AuthConnectListener(FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void onConnect(SocketIOClient client) {
        HandshakeData handshakeData = client.getHandshakeData();
        String cookieHeader = handshakeData.getHttpHeaders().get("cookie");
        if (cookieHeader == null) {
            client.disconnect();
            return;
        }
        String sessionId = extractJSessionId(cookieHeader);
        if (sessionId == null) {
            client.disconnect();
            return;
        }
        Session session = sessionRepository.findById(sessionId);
        if (session == null) {
            client.disconnect();
            return;
        }
        Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext == null) {
            client.disconnect();
        }
        // Здесь можно сохранить авторизацию в атрибут клиента, например:
        // client.set("securityContext", securityContext);
    }

    private String extractJSessionId(String cookieHeader) {
        return Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(c -> c.startsWith("JSESSIONID="))
                .map(c -> c.substring("JSESSIONID=".length()))
                .findFirst()
                .orElse(null);
    }
}
