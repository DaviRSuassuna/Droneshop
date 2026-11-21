package br.com.droneshop.estoque.service;

import java.time.LocalDateTime;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Rotinas agendadas do estoque (D33); nos testes o agendador fica desligado. */
@ApplicationScoped
public class RotinasEstoque {

    @Inject
    ReservaService reservas;

    @Scheduled(every = "1m", identity = "expirar-reservas")
    void expirarReservas() {
        reservas.expirarVencidas(LocalDateTime.now());
    }
}
