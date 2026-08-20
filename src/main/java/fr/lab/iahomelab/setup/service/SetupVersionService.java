package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupVersionRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupVersionResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupVersionRequest;
import fr.lab.iahomelab.setup.entity.Setup;
import fr.lab.iahomelab.setup.entity.SetupVersion;
import fr.lab.iahomelab.setup.entity.SetupVersionStatus;
import fr.lab.iahomelab.setup.repository.SetupRepository;
import fr.lab.iahomelab.setup.repository.SetupVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SetupVersionService {

    private final SetupVersionRepository setupVersionRepository;
    private final SetupRepository setupRepository;

    @Transactional
    public SetupVersionResponse create(UUID setupId, CreateSetupVersionRequest request) {
        Setup setup = findSetup(setupId);

        int nextVersionNumber = setupVersionRepository
                .findTopBySetupIdOrderByVersionNumberDesc(setupId)
                .map(version -> version.getVersionNumber() + 1)
                .orElse(1);

        SetupVersion setupVersion = new SetupVersion();
        setupVersion.setSetup(setup);
        setupVersion.setVersionNumber(nextVersionNumber);
        setupVersion.setStatus(SetupVersionStatus.DRAFT);
        setupVersion.setDescription(request.description());

        return toResponse(setupVersionRepository.save(setupVersion));
    }

    @Transactional(readOnly = true)
    public SetupVersionResponse getById(UUID id) {
        return toResponse(findSetupVersion(id));
    }

    @Transactional(readOnly = true)
    public List<SetupVersionResponse> list(UUID setupId) {
        findSetup(setupId);

        return setupVersionRepository.findAllBySetupIdOrderByVersionNumberAsc(setupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SetupVersionResponse update(UUID id, UpdateSetupVersionRequest request) {
        SetupVersion setupVersion = findSetupVersion(id);
        ensureDraft(setupVersion);

        setupVersion.setDescription(request.description());

        return toResponse(setupVersionRepository.save(setupVersion));
    }

    @Transactional
    public SetupVersionResponse freeze(UUID id) {
        SetupVersion setupVersion = findSetupVersion(id);
        ensureDraft(setupVersion);

        setupVersion.setStatus(SetupVersionStatus.FROZEN);

        return toResponse(setupVersionRepository.save(setupVersion));
    }

    @Transactional
    public void delete(UUID id) {
        SetupVersion setupVersion = findSetupVersion(id);
        ensureDraft(setupVersion);
        setupVersionRepository.delete(setupVersion);
    }

    private Setup findSetup(UUID id) {
        return setupRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Setup not found: " + id)
                );
    }

    private SetupVersion findSetupVersion(UUID id) {
        return setupVersionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Setup version not found: " + id)
                );
    }

    private void ensureDraft(SetupVersion setupVersion) {
        if (setupVersion.getStatus() != SetupVersionStatus.DRAFT) {
            throw new InvalidRequestException(
                    "A frozen setup version cannot be modified or deleted"
            );
        }
    }

    private SetupVersionResponse toResponse(SetupVersion setupVersion) {
        return new SetupVersionResponse(
                setupVersion.getId(),
                setupVersion.getSetup().getId(),
                setupVersion.getVersionNumber(),
                setupVersion.getStatus(),
                setupVersion.getDescription()
        );
    }
}
