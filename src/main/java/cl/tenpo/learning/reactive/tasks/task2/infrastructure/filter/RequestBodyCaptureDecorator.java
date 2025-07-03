package cl.tenpo.learning.reactive.tasks.task2.infrastructure.filter;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class RequestBodyCaptureDecorator extends ServerHttpRequestDecorator {
    
    private final AtomicReference<String> bodyRef = new AtomicReference<>("");
    
    public RequestBodyCaptureDecorator(ServerHttpRequest delegate) {
        super(delegate);
    }
    
    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody().doOnNext(dataBuffer -> {
            byte[] content = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(content);
            dataBuffer.readPosition(0);
            
            String bodyContent = new String(content, StandardCharsets.UTF_8);
            bodyRef.set(bodyContent);
        });
    }
    
    public String getBodyContent() {
        return bodyRef.get();
    }
}
