package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.setup.repository.SetupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SetupService {

    private final SetupRepository setupRepository;
}
