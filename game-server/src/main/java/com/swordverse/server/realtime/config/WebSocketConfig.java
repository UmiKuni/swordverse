package com.swordverse.server.realtime.config;

import com.swordverse.server.realtime.security.WebSocketAuthenticationInterceptor;
import com.swordverse.server.realtime.transport.TrackingWebSocketHandlerDecoratorFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Configures SwordVerse STOMP-over-WebSocket transport, routing, authentication, and physical
 * transport tracking. The HTTP handshake remains public while STOMP CONNECT performs JWT
 * authentication on the inbound channel.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthenticationInterceptor authenticationInterceptor;
    private final TrackingWebSocketHandlerDecoratorFactory trackingDecoratorFactory;

    /**
     * Creates the realtime configuration.
     *
     * @param authenticationInterceptor authenticates STOMP CONNECT frames
     * @param trackingDecoratorFactory tracks physical WebSocket sessions
     */
    public WebSocketConfig(
            WebSocketAuthenticationInterceptor authenticationInterceptor,
            TrackingWebSocketHandlerDecoratorFactory trackingDecoratorFactory) {

        this.authenticationInterceptor = authenticationInterceptor;

        this.trackingDecoratorFactory = trackingDecoratorFactory;
    }

    /** Registers the browser-accessible SwordVerse WebSocket handshake endpoint. */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:8080", "http://localhost:5173");
    }

    /** Configures application command and broker topic destination prefixes. */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.setApplicationDestinationPrefixes("/app");

        registry.enableSimpleBroker(("/topic"));
    }

    /** Installs JWT authentication before inbound STOMP messages reach application handlers. */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(this.authenticationInterceptor);
    }

    /** Installs the decorator that indexes physical WebSocket sessions by connection ID. */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {

        registration.addDecoratorFactory(trackingDecoratorFactory);
    }
}
