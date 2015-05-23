package org.jsonbuddy;

import org.junit.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFactoryTest {
    @Test
    public void shouldCreateJsonNode() throws Exception {
        JsonObject jsonObject = JsonFactory.jsonObject()
                .create();
        assertThat(jsonObject).isNotNull();
    }

    @Test
    public void shouldCreateValue() throws Exception {
        JsonSimpleValueFactory text = JsonSimpleValueFactory.text("Darth Vader");
        JsonSimpleValue jsonSimpleValue = text.create();
        assertThat(jsonSimpleValue.value()).isEqualTo("Darth Vader");

    }

    @Test
    public void shouldCreateObjectWithValue() throws Exception {
        JsonObject jsonObject = JsonObjectFactory.jsonObject()
                .withValue("name", JsonSimpleValueFactory.text("Darth Vader"))
                .create();

        assertThat(jsonObject.stringValue("name").get()).isEqualTo("Darth Vader");
        assertThat(jsonObject.stringValue("xxx").isPresent()).isFalse();

    }

    @Test
    public void shouldCreateJsonArray() throws Exception {
        JsonArray jsonArray = JsonFactory.jsonArray()
                .add("Darth")
                .add("Luke")
                .create();
        assertThat(jsonArray.nodeStream()
                .map(an -> ((JsonSimpleValue) an).value())
                .collect(Collectors.toList())).containsExactly("Darth","Luke");
    }
}
