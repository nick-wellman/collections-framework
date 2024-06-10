package com.nickwellman.collections.models.config.component;

import com.nickwellman.collections.jdbc.DataSource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentConfigParserTest {

    private InputStream inputStream;

    @Test
    public void testParseDataSource() {
        inputStream = new ByteArrayInputStream(buildPropertiesFile().getBytes());
        final DataSource ds = ComponentConfigParser.parseDataSource(inputStream);
        System.out.println(ds);

        assertThat(ds.getHost()).isEqualTo("localhost");
        assertThat(ds.getDatabase()).isEqualTo("testdb");
        assertThat(ds.getUsername()).isEqualTo("fancyuser");
        assertThat(ds.getPassword()).isEqualTo("passwrod");
    }

    private static String buildPropertiesFile() {
        final StringBuilder sb = new StringBuilder();

        sb.append("$class=com.nickwellman.collections.jdbc.MySqlDataSource");
        sb.append(System.lineSeparator());
        sb.append("host=localhost");
        sb.append(System.lineSeparator());
        sb.append("database=testdb");
        sb.append(System.lineSeparator());
        sb.append("username=fancyuser");
        sb.append(System.lineSeparator());
        sb.append("password=passwrod");
        sb.append(System.lineSeparator());

        return sb.toString();
    }
}
