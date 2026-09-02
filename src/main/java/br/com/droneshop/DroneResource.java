package br.com.droneshop;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/drones")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DroneResource {
    private final DroneRepository repository;

    public DroneResource(DroneRepository repository) {
        this.repository = repository;
    }

    @GET
    public List<Drone> listar() {
        return repository.listAll();
    }

    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") UUID id) {
        Drone drone = repository.findById(id);
        return drone == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(drone).build();
    }

    @POST
    @Transactional
    public Response criar(@Valid Drone drone) {
        repository.persist(drone);
        return Response.created(URI.create("/drones/" + drone.id)).entity(drone).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response atualizar(@PathParam("id") UUID id, @Valid Drone dados) {
        Drone drone = repository.findById(id);
        if (drone == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        drone.nome = dados.nome;
        drone.sku = dados.sku;
        drone.preco = dados.preco;
        drone.estoque = dados.estoque;
        drone.status = dados.status;
        drone.autonomiaMin = dados.autonomiaMin;
        drone.alcanceKm = dados.alcanceKm;
        drone.resolucaoVideo = dados.resolucaoVideo;
        drone.pesoGramas = dados.pesoGramas;
        drone.tipo = dados.tipo;
        return Response.ok(drone).build();
    }

    @PATCH
    @Path("/{id}/estoque")
    @Transactional
    public Response atualizarEstoque(@PathParam("id") UUID id, java.util.Map<String, Integer> corpo) {
        Drone drone = repository.findById(id);
        if (drone == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            drone.atualizarEstoque(corpo.getOrDefault("quantidade", 0));
            return Response.ok(drone).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(java.util.Map.of("erro", e.getMessage())).build();
        }
    }

    @POST
    @Path("/{id}/publicar")
    @Transactional
    public Response publicar(@PathParam("id") UUID id) {
        Drone drone = repository.findById(id);
        if (drone == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        drone.publicar();
        return Response.ok(drone).build();
    }

    @GET
    @Path("/{id}/especificacoes")
    public Response especificacoes(@PathParam("id") UUID id) {
        Drone drone = repository.findById(id);
        return drone == null ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.ok(java.util.Map.of("autonomiaMin", drone.autonomiaMin, "alcanceKm", drone.alcanceKm,
                        "resolucaoVideo", drone.resolucaoVideo, "pesoGramas", drone.pesoGramas, "tipo", drone.tipo)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response remover(@PathParam("id") UUID id) {
        return repository.deleteById(id)
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}