package net.vamoscantar.utils.http;

import org.junit.jupiter.api.Test;

import static net.vamoscantar.utils.http.UrlUtils.sanitizePath;
import static org.assertj.core.api.Assertions.assertThat;

class UrlUtilsTest {


    @Test
    void sanitizePathResolvesPathTraversal() {
        assertThat(sanitizePath("")).isEmpty();

        assertThat(sanitizePath("/test/../")).isEqualTo("/");

        assertThat(sanitizePath("/test/../test2")).isEqualTo("/test2");
        assertThat(sanitizePath("/test/../test2/")).isEqualTo("/test2/");

        assertThat(sanitizePath("/test/./test2")).isEqualTo("/test/test2");
        assertThat(sanitizePath("/test/./test2/")).isEqualTo("/test/test2/");

        assertThat(sanitizePath("/test/../../test2")).isEqualTo("/test2");
        assertThat(sanitizePath("/test/../../test2/./test3")).isEqualTo("/test2/test3");

        assertThat(sanitizePath("/test/../../test2/./test3/../")).isEqualTo("/test2/");
        assertThat(sanitizePath("/test/../../test2/./test3/.././")).isEqualTo("/test2/");

        assertThat(sanitizePath("/test/../../test2/./test3/.././../")).isEqualTo("/");
        assertThat(sanitizePath("/test/../../test2/./test3/.././../../")).isEqualTo("/");
    }

    @Test
    void sanitizePathRemovesLeftOverPathTraversals() {
        assertThat(sanitizePath("/../../test/")).isEqualTo("/test/");
        assertThat(sanitizePath("/../../test/../")).isEqualTo("/");
    }

}