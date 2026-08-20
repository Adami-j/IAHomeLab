package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.setup.controller.dto.ComponentInstanceResponse;
import fr.lab.iahomelab.setup.controller.dto.CreateComponentInstanceRequest;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupVersionRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.SetupVersionResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateComponentInstanceRequest;
import fr.lab.iahomelab.setup.entity.ComponentType;
import fr.lab.iahomelab.setup.repository.ComponentInstanceRepository;
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
class ComponentInstanceServiceIT {

    @Autowired
    private ComponentInstanceService componentInstanceService;

    @Autowired
    private SetupVersionService setupVersionService;

    @Autowired
    private SetupService setupService;

    @Autowired
    private ComponentInstanceRepository componentInstanceRepository;

    @Autowired
    private SetupVersionRepository setupVersionRepository;

    @Autowired
    private SetupRepository setupRepository;

    @BeforeEach
    void setUp() {
        componentInstanceRepository.deleteAll();
        setupVersionRepository.deleteAll();
        setupRepository.deleteAll();
    }

    @Test
    void shouldCreateComponentInDraftVersion() {
        SetupVersionResponse version = createVersion();

        ComponentInstanceResponse component = componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest(
                        "llm",
                        ComponentType.LLM,
                        Map.of("model", "gpt-test", "temperature", 0.2)
                )
        );

        assertThat(component.id()).isNotNull();
        assertThat(component.setupVersionId()).isEqualTo(version.id());
        assertThat(component.name()).isEqualTo("llm");
        assertThat(component.type()).isEqualTo(ComponentType.LLM);
        assertThat(component.configuration())
                .containsEntry("model", "gpt-test")
                .containsEntry("temperature", 0.2);
        assertThat(componentInstanceRepository.findById(component.id())).isPresent();
    }

    @Test
    void shouldUseEmptyConfigurationWhenConfigurationIsNull() {
        SetupVersionResponse version = createVersion();

        ComponentInstanceResponse component = componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("prompt", ComponentType.PROMPT, null)
        );

        assertThat(component.configuration()).isEmpty();
    }

    @Test
    void shouldRejectDuplicateNameInsideSameVersion() {
        SetupVersionResponse version = createVersion();
        componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("llm", ComponentType.LLM, null)
        );

        assertThatThrownBy(() -> componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("llm", ComponentType.OTHER, null)
        )).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldAllowSameNameInDifferentVersions() {
        SetupResponse setup = setupService.create(new CreateSetupRequest("Setup", null));
        SetupVersionResponse first = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("v1")
        );
        SetupVersionResponse second = setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest("v2")
        );

        ComponentInstanceResponse firstComponent = componentInstanceService.create(
                first.id(),
                new CreateComponentInstanceRequest("llm", ComponentType.LLM, null)
        );
        ComponentInstanceResponse secondComponent = componentInstanceService.create(
                second.id(),
                new CreateComponentInstanceRequest("llm", ComponentType.LLM, null)
        );

        assertThat(firstComponent.setupVersionId()).isNotEqualTo(secondComponent.setupVersionId());
    }

    @Test
    void shouldGetComponentById() {
        ComponentInstanceResponse created = createComponent("llm", ComponentType.LLM);

        ComponentInstanceResponse found = componentInstanceService.getById(created.id());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void shouldListComponentsOrderedByName() {
        SetupVersionResponse version = createVersion();
        componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("vector-store", ComponentType.VECTOR_STORE, null)
        );
        componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("llm", ComponentType.LLM, null)
        );
        componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("retriever", ComponentType.RETRIEVER, null)
        );

        List<ComponentInstanceResponse> components = componentInstanceService.list(version.id());

        assertThat(components)
                .extracting(ComponentInstanceResponse::name)
                .containsExactly("llm", "retriever", "vector-store");
    }

    @Test
    void shouldUpdateDraftComponentAndReplaceConfiguration() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse created = componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest(
                        "llm",
                        ComponentType.LLM,
                        Map.of("model", "old", "temperature", 0.7)
                )
        );

        ComponentInstanceResponse updated = componentInstanceService.update(
                created.id(),
                new UpdateComponentInstanceRequest(
                        "main-llm",
                        ComponentType.OTHER,
                        Map.of("model", "new")
                )
        );

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.setupVersionId()).isEqualTo(version.id());
        assertThat(updated.name()).isEqualTo("main-llm");
        assertThat(updated.type()).isEqualTo(ComponentType.OTHER);
        assertThat(updated.configuration()).containsExactlyEntriesOf(Map.of("model", "new"));
        assertThat(updated.configuration()).doesNotContainKey("temperature");
    }

    @Test
    void shouldRejectDuplicateNameWhenUpdating() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse first = componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("first", ComponentType.LLM, null)
        );
        componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("second", ComponentType.PROMPT, null)
        );

        assertThatThrownBy(() -> componentInstanceService.update(
                first.id(),
                new UpdateComponentInstanceRequest("second", ComponentType.LLM, null)
        )).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldDeleteComponentFromDraftVersion() {
        ComponentInstanceResponse component = createComponent("llm", ComponentType.LLM);

        componentInstanceService.delete(component.id());

        assertThat(componentInstanceRepository.findById(component.id())).isEmpty();
    }

    @Test
    void shouldRejectCreateUpdateAndDeleteOnFrozenVersion() {
        SetupVersionResponse version = createVersion();
        ComponentInstanceResponse component = componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("llm", ComponentType.LLM, null)
        );
        setupVersionService.freeze(version.id());

        assertThatThrownBy(() -> componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("prompt", ComponentType.PROMPT, null)
        )).isInstanceOf(InvalidRequestException.class);

        assertThatThrownBy(() -> componentInstanceService.update(
                component.id(),
                new UpdateComponentInstanceRequest("changed", ComponentType.OTHER, null)
        )).isInstanceOf(InvalidRequestException.class);

        assertThatThrownBy(() -> componentInstanceService.delete(component.id()))
                .isInstanceOf(InvalidRequestException.class);

        assertThat(componentInstanceRepository.findById(component.id())).isPresent();
    }

    @Test
    void shouldRejectInvalidComponentData() {
        SetupVersionResponse version = createVersion();

        assertThatThrownBy(() -> componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest(" ", ComponentType.LLM, null)
        )).isInstanceOf(InvalidRequestException.class);

        assertThatThrownBy(() -> componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest("component", null, null)
        )).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void shouldRejectUnknownSetupVersion() {
        UUID unknownVersionId = UUID.randomUUID();

        assertThatThrownBy(() -> componentInstanceService.create(
                unknownVersionId,
                new CreateComponentInstanceRequest("llm", ComponentType.LLM, null)
        )).isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> componentInstanceService.list(unknownVersionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectUnknownComponent() {
        UUID unknownComponentId = UUID.randomUUID();

        assertThatThrownBy(() -> componentInstanceService.getById(unknownComponentId))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> componentInstanceService.update(
                unknownComponentId,
                new UpdateComponentInstanceRequest("component", ComponentType.OTHER, null)
        )).isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> componentInstanceService.delete(unknownComponentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private ComponentInstanceResponse createComponent(String name, ComponentType type) {
        SetupVersionResponse version = createVersion();
        return componentInstanceService.create(
                version.id(),
                new CreateComponentInstanceRequest(name, type, null)
        );
    }

    private SetupVersionResponse createVersion() {
        SetupResponse setup = setupService.create(new CreateSetupRequest("Setup", null));
        return setupVersionService.create(
                setup.id(),
                new CreateSetupVersionRequest(null)
        );
    }
}
