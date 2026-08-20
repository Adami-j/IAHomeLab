package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.setup.repository.ComponentInstanceRepository;
import fr.lab.iahomelab.setup.repository.SetupVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComponentInstanceService {

    private final ComponentInstanceRepository componentInstanceRepository;
    private final SetupVersionRepository setupVersionRepository;
}
