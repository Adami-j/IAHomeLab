package fr.lab.iahomelab.common.api;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping
public @interface ApiV1Controller {

    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] value() default {};
}