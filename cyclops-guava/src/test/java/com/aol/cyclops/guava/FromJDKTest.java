package com.aol.cyclops.guava;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import cyclops.conversion.guava.FromJDK;
import org.junit.Test;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class FromJDKTest {

    @Test
    public void testJDKf() {
        assertThat(FromJDK.f1((Integer a) -> a * 100)
                          .apply(2),
                   is(200));

    }

    @Test
    public void testJDKOption() {
        assertThat(FromJDK.optional(Optional.of(1))
                          .get(),
                   is(1));
    }

    @Test
    public void testJDKOptionNull() {
        assertThat(FromJDK.optional(Optional.ofNullable(null))
                          .or(100),
                   is(100));
    }

}
