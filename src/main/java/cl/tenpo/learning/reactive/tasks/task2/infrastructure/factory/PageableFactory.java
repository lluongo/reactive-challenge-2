package cl.tenpo.learning.reactive.tasks.task2.infrastructure.factory;

import cl.tenpo.learning.reactive.tasks.task2.infrastructure.config.PaginationConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageableFactory {
    
    private final PaginationConfig paginationConfig;
    
    public Pageable createPageable(Integer page, Integer size) {
        int validatedPage = validateAndGetPage(page);
        int validatedSize = validateAndGetSize(size);
        
        return PageRequest.of(validatedPage, validatedSize);
    }
    
    private int validateAndGetPage(Integer page) {
        if (page == null || page < 0) {
            return paginationConfig.getDefaultPage();
        }
        return page;
    }
    
    private int validateAndGetSize(Integer size) {
        if (size == null || size <= 0) {
            return paginationConfig.getDefaultPageSize();
        }
        if (size > paginationConfig.getMaxPageSize()) {
            return paginationConfig.getMaxPageSize();
        }
        return size;
    }
}
