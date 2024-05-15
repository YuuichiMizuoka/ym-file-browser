package net.vamoscantar.utils;

import org.junit.jupiter.api.Test;

import static net.vamoscantar.utils.lang.StringUtils.buildTree;
import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

    @Test
    void buildTreeHandleUrlPathAsExpected() {
        var result = buildTree("/etc/nginx/sites-available/", "/");
        assertThat(result).containsExactly("/etc", "/etc/nginx", "/etc/nginx/sites-available");
    }

}