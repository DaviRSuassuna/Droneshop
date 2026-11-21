package br.com.droneshop.compra.service;

import java.time.LocalDateTime;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Rotinas agendadas da compra (D33); nos testes o agendador fica desligado. */
@ApplicationScoped
public class RotinasCompra {

    @Inject
    CarrinhoService carrinhos;

    @Scheduled(every = "1h", identity = "carrinhos-abandonados")
    void carrinhosAbandonados() {
        carrinhos.marcarAbandonados(LocalDateTime.now());
    }
}
