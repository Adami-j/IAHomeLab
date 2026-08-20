package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.setup.repository.SetupVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetupVersionService {

    private final SetupVersionRepository setupVersionRepository;
}
