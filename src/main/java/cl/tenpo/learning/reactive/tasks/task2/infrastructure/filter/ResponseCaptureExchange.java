package cl.tenpo.learning.reactive.tasks.task2.infrastructure.filter;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class ResponseCaptureExchange extends ServerWebExchangeDecorator {

    private final CaptureRequestDecorator requestDecorator;
    private final CaptureResponseDecorator responseDecorator;

    public ResponseCaptureExchange(ServerWebExchange delegate) {
        super(delegate);
        this.requestDecorator = new CaptureRequestDecorator(delegate.getRequest());
        this.responseDecorator = new CaptureResponseDecorator(delegate.getResponse());
    }

    @Override
    public ServerHttpRequest getRequest() {
        return requestDecorator;
    }

    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }

    public String getRequestBody() {
        return requestDecorator.getBodyContent();
    }

    public String getResponseBody() {
        return responseDecorator.getBodyContent();
    }

    public int getStatusCode() {
        return responseDecorator.getRawStatusCode();
    }

    private static class CaptureRequestDecorator extends RequestBodyCaptureDecorator {
        public CaptureRequestDecorator(ServerHttpRequest delegate) {
            super(delegate);
        }
    }

    private static class CaptureResponseDecorator extends ServerHttpResponseDecorator {
        private final AtomicReference<String> bodyRef = new AtomicReference<>("");
        
        public CaptureResponseDecorator(ServerHttpResponse delegate) {
            super(delegate);
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            if (body instanceof Flux) {
                Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                
                return super.writeWith(fluxBody.map(dataBuffer -> {
                    byte[] content = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(content);
                    
                    // Make a copy of the content
                    String bodyContent = new String(content, StandardCharsets.UTF_8);
                    bodyRef.set(bodyContent);
                    
                    // Return a copy of the data buffer
                    return this.bufferFactory().wrap(content);
                }));
            }
            return super.writeWith(body);
        }

        public String getBodyContent() {
            return bodyRef.get();
        }
    }
}
