package cl.tenpo.learning.reactive.tasks.task2.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calculation {
    private BigDecimal number1;
    private BigDecimal number2;
    private BigDecimal percentage;
    private BigDecimal result;
    
    public static Calculation of(BigDecimal number1, BigDecimal number2, BigDecimal percentage) {
        BigDecimal sum = number1.add(number2);
        BigDecimal percentageAmount = sum.multiply(percentage);
        BigDecimal result = sum.add(percentageAmount);
        
        return Calculation.builder()
                .number1(number1)
                .number2(number2)
                .percentage(percentage)
                .result(result)
                .build();
    }
}
