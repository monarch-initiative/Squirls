package org.monarchinitiative.threes.autoconfigure;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(ThreesAutoConfiguration.class)
public @interface EnableThrees {
}

