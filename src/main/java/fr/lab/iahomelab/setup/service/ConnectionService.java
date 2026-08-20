package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.setup.controller.dto.ConnectionResponse;
import fr.lab.iahomelab.setup.controller.dto.CreateConnectionRequest;
import fr.lab.iahomelab.setup.controller.dto.UpdateConnectionRequest;
import fr.lab.iahomelab.setup.entity.ComponentInstance;
import fr.lab.iahomelab.setup.entity.Connection;
import fr.lab.iahomelab.setup.entity.SetupVersion;
import fr.lab.iahomelab.setup.entity.SetupVersionStatus;
import fr.lab.iahomelab.setup.repository.ComponentInstanceRepository;
import fr.lab.iahomelab.setup.repository.ConnectionRepository;
import fr.lab.iahomelab.setup.repository.SetupVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final SetupVersionRepository setupVersionRepository;
    private final ComponentInstanceRepository componentInstanceRepository;

    @Transactional
    public ConnectionResponse create(UUID setupVersionId, CreateConnectionRequest request) {
        SetupVersion setupVersion = findSetupVersion(setupVersionId);
        ensureDraft(setupVersion);
        validateComponentIds(request.sourceComponentId(), request.targetComponentId());

        ComponentInstance source = findComponent(request.sourceComponentId());
        ComponentInstance target = findComponent(request.targetComponentId());

        ensureSameSetupVersion(setupVersionId, source, target);
        ensureDifferentComponents(source, target);

        Connection connection = new Connection();
        connection.setSetupVersion(setupVersion);
        connection.setSourceComponent(source);
        connection.setTargetComponent(target);
        connection.setName(request.name());

        return toResponse(connectionRepository.save(connection));
    }

    @Transactional(readOnly = true)
    public ConnectionResponse getById(UUID id) {
        return toResponse(findConnection(id));
    }

    @Transactional(readOnly = true)
    public List<ConnectionResponse> list(UUID setupVersionId) {
        findSetupVersion(setupVersionId);

        return connectionRepository.findAllBySetupVersionId(setupVersionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConnectionResponse update(UUID id, UpdateConnectionRequest request) {
        Connection connection = findConnection(id);
        SetupVersion setupVersion = connection.getSetupVersion();
        ensureDraft(setupVersion);
        validateComponentIds(request.sourceComponentId(), request.targetComponentId());

        ComponentInstance source = findComponent(request.sourceComponentId());
        ComponentInstance target = findComponent(request.targetComponentId());

        ensureSameSetupVersion(setupVersion.getId(), source, target);
        ensureDifferentComponents(source, target);

        connection.setSourceComponent(source);
        connection.setTargetComponent(target);
        connection.setName(request.name());

        return toResponse(connectionRepository.save(connection));
    }

    @Transactional
    public void delete(UUID id) {
        Connection connection = findConnection(id);
        ensureDraft(connection.getSetupVersion());
        connectionRepository.delete(connection);
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

    private Connection findConnection(UUID id) {
        return connectionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Connection not found: " + id)
                );
    }

    private void ensureDraft(SetupVersion setupVersion) {
        if (setupVersion.getStatus() != SetupVersionStatus.DRAFT) {
            throw new InvalidRequestException(
                    "A frozen setup version cannot be modified"
            );
        }
    }

    private void validateComponentIds(UUID sourceComponentId, UUID targetComponentId) {
        if (sourceComponentId == null || targetComponentId == null) {
            throw new InvalidRequestException(
                    "A connection must define source and target components"
            );
        }
    }

    private void ensureSameSetupVersion(
            UUID setupVersionId,
            ComponentInstance source,
            ComponentInstance target
    ) {
        if (!source.getSetupVersion().getId().equals(setupVersionId)
                || !target.getSetupVersion().getId().equals(setupVersionId)) {
            throw new InvalidRequestException(
                    "Connection components must belong to the same setup version"
            );
        }
    }

    private void ensureDifferentComponents(
            ComponentInstance source,
            ComponentInstance target
    ) {
        if (source.getId().equals(target.getId())) {
            throw new InvalidRequestException(
                    "A component cannot be connected to itself"
            );
        }
    }

    private ConnectionResponse toResponse(Connection connection) {
        return new ConnectionResponse(
                connection.getId(),
                connection.getSetupVersion().getId(),
                connection.getSourceComponent().getId(),
                connection.getTargetComponent().getId(),
                connection.getName()
        );
    }
}
