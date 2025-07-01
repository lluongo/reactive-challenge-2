package cl.tenpo.learning.reactive.utils.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("health")
@Document("health")
public record HealthEntity(@Id Long id, @Column Boolean up) {
}
