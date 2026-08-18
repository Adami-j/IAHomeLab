package fr.lab.iahomelab.exemple;

import fr.lab.iahomelab.common.api.ApiPaths;
import fr.lab.iahomelab.common.api.ApiV1Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiV1Controller
@RequestMapping("/exemple")
public class ApiControllerExample {

    @GetMapping("test")
    public ResponseEntity<Integer> test() {
        return ResponseEntity.status(300).build();
    }

}
