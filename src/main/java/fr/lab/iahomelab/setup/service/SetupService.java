package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.common.exception.ResourceNotFoundException;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupRequest;
import fr.lab.iahomelab.setup.entity.Setup;
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
public class SetupService {

    private final SetupRepository setupRepository;
    private final SetupVersionRepository setupVersionRepository;

    @Transactional
    public SetupResponse create(CreateSetupRequest request) {
        validateName(request.name());

        Setup setup = new Setup();
        setup.setName(request.name());
        setup.setDescription(request.description());

        return toResponse(setupRepository.save(setup));
    }

    @Transactional(readOnly = true)
    public SetupResponse getById(UUID id) {
        return toResponse(findSetup(id));
    }

    @Transactional(readOnly = true)
    public List<SetupResponse> list() {
        return setupRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SetupResponse update(UUID id, UpdateSetupRequest request) {
        validateName(request.name());

        Setup setup = findSetup(id);
        setup.setName(request.name());
        setup.setDescription(request.description());

        return toResponse(setupRepository.save(setup));
    }

    @Transactional
    public void delete(UUID id) {
        Setup setup = findSetup(id);

        if (setupVersionRepository.existsBySetupIdAndStatus(id, SetupVersionStatus.FROZEN)) {
            throw new InvalidRequestException(
                    "A setup with a frozen version cannot be deleted"
            );
        }

        setupRepository.delete(setup);
    }

    private Setup findSetup(UUID id) {
        return setupRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Setup not found: " + id)
                );
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("A setup must have a name");
        }
    }

    private SetupResponse toResponse(Setup setup) {
        return new SetupResponse(
                setup.getId(),
                setup.getName(),
                setup.getDescription()
        );
    }
}
