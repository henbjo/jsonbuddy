package org.jsonbuddy.pojo;

import org.jsonbuddy.*;
import org.jsonbuddy.pojo.testclasses.CombinedClassWithSetter;
import org.jsonbuddy.pojo.testclasses.JsonGeneratorOverrides;
import org.jsonbuddy.pojo.testclasses.SimpleWithName;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonGeneratorTest {

    @Test
    public void shouldHandleSimpleClass() throws Exception {
        SimpleWithName simpleWithName = new SimpleWithName("Darth Vader");
        JsonNode generated = JsonGenerator.generate(simpleWithName);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.stringValue("name").get()).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleSimpleValues() throws Exception {
        assertThat(JsonGenerator.generate(null)).isEqualTo(new JsonNullValue());
        assertThat(JsonGenerator.generate("Darth")).isEqualTo(JsonFactory.jsonText("Darth"));
        assertThat(JsonGenerator.generate(42)).isEqualTo(JsonFactory.jsonLong(42L));

    }

    @Test
    public void shoulHandleFloats() throws Exception {
        JsonNode jsonNode = JsonGenerator.generate(3.14f);
        JsonDouble jsonDouble = (JsonDouble) jsonNode;
        assertThat(new Double(jsonDouble.doubleValue()).floatValue()).isEqualTo(3.14f);
    }

    @Test
    public void shouldHandleList() throws Exception {
        List<String> stringlist = Arrays.asList("one", "two", "three");

        JsonNode generate = JsonGenerator.generate(stringlist);
        assertThat(generate).isInstanceOf(JsonArray.class);
        JsonArray array = (JsonArray) generate;
        assertThat(array.nodeStream().map(JsonNode::textValue).collect(Collectors.toList())).isEqualTo(stringlist);
    }

    @Test
    public void shouldHandleListWithClasses() throws Exception {
        List<SimpleWithName> simpleWithNames = Arrays.asList(new SimpleWithName("Darth"), new SimpleWithName("Anakin"));
        JsonArray array = (JsonArray) JsonGenerator.generate(simpleWithNames);

        List<JsonObject> objects = array.nodeStream().map(no -> (JsonObject) no).collect(Collectors.toList());

        assertThat(objects.get(0).stringValue("name").get()).isEqualTo("Darth");
        assertThat(objects.get(1).stringValue("name").get()).isEqualTo("Anakin");
    }

    @Test
    public void shouldHandleClassWithGetter() throws Exception {
        CombinedClassWithSetter combinedClassWithSetter = new CombinedClassWithSetter();
        combinedClassWithSetter.setPerson(new SimpleWithName("Darth Vader"));
        combinedClassWithSetter.setOccupation("Dark Lord");

        JsonObject jsonObject = (JsonObject) JsonGenerator.generate(combinedClassWithSetter);

        assertThat(jsonObject.stringValue("occupation").get()).isEqualTo("Dark Lord");
        Optional<JsonNode> person = jsonObject.value("person");

        assertThat(person).isPresent();
        assertThat(person.get()).isInstanceOf(JsonObject.class);
        assertThat(person.get().requiredString("name")).isEqualTo("Darth Vader");

    }

    @Test
    public void shouldHandleOverriddenValues() throws Exception {
        JsonGeneratorOverrides overrides = new JsonGeneratorOverrides();
        JsonObject generate = (JsonObject) JsonGenerator.generate(overrides);

        assertThat(generate.requiredLong("myOverriddenValue")).isEqualTo(42);

    }
}