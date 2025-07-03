package cl.tenpo.learning.reactive.tasks.task2.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculationResponse {
    private BigDecimal result;
    private BigDecimal num1;
    private BigDecimal num2;
}
