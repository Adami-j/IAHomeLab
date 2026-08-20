package fr.lab.iahomelab.setup.service;


import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupRequest;
import fr.lab.iahomelab.setup.repository.SetupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
public class SetupServiceIT {

    @Autowired
    private SetupService setupService;

    @Autowired
    private SetupRepository SetupRepository;
    @Autowired
    private SetupRepository setupRepository;

    @BeforeEach
    void setUp() {
        SetupRepository.deleteAll();

    }

    @Test
    void shouldSaveSetupWithNameAndDescription() {

        CreateSetupRequest setupRequest = new CreateSetupRequest("Setup Name", "Setup Description");
        SetupResponse setupResponse = setupService.create(setupRequest);

        assert setupResponse != null;
        assert setupResponse.id() != null;
        assert setupRepository.findById(setupResponse.id()).isPresent();
    }

    @Test
    void shouldSaveSetupWithNameWithoutDescription() {

        CreateSetupRequest setupRequest = new CreateSetupRequest("Setup Name", null);
        SetupResponse setupResponse = setupService.create(setupRequest);

        assert setupResponse != null;
        assert setupResponse.id() != null;
        assert setupRepository.findById(setupResponse.id()).isPresent();
    }
    @Test
    void shouldNotSaveSetupWithoutName() {
        CreateSetupRequest setupRequest = new CreateSetupRequest(null, "Setup Description");
        assertThatThrownBy(() -> setupService.create(setupRequest)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldNotSaveCreateAnExistingNameSetUp(){
        CreateSetupRequest setupRequest = new CreateSetupRequest("Setup Name", null);
        setupService.create(setupRequest);

        CreateSetupRequest setupRequestClone = new CreateSetupRequest("Setup Name", null);

        assertThatThrownBy(() -> setupService.create(setupRequestClone)).isInstanceOf(InvalidRequestException.class);

    }

    @Test
    void shouldNotSaveUpdateAnExistingNameSetUp(){
        CreateSetupRequest setupRequest = new CreateSetupRequest("Setup Name", null);
        SetupResponse setupResponse = setupService.create(setupRequest);

        UpdateSetupRequest updateSetupRequest = new UpdateSetupRequest(setupResponse.id(), "Setup Name", "description");

        assertThatThrownBy(() -> setupService.update(updateSetupRequest)).isInstanceOf(InvalidRequestException.class);

    }

    @Test
    void shouldSaveUpdateSetup(){
        CreateSetupRequest setupRequest = new CreateSetupRequest("Setup Name V1", null);
        SetupResponse setupResponse = setupService.create(setupRequest);

        UpdateSetupRequest updateSetupRequest = new UpdateSetupRequest(setupResponse.id(), "Setup Name V2", "description");

        SetupResponse setupResponse1 = setupService.update(updateSetupRequest);

        assert setupResponse1 != null;
        assert setupResponse1.id() != null;
        assert setupRepository.findById(setupResponse1.id()).isPresent();
        assert setupResponse1.name().equals("Setup Name V2");


    }

    @Test
    void shouldDeleteSetup(){
        CreateSetupRequest setupRequest = new CreateSetupRequest("Setup Name", null);
        SetupResponse setupResponseCreate = setupService.create(setupRequest);
        setupService.delete(setupResponseCreate.id());
    }

    @Test
    void shouldNotDeleteSetup(){
        CreateSetupRequest setupRequest = new CreateSetupRequest("Setup Name", null);
        SetupResponse setupResponseCreate = setupService.create(setupRequest);
        setupService.delete(setupResponseCreate.id());
        assertThatThrownBy(() -> setupService.delete(setupResponseCreate.id())).isInstanceOf(InvalidRequestException.class);
    }


    }
