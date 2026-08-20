package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.setup.controller.dto.ComponentInstanceResponse;
import fr.lab.iahomelab.setup.controller.dto.ConnectionResponse;
import fr.lab.iahomelab.setup.controller.dto.CreateComponentInstanceRequest;
import fr.lab.iahomelab.setup.controller.dto.CreateConnectionRequest;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupVersionRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.SetupVersionResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateConnectionRequest;
import fr.lab.iahomelab.setup.entity.ComponentType;
import fr.lab.iahomelab.setup.repository.ComponentInstanceRepository;
import fr.lab.iahomelab.setup.repository.ConnectionRepository;
import fr.lab.iahomelab.setup.repository.SetupRepository;
import fr.lab.iahomelab.setup.repository.SetupVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class ConnectionServiceIT {

    @Autowired
    private ConnectionService connectionService;

    @Autowired
    private ComponentInstanceService componentInstanceService;

    @Autowired
    private SetupVersionService setupVersionService;

    @Autowired
    private SetupService setupService;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private ComponentInstanceRepository componentInstanceRepository;

    @Autowired
    private SetupVersionRepository setupVersionRepository;

    @Autowired
    private SetupRepository setupRepository;

    @BeforeEach
    void setUp() {
        connectionRepository.deleteAll();
        componentInstanceRepository.deleteAll();
        setupVersionRepository.deleteAll();
        setupRepository.deleteAll();
    }

    @Test
    void shouldCreateConnectionBetweenComponentsOfSameDraftVersion() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse source = createComponent(version.id(), "source");
        ComponentInstanceResponse target = createComponent(version.id(), "target");

        ConnectionResponse connection = connectionService.create(
                version.id(),
                new CreateConnectionRequest(source.id(), target.id(), "source-to-target")
        );

        assertThat(connection.id()).isNotNull();
        assertThat(connection.setupVersionId()).isEqualTo(version.id());
        assertThat(connection.sourceComponentId()).isEqualTo(source.id());
        assertThat(connection.targetComponentId()).isEqualTo(target.id());
        assertThat(connection.name()).isEqualTo("source-to-target");
        assertThat(connectionRepository.findById(connection.id())).isPresent();
    }

    @Test
    void shouldGetAndListConnections() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse source = createComponent(version.id(), "source");
        ComponentInstanceResponse target = createComponent(version.id(), "target");
        ConnectionResponse created = connectionService.create(
                version.id(),
                new CreateConnectionRequest(source.id(), target.id(), null)
        );

        ConnectionResponse found = connectionService.getById(created.id());
        List<ConnectionResponse> connections = connectionService.list(version.id());

        assertThat(found).isEqualTo(created);
        assertThat(connections).containsExactly(created);
    }

    @Test
    void shouldUpdateConnectionInsideSameDraftVersion() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse source = createComponent(version.id(), "source");
        ComponentInstanceResponse firstTarget = createComponent(version.id(), "target-1");
        ComponentInstanceResponse secondTarget = createComponent(version.id(), "target-2");
        ConnectionResponse created = connectionService.create(
                version.id(),
                new CreateConnectionRequest(source.id(), firstTarget.id(), "before")
        );

        ConnectionResponse updated = connectionService.update(
                created.id(),
                new UpdateConnectionRequest(source.id(), secondTarget.id(), "after")
        );

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.setupVersionId()).isEqualTo(version.id());
        assertThat(updated.sourceComponentId()).isEqualTo(source.id());
        assertThat(updated.targetComponentId()).isEqualTo(secondTarget.id());
        assertThat(updated.name()).isEqualTo("after");
    }

    @Test
    void shouldDeleteConnectionFromDraftVersion() {
        ConnectionResponse connection = createConnection();

        connectionService.delete(connection.id());

        assertThat(connectionRepository.findById(connection.id())).isEmpty();
    }

    @Test
    void shouldRejectSelfConnection() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse component = createComponent(version.id(), "component");

        assertThatThrownBy(() -> connectionService.create(
                version.id(),
                new CreateConnectionRequest(component.id(), component.id(), null)
        )).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldRejectComponentsFromAnotherSetupVersion() {
        SetupResponse setup = setupService.create(new CreateSetupRequest("Setup", null));
        SetupVersionResponse firstVersion = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("v1")
        );
        SetupVersionResponse secondVersion = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("v2")
        );
        ComponentInstanceResponse source = createComponent(firstVersion.id(), "source");
        ComponentInstanceResponse target = createComponent(secondVersion.id(), "target");

        assertThatThrownBy(() -> connectionService.create(
                firstVersion.id(),
                new CreateConnectionRequest(source.id(), target.id(), null)
        )).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldRejectConnectionMutationWhenVersionIsFrozen() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse source = createComponent(version.id(), "source");
        ComponentInstanceResponse target = createComponent(version.id(), "target");
        ConnectionResponse connection = connectionService.create(
                version.id(),
                new CreateConnectionRequest(source.id(), target.id(), null)
        );
        setupVersionService.freeze(version.id());

        assertThatThrownBy(() -> connectionService.create(
                version.id(),
                new CreateConnectionRequest(source.id(), target.id(), "another")
        )).isInstanceOf(InvalidRequestException.class);

        assertThatThrownBy(() -> connectionService.update(
                connection.id(),
                new UpdateConnectionRequest(source.id(), target.id(), "updated")
        )).isInstanceOf(InvalidRequestException.class);

        assertThatThrownBy(() -> connectionService.delete(connection.id()))
                .isInstanceOf(InvalidRequestException.class);

        assertThat(connectionRepository.findById(connection.id())).isPresent();
    }

    @Test
    void shouldDeleteConnectionsWhenComponentIsDeleted() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse source = createComponent(version.id(), "source");
        ComponentInstanceResponse target = createComponent(version.id(), "target");
        ConnectionResponse connection = connectionService.create(
                version.id(),
                new CreateConnectionRequest(source.id(), target.id(), null)
        );

        componentInstanceService.delete(source.id());

        assertThat(connectionRepository.findById(connection.id())).isEmpty();
    }

    @Test
    void shouldRejectUnknownResources() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse source = createComponent(version.id(), "source");
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> connectionService.create(
                unknownId,
                new CreateConnectionRequest(source.id(), source.id(), null)
        )).isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> connectionService.create(
                version.id(),
                new CreateConnectionRequest(source.id(), unknownId, null)
        )).isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> connectionService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> connectionService.list(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> connectionService.update(
                unknownId,
                new UpdateConnectionRequest(source.id(), source.id(), null)
        )).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> connectionService.delete(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private ConnectionResponse createConnection() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse source = createComponent(version.id(), "source");
        ComponentInstanceResponse target = createComponent(version.id(), "target");
        return connectionService.create(
                version.id(),
                new CreateConnectionRequest(source.id(), target.id(), null)
        );
    }

    private SetupVersionResponse createVersion() {
        SetupResponse setup = setupService.create(new CreateSetupRequest("Setup", null));
        return setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest(null)
        );
    }

    private ComponentInstanceResponse createComponent(UUID setupVersionId, String name) {
        return componentInstanceService.create(
                setupVersionId,
                new CreateComponentInstanceRequest(
                        name,
                        ComponentType.OTHER,
                        Map.of("value", name)
                )
        );
    }
}
