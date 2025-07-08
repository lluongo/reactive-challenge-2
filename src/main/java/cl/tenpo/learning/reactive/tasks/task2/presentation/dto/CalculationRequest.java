package cl.tenpo.learning.reactive.tasks.task2.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculationRequest {

    @NotNull(message = "number_1 cannot be null")
    @PositiveOrZero(message = "number_1 must be a non-negative value")
    @JsonProperty("number_1")
    private BigDecimal num1;
    
    @NotNull(message = "number_2 cannot be null")
    @PositiveOrZero(message = "number_2 must be a non-negative value")
    @JsonProperty("number_2")
    private BigDecimal num2;
}
