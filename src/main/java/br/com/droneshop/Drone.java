package br.com.droneshop;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Drone extends Produto {
    @NotNull
    @Min(1)
    public Integer autonomiaMin;

    @NotNull
    @DecimalMin("0.0")
    public BigDecimal alcanceKm;

    @NotBlank
    public String resolucaoVideo;

    @NotNull
    @Min(1)
    public Integer pesoGramas;

    @NotNull
    @Enumerated(EnumType.STRING)
    public TipoDrone tipo;
}