package br.com.droneshop;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class DroneRepository implements PanacheRepositoryBase<Drone, UUID> {
}