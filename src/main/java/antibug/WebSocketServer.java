/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public class WebSocketServer {

    /** The client side implementation. */
    private final Client client = new Client();

    /** The websocket listern in client. */
    private Listener clientListener;

    private Duration timeout = Duration.ofMillis(Long.MAX_VALUE);

    /** errorWhenConnectingToServer */
    private Throwable errorWhenConnectingToServer;

    /** A reply message registered before the connection was established. */
    private List<Runnable> messageQueue = new ArrayList();

    /** A reply message that corresponds to a specific message. */
    private Map<String, List<Runnable>> requestAndResponses = new HashMap();

    /**
     * Specify the errors that occur on the client side when connecting to the server.
     * 
     * @param errorWhenConnectingToServer
     */
    public void rejectConnectionBy(Throwable errorWhenConnectingToServer) {
        this.errorWhenConnectingToServer = errorWhenConnectingToServer;
    }

    /**
     * Emulate request and response.
     * 
     * @param clientRequest
     * @param serverResponse
     */
    public void replyWhen(String clientRequest, Runnable serverResponse) {
        requestAndResponses.computeIfAbsent(clientRequest, k -> new ArrayList()).add(serverResponse);
    }

    /**
     * Emulate sending message to client.
     * 
     * @param messageFromServer
     */
    public void send(String messageFromServer) {
        if (messageFromServer != null && messageFromServer.length() != 0) {
            if (clientListener == null) {
                messageQueue.add(() -> send(messageFromServer));
            } else {
                clientListener.onText(client, messageFromServer, true);
            }
        }
    }

    /**
     * Emulate sending message to client.
     * 
     * @param messageFromServer
     */
    public void sendClose(int status, String reason) {
        if (clientListener == null) {
            messageQueue.add(() -> sendClose(status, reason));
        } else {
            clientListener.onClose(client, status, reason);
        }
    }

    /**
     * Return the associated mocked http client.
     * 
     * @return
     */
    public HttpClient client() {
        return client;
    }

    /**
     * 
     */
    private class Client extends HttpClient implements WebSocket.Builder, WebSocket {

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<CookieHandler> cookieHandler() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Duration> connectTimeout() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Redirect followRedirects() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<ProxySelector> proxy() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SSLContext sslContext() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Authenticator> authenticator() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Version version() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Executor> executor() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler, PushPromiseHandler<T> pushPromiseHandler) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public WebSocket.Builder newWebSocketBuilder() {
            return client;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.net.http.WebSocket.Builder header(String name, String value) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.net.http.WebSocket.Builder connectTimeout(Duration timeout) {
            WebSocketServer.this.timeout = timeout;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public java.net.http.WebSocket.Builder subprotocols(String mostPreferred, String... lesserPreferred) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> buildAsync(URI uri, Listener listener) {
            if (errorWhenConnectingToServer != null) {
                // fail
                return CompletableFuture.failedFuture(errorWhenConnectingToServer);
            } else {
                clientListener = listener;

                // success
                listener.onOpen(this);

                // send response
                for (Runnable message : messageQueue) {
                    message.run();
                }

                return CompletableFuture.completedFuture(this);
            }
        }

        StringBuilder text = new StringBuilder();

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendText(CharSequence data, boolean last) {
            text.append(data);
            if (last) {
                String request = text.toString();
                text.setLength(0);

                List<Runnable> responses = requestAndResponses.remove(request);
                if (responses != null) {
                    for (Runnable response : responses) {
                        response.run();
                    }
                }
            }

            return CompletableFuture.completedFuture(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendBinary(ByteBuffer data, boolean last) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendPing(ByteBuffer message) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendPong(ByteBuffer message) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletableFuture<WebSocket> sendClose(int statusCode, String reason) {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void request(long n) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSubprotocol() {
            throw new Error();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isOutputClosed() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isInputClosed() {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void abort() {
            throw new Error();
        }
    }

    /**
     * Utility to test client state.
     */
    public static class Validator implements WebSocket.Listener {

        /** The associated client implementation. */
        private WebSocket ws;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onOpen(WebSocket webSocket) {
            this.ws = webSocket;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            builder.append(data);
            if (last) {
                messages.add(builder.toString());
                builder.setLength(0);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            this.closed = true;
            this.statusCode = statusCode;
            this.reason = reason;
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            this.error = error;
        }

        private List<String> messages = new ArrayList();

        private StringBuilder builder = new StringBuilder();

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean hasMessage(String message) {
            return messages.contains(message);
        }

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean hasNoMessage() {
            return messages.isEmpty();
        }

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean isOpened() {
            return ws != null;
        }

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean isNotOpened() {
            return ws == null;
        }

        /** The error response. */
        private Throwable error;

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean isError() {
            return error != null;
        }

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean isError(Class<? extends Throwable> errorType) {
            return errorType.isInstance(error);
        }

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean isNotError() {
            return error == null;
        }

        /** The client status. */
        private boolean closed;

        /** The status code. */
        private int statusCode;

        /** The closing reason. */
        private String reason;

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean isClosed() {
            return closed;
        }

        /**
         * Check client status.
         * 
         * @return A result.
         */
        public final boolean isNotClosed() {
            return !closed;
        }

        /**
         * Return the reason of disconnection.
         * 
         * @return
         */
        public final String reason() {
            return reason;
        }

        /**
         * Return the status code of disconnection.
         * 
         * @return
         */
        public final int statusCode() {
            return statusCode;
        }

        public final void send(String text) {
            ws.sendText(text, true);
        }
    }
}