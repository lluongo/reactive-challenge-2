package cl.tenpo.learning.reactive.tasks.task2.presentation.dto;

import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "num1 cannot be null")
    private BigDecimal num1;
    
    @NotNull(message = "num2 cannot be null")
    private BigDecimal num2;
}
