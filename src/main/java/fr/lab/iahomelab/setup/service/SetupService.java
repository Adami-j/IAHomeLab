package fr.lab.iahomelab.setup.service;

import fr.lab.iahomelab.common.exception.InvalidRequestException;
import fr.lab.iahomelab.setup.controller.dto.CreateSetupRequest;
import fr.lab.iahomelab.setup.controller.dto.SetupResponse;
import fr.lab.iahomelab.setup.controller.dto.UpdateSetupRequest;
import fr.lab.iahomelab.setup.entity.Setup;
import fr.lab.iahomelab.setup.repository.SetupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SetupService {

    private final SetupRepository setupRepository;

    public SetupResponse create(CreateSetupRequest setup){

        String setupName = setup.name();
        String setupDescription = setup.description();

        Setup newSetup = new Setup();

        if(setup.name() == null || setup.name().isBlank()){
            throw new InvalidRequestException("A setup must have a name");
        }

        List<Setup> listSetup = setupRepository.findByNameEquals(setupName);

        if(!listSetup.isEmpty()){
            throw new InvalidRequestException("A setup with this name already exists");
        }

        newSetup.setName(setupName);
        newSetup.setDescription(setupDescription);

       Setup setupGenerated =  setupRepository.saveAndFlush(newSetup);

        return toResponse(setupGenerated);


    }
    public SetupResponse update(UpdateSetupRequest setup){

        String setupName = setup.name();
        String setupDescription = setup.description();

        Setup newSetup = new Setup();

        if(setup.name() == null || setup.name().isBlank()){
            throw new InvalidRequestException("A setup must have a name");
        }

        List<Setup> listSetup = setupRepository.findByNameEqualsAndIdEquals(setupName,setup.id());

        if(!listSetup.isEmpty()){
            throw new InvalidRequestException("A setup with this name already exists");
        }

        newSetup.setName(setupName);
        newSetup.setDescription(setupDescription);

        Setup setupUpdated =  setupRepository.saveAndFlush(newSetup);

        return toResponse(setupUpdated);


    }

    public void delete(UUID id){
        if(!setupRepository.existsById(id)){
            throw new InvalidRequestException("A setup with this id does not exist");
        }
        setupRepository.deleteById(id);
    }


    private SetupResponse toResponse(Setup setup) {
        return new SetupResponse(setup.getId(),setup.getName(), setup.getDescription() );
    }

}
