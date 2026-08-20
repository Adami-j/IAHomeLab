package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.setup.repository.ComponentInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComponentInstanceService {

    private final ComponentInstanceRepository componentInstanceRepository;
}
