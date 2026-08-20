package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupRequest;
import fr.lab.iahomelab.setup.entity.Setup;
import fr.lab.iahomelab.setup.entity.SetupVersion;
import fr.lab.iahomelab.setup.entity.SetupVersionStatus;
import fr.lab.iahomelab.setup.repository.SetupRepository;
import fr.lab.iahomelab.setup.repository.SetupVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class SetupServiceIT {

    @Autowired
    private SetupService setupService;

    @Autowired
    private SetupRepository setupRepository;

    @Autowired
    private SetupVersionRepository setupVersionRepository;

    @BeforeEach
    void setUp() {
        setupVersionRepository.deleteAll();
        setupRepository.deleteAll();
    }

    @Test
    void shouldSaveSetupWithNameAndDescription() {
        SetupResponse response = setupService.create(
                new CreateSetupRequest("Setup Name", "Setup Description")
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Setup Name");
        assertThat(response.description()).isEqualTo("Setup Description");
        assertThat(setupRepository.findById(response.id())).isPresent();
    }

    @Test
    void shouldSaveSetupWithoutDescription() {
        SetupResponse response = setupService.create(
                new CreateSetupRequest("Setup Name", null)
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.description()).isNull();
    }

    @Test
    void shouldNotSaveSetupWithoutName() {
        CreateSetupRequest request = new CreateSetupRequest(null, "Setup Description");

        assertThatThrownBy(() -> setupService.create(request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldAllowTwoSetupsWithSameName() {
        setupService.create(new CreateSetupRequest("Setup Name", null));
        setupService.create(new CreateSetupRequest("Setup Name", null));

        assertThat(setupRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldGetSetupById() {
        SetupResponse created = setupService.create(
                new CreateSetupRequest("Setup Name", "Description")
        );

        SetupResponse found = setupService.getById(created.id());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void shouldFailWhenGettingUnknownSetup() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> setupService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldListSetups() {
        setupService.create(new CreateSetupRequest("Setup A", null));
        setupService.create(new CreateSetupRequest("Setup B", null));

        List<SetupResponse> setups = setupService.list();

        assertThat(setups)
                .extracting(SetupResponse::name)
                .containsExactlyInAnyOrder("Setup A", "Setup B");
    }

    @Test
    void shouldUpdateExistingSetupWithoutChangingItsId() {
        SetupResponse created = setupService.create(
                new CreateSetupRequest("Setup Name V1", null)
        );

        SetupResponse updated = setupService.update(
                created.id(),
                new UpdateSetupRequest("Setup Name V2", "description")
        );

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("Setup Name V2");
        assertThat(updated.description()).isEqualTo("description");
        assertThat(setupRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldFailWhenUpdatingUnknownSetup() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> setupService.update(
                unknownId,
                new UpdateSetupRequest("Setup Name", null)
        )).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDeleteSetup() {
        SetupResponse created = setupService.create(
                new CreateSetupRequest("Setup Name", null)
        );

        setupService.delete(created.id());

        assertThat(setupRepository.existsById(created.id())).isFalse();
    }

    @Test
    void shouldDeleteSetupWithDraftVersion() {
        SetupResponse created = setupService.create(
                new CreateSetupRequest("Setup Name", null)
        );
        Setup setup = setupRepository.findById(created.id()).orElseThrow();

        SetupVersion version = new SetupVersion();
        version.setSetup(setup);
        version.setVersionNumber(1);
        version.setStatus(SetupVersionStatus.DRAFT);
        SetupVersion savedVersion = setupVersionRepository.save(version);

        setupService.delete(created.id());

        assertThat(setupRepository.existsById(created.id())).isFalse();
        assertThat(setupVersionRepository.existsById(savedVersion.getId())).isFalse();
    }

    @Test
    void shouldNotDeleteSetupWithFrozenVersion() {
        SetupResponse created = setupService.create(
                new CreateSetupRequest("Setup Name", null)
        );
        Setup setup = setupRepository.findById(created.id()).orElseThrow();

        SetupVersion version = new SetupVersion();
        version.setSetup(setup);
        version.setVersionNumber(1);
        version.setStatus(SetupVersionStatus.FROZEN);
        setupVersionRepository.save(version);

        assertThatThrownBy(() -> setupService.delete(created.id()))
                .isInstanceOf(InvalidRequestException.class);

        assertThat(setupRepository.existsById(created.id())).isTrue();
    }

    @Test
    void shouldFailWhenDeletingUnknownSetup() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> setupService.delete(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
