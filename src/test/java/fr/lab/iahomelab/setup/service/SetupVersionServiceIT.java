package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupVersionRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.SetupVersionResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupVersionRequest;
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
class SetupVersionServiceIT {

    @Autowired
    private SetupVersionService setupVersionService;

    @Autowired
    private SetupService setupService;

    @Autowired
    private SetupVersionRepository setupVersionRepository;

    @Autowired
    private SetupRepository setupRepository;

    @BeforeEach
    void setUp() {
        setupVersionRepository.deleteAll();
        setupRepository.deleteAll();
    }

    @Test
    void shouldCreateFirstVersionAsDraftNumberOne() {
        SetupResponse setup = createSetup();

        SetupVersionResponse version = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("First version")
        );

        assertThat(version.id()).isNotNull();
        assertThat(version.setupId()).isEqualTo(setup.id());
        assertThat(version.versionNumber()).isEqualTo(1);
        assertThat(version.status()).isEqualTo(SetupVersionStatus.DRAFT);
        assertThat(version.description()).isEqualTo("First version");
        assertThat(setupVersionRepository.findById(version.id())).isPresent();
    }

    @Test
    void shouldIncrementVersionNumberForSameSetup() {
        SetupResponse setup = createSetup();

        SetupVersionResponse first = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("v1")
        );
        SetupVersionResponse second = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("v2")
        );

        assertThat(first.versionNumber()).isEqualTo(1);
        assertThat(second.versionNumber()).isEqualTo(2);
    }

    @Test
    void shouldStartNumberingAtOneForEachSetup() {
        SetupResponse firstSetup = createSetup("Setup A");
        SetupResponse secondSetup = createSetup("Setup B");

        SetupVersionResponse firstVersion = setupVersionService.create(
                firstSetup.id(),
                new CreateSetupVersionRequest(null)
        );
        SetupVersionResponse secondVersion = setupVersionService.create(
                secondSetup.id(),
                new CreateSetupVersionRequest(null)
        );

        assertThat(firstVersion.versionNumber()).isEqualTo(1);
        assertThat(secondVersion.versionNumber()).isEqualTo(1);
    }

    @Test
    void shouldGetVersionById() {
        SetupResponse setup = createSetup();
        SetupVersionResponse created = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("description")
        );

        SetupVersionResponse found = setupVersionService.getById(created.id());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void shouldListVersionsOrderedByVersionNumber() {
        SetupResponse setup = createSetup();
        setupVersionService.create(setup.id(), new CreateSetupVersionRequest("v1"));
        setupVersionService.create(setup.id(), new CreateSetupVersionRequest("v2"));
        setupVersionService.create(setup.id(), new CreateSetupVersionRequest("v3"));

        List<SetupVersionResponse> versions = setupVersionService.list(setup.id());

        assertThat(versions)
                .extracting(SetupVersionResponse::versionNumber)
                .containsExactly(1, 2, 3);
    }

    @Test
    void shouldUpdateDraftVersionDescription() {
        SetupResponse setup = createSetup();
        SetupVersionResponse created = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("before")
        );

        SetupVersionResponse updated = setupVersionService.update(
                created.id(),
                new UpdateSetupVersionRequest("after")
        );

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.versionNumber()).isEqualTo(created.versionNumber());
        assertThat(updated.status()).isEqualTo(SetupVersionStatus.DRAFT);
        assertThat(updated.description()).isEqualTo("after");
    }

    @Test
    void shouldFreezeDraftVersion() {
        SetupResponse setup = createSetup();
        SetupVersionResponse created = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest(null)
        );

        SetupVersionResponse frozen = setupVersionService.freeze(created.id());

        assertThat(frozen.status()).isEqualTo(SetupVersionStatus.FROZEN);
        assertThat(setupVersionRepository.findById(created.id()).orElseThrow().getStatus())
                .isEqualTo(SetupVersionStatus.FROZEN);
    }

    @Test
    void shouldNotUpdateFrozenVersion() {
        SetupVersionResponse frozen = createFrozenVersion();

        assertThatThrownBy(() -> setupVersionService.update(
                frozen.id(),
                new UpdateSetupVersionRequest("changed")
        )).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldNotFreezeAlreadyFrozenVersion() {
        SetupVersionResponse frozen = createFrozenVersion();

        assertThatThrownBy(() -> setupVersionService.freeze(frozen.id()))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldDeleteDraftVersion() {
        SetupResponse setup = createSetup();
        SetupVersionResponse version = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest(null)
        );

        setupVersionService.delete(version.id());

        assertThat(setupVersionRepository.findById(version.id())).isEmpty();
    }

    @Test
    void shouldNotDeleteFrozenVersion() {
        SetupVersionResponse frozen = createFrozenVersion();

        assertThatThrownBy(() -> setupVersionService.delete(frozen.id()))
                .isInstanceOf(InvalidRequestException.class);

        assertThat(setupVersionRepository.findById(frozen.id())).isPresent();
    }

    @Test
    void shouldRejectUnknownSetupWhenCreatingVersion() {
        UUID unknownSetupId = UUID.randomUUID();

        assertThatThrownBy(() -> setupVersionService.create(
                unknownSetupId,
                new CreateSetupVersionRequest(null)
        )).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectUnknownSetupWhenListingVersions() {
        assertThatThrownBy(() -> setupVersionService.list(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectUnknownVersion() {
        UUID unknownVersionId = UUID.randomUUID();

        assertThatThrownBy(() -> setupVersionService.getById(unknownVersionId))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> setupVersionService.update(
                unknownVersionId,
                new UpdateSetupVersionRequest(null)
        )).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> setupVersionService.freeze(unknownVersionId))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> setupVersionService.delete(unknownVersionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private SetupResponse createSetup() {
        return createSetup("Setup");
    }

    private SetupResponse createSetup(String name) {
        return setupService.create(new CreateSetupRequest(name, null));
    }

    private SetupVersionResponse createFrozenVersion() {
        SetupResponse setup = createSetup();
        SetupVersionResponse version = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest(null)
        );
        return setupVersionService.freeze(version.id());
    }
}
