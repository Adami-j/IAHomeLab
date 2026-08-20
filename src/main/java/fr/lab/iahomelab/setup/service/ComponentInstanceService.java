package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.setup.controller.dto.ComponentInstanceResponse;
import fr.lab.iahomelab.setup.controller.dto.CreateComponentInstanceRequest;
import fr.lab.iahomelab.setup.controller.dto.UpdateComponentInstanceRequest;
import fr.lab.iahomelab.setup.entity.ComponentInstance;
import fr.lab.iahomelab.setup.entity.ComponentType;
import fr.lab.iahomelab.setup.entity.SetupVersion;
import fr.lab.iahomelab.setup.entity.SetupVersionStatus;
import fr.lab.iahomelab.setup.repository.ComponentInstanceRepository;
import fr.lab.iahomelab.setup.repository.SetupVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComponentInstanceService {

    private final ComponentInstanceRepository componentInstanceRepository;
    private final SetupVersionRepository setupVersionRepository;

    @Transactional
    public ComponentInstanceResponse create(
            UUID setupVersionId,
            CreateComponentInstanceRequest request
    ) {
        SetupVersion setupVersion = findSetupVersion(setupVersionId);
        ensureDraft(setupVersion);
        validate(request.name(), request.type());
        ensureNameAvailable(setupVersionId, request.name());

        ComponentInstance component = new ComponentInstance();
        component.setSetupVersion(setupVersion);
        component.setName(request.name());
        component.setType(request.type());
        component.setConfiguration(copyConfiguration(request.configuration()));

        return toResponse(componentInstanceRepository.save(component));
    }

    @Transactional(readOnly = true)
    public ComponentInstanceResponse getById(UUID id) {
        return toResponse(findComponent(id));
    }

    @Transactional(readOnly = true)
    public List<ComponentInstanceResponse> list(UUID setupVersionId) {
        findSetupVersion(setupVersionId);

        return componentInstanceRepository
                .findAllBySetupVersionIdOrderByNameAsc(setupVersionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ComponentInstanceResponse update(
            UUID id,
            UpdateComponentInstanceRequest request
    ) {
        ComponentInstance component = findComponent(id);
        ensureDraft(component.getSetupVersion());
        validate(request.name(), request.type());

        UUID setupVersionId = component.getSetupVersion().getId();
        if (componentInstanceRepository.existsBySetupVersionIdAndNameAndIdNot(
                setupVersionId,
                request.name(),
                id
        )) {
            throw new InvalidRequestException(
                    "A component with this name already exists in this setup version"
            );
        }

        component.setName(request.name());
        component.setType(request.type());
        component.setConfiguration(copyConfiguration(request.configuration()));

        return toResponse(componentInstanceRepository.save(component));
    }

    @Transactional
    public void delete(UUID id) {
        ComponentInstance component = findComponent(id);
        ensureDraft(component.getSetupVersion());
        componentInstanceRepository.delete(component);
    }

    private SetupVersion findSetupVersion(UUID id) {
        return setupVersionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Setup version not found: " + id)
                );
    }

    private ComponentInstance findComponent(UUID id) {
        return componentInstanceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Component instance not found: " + id)
                );
    }

    private void ensureDraft(SetupVersion setupVersion) {
        if (setupVersion.getStatus() != SetupVersionStatus.DRAFT) {
            throw new InvalidRequestException(
                    "A frozen setup version cannot be modified"
            );
        }
    }

    private void validate(String name, ComponentType type) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("A component must have a name");
        }
        if (type == null) {
            throw new InvalidRequestException("A component must have a type");
        }
    }

    private void ensureNameAvailable(UUID setupVersionId, String name) {
        if (componentInstanceRepository.existsBySetupVersionIdAndName(setupVersionId, name)) {
            throw new InvalidRequestException(
                    "A component with this name already exists in this setup version"
            );
        }
    }

    private Map<String, Object> copyConfiguration(Map<String, Object> configuration) {
        return configuration != null
                ? new HashMap<>(configuration)
                : new HashMap<>();
    }

    private ComponentInstanceResponse toResponse(ComponentInstance component) {
        return new ComponentInstanceResponse(
                component.getId(),
                component.getSetupVersion().getId(),
                component.getName(),
                component.getType(),
                new HashMap<>(component.getConfiguration())
        );
    }
}
