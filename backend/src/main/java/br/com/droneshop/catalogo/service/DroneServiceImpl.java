package br.com.droneshop.catalogo.service;

import java.util.ArrayList;
import java.util.List;

import br.com.droneshop.catalogo.dto.DroneRequestDTO;
import br.com.droneshop.catalogo.model.Drone;
import br.com.droneshop.catalogo.model.StatusProduto;
import br.com.droneshop.exception.Problem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DroneServiceImpl implements DroneService {

    @Inject
    ProdutoCadastro cadastro;

    @Override
    @Transactional
    public Drone create(DroneRequestDTO dto) {
        Drone drone = new Drone();
        aplicarDados(drone, dto, null);
        cadastro.persistir(drone);
        return drone;
    }

    @Override
    @Transactional
    public void update(long id, DroneRequestDTO dto) {
        aplicarDados(cadastro.buscarOuFalhar(id, Drone.class), dto, id);
    }

    private void aplicarDados(Drone destino, DroneRequestDTO dto, Long idAtual) {
        StatusProduto status = cadastro.status(dto.idStatus());
        cadastro.validarObrigatorios(dto, status, null, obrigatoriosDoDrone(dto));
        cadastro.aplicar(destino, dto, status, idAtual);

        destino.setPesoGramas(dto.pesoGramas());
        destino.setDimensoesDobrado(dto.dimensoesDobrado());
        destino.setDimensoesAberto(dto.dimensoesAberto());
        destino.setAutonomiaMinutos(dto.autonomiaMinutos());
        destino.setAlcanceMetros(dto.alcanceMetros());
        destino.setVelocidadeMaximaKmh(dto.velocidadeMaximaKmh());
        destino.setResolucaoFotoMp(dto.resolucaoFotoMp());
        destino.setTipoSensor(dto.tipoSensor());
        destino.setResolucaoVideo(dto.resolucaoVideo());
        destino.setEstabilizacao(dto.estabilizacao());
        destino.setTipoBateria(dto.tipoBateria());
        destino.setCapacidadeBateriaMah(dto.capacidadeBateriaMah());
        destino.setTempoCarregamentoMinutos(dto.tempoCarregamentoMinutos());
        destino.setGpsIntegrado(Boolean.TRUE.equals(dto.gpsIntegrado()));
        destino.setSensoresAnticolisao(Boolean.TRUE.equals(dto.sensoresAnticolisao()));
        destino.setExigeRegistroAnac(Boolean.TRUE.equals(dto.exigeRegistroAnac()));
        destino.setClasseAnac(dto.classeAnac());
        destino.setRegistroSisant(dto.registroSisant());
    }

    // So vale fora do rascunho (validarObrigatorios ignora a lista no RASCUNHO)
    private List<Problem.FieldError> obrigatoriosDoDrone(DroneRequestDTO dto) {
        List<Problem.FieldError> erros = new ArrayList<>();
        if (dto.pesoGramas() == null)
            erros.add(new Problem.FieldError("pesoGramas", ProdutoCadastro.OBRIGATORIO));
        if (Boolean.TRUE.equals(dto.exigeRegistroAnac()) && dto.classeAnac() == null)
            erros.add(new Problem.FieldError("classeAnac", "Informe a classe ANAC do drone que exige registro (back)."));
        return erros;
    }

}
