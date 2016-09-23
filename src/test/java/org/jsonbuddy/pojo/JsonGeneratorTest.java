package org.jsonbuddy.pojo;

import org.jsonbuddy.*;
import org.jsonbuddy.pojo.testclasses.*;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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
        assertThat(JsonGenerator.generate(null)).isEqualTo(new JsonNull());
        assertThat(JsonGenerator.generate("Darth")).isEqualTo(JsonFactory.jsonString("Darth"));
        assertThat(JsonGenerator.generate(42)).isEqualTo(JsonFactory.jsonNumber(42));

    }

    @Test
    public void shoulHandleFloats() throws Exception {
        JsonNode jsonNode = JsonGenerator.generate(3.14f);
        JsonNumber jsonDouble = (JsonNumber) jsonNode;
        assertThat(new Double(jsonDouble.doubleValue()).floatValue()).isEqualTo(3.14f);
    }

    @Test
    public void shouldHandleList() throws Exception {
        List<String> stringlist = Arrays.asList("one", "two", "three");

        JsonNode generate = JsonGenerator.generate(stringlist);
        assertThat(generate).isInstanceOf(JsonArray.class);
        JsonArray array = (JsonArray) generate;
        assertThat(array.strings()).isEqualTo(stringlist);
    }

    @Test
    public void shouldHandleListWithClasses() throws Exception {
        List<SimpleWithName> simpleWithNames = Arrays.asList(new SimpleWithName("Darth"), new SimpleWithName("Anakin"));
        JsonArray array = (JsonArray) JsonGenerator.generate(simpleWithNames);

        List<JsonObject> objects = array.objects(o -> o);

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
        Optional<JsonObject> person = jsonObject.objectValue("person");

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

    @Test
    public void shoulHandleEmbeddedJson() throws Exception {
        ClassWithJsonElements classWithJsonElements = new ClassWithJsonElements("Darth Vader",
                JsonFactory.jsonObject().put("title", "Dark Lord"),
                JsonFactory.jsonArray().add("Luke").add("Leia"));
        JsonObject generate = (JsonObject) JsonGenerator.generate(classWithJsonElements);

        assertThat(generate.requiredString("name")).isEqualTo("Darth Vader");
        assertThat(generate.requiredObject("myObject").requiredString("title")).isEqualTo("Dark Lord");
        assertThat(generate.requiredArray("myArray").stringStream().collect(Collectors.toList())).containsExactly("Luke", "Leia");
    }

    @Test
    public void shouldMakeMapsIntoObjects() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("name", "Darth Vader");
        ClassWithMap classWithMap = new ClassWithMap(map);
        JsonObject generate = (JsonObject) JsonGenerator.generate(classWithMap);
        assertThat(generate.requiredObject("properties").requiredString("name")).isEqualTo("Darth Vader");
    }

    @Test
    public void shouldHandleDifferentSimpleTypes() throws Exception {
        ClassWithDifferentTypes classWithDifferentTypes = new ClassWithDifferentTypes("my text", 42, true, false);
        JsonObject generated = (JsonObject) JsonGenerator.generate(classWithDifferentTypes);
        assertThat(generated.requiredString("text")).isEqualTo("my text");
        assertThat(generated.requiredLong("number")).isEqualTo(42);
        assertThat(generated.requiredBoolean("bool")).isTrue();
    }

    @Test
    public void shouldHandleClassWithTime() throws Exception {
        OffsetDateTime dateTime = OffsetDateTime.of(2015, 8, 13, 21, 14, 18, 321, ZoneOffset.UTC);
        ClassWithTime classWithTime = new ClassWithTime();
        classWithTime.setTime(dateTime.toInstant());

        JsonObject jsonNode = (JsonObject) JsonGenerator.generate(classWithTime);
        assertThat(jsonNode.requiredString("time")).isEqualTo("2015-08-13T21:14:18.000000321Z");
    }

    @Test
    public void shouldHandleClassWithEnum() throws Exception {
        ClassWithEnum classWithEnum = new ClassWithEnum();
        JsonObject jso = (JsonObject) JsonGenerator.generate(classWithEnum);
        assertThat(jso.requiredString("enumNumber")).isEqualTo("ONE");
    }

    @Test
    public void shouldHandleNumbers() throws Exception {
        assertThat(JsonGenerator.generate(12L)).isEqualTo(new JsonNumber(12L));
        assertThat(JsonGenerator.generate(12)).isEqualTo(new JsonNumber(12));
    }

    @Test
    public void shouldHandleMapWithKeyOtherThanString() throws Exception {
        Map<Long,String> myLongMap = new HashMap<>();
        myLongMap.put(42L, "Meaning of life");
        JsonNode generated = JsonGenerator.generate(myLongMap);
        assertThat(generated).isInstanceOf(JsonObject.class);
        JsonObject jsonObject = (JsonObject) generated;
        assertThat(jsonObject.requiredString("42")).isEqualTo("Meaning of life");
    }

    @Test
    public void shouldHandleArrays() throws Exception {
        Integer[] integerArray = IntStream.range(0, 10).mapToObj(Integer::valueOf).toArray(Integer[]::new);
        JsonNode generated = JsonGenerator.generate(integerArray);

        assertThat(generated).isInstanceOf(JsonArray.class);
        JsonArray jsonArray = (JsonArray) generated;
        assertThat(jsonArray).hasSize(10);
        Integer[] integerArrayFromJson = jsonArray.nodeStream().map(n -> ((JsonNumber) n).intValue()).toArray(Integer[]::new);
        assertThat(integerArrayFromJson).isEqualTo(integerArray);
    }

    @Test
    public void shouldHandlePrimitiveIntegerNumberArrays() throws Exception {
        int[] intArray = IntStream.range(0, 10).toArray();
        JsonNode generated = JsonGenerator.generate(intArray);

        assertThat(generated).isInstanceOf(JsonArray.class);
        JsonArray jsonArray = (JsonArray) generated;
        assertThat(jsonArray).hasSize(intArray.length);
        int[] intArrayFromJson = jsonArray.nodeStream().mapToInt(n -> ((JsonNumber) n).intValue()).toArray();
        assertThat(intArrayFromJson).isEqualTo(intArray);

        long[] longArray = LongStream.range(0, 10).toArray();
        generated = JsonGenerator.generate(longArray);

        assertThat(generated).isInstanceOf(JsonArray.class);
        jsonArray = (JsonArray) generated;
        assertThat(jsonArray).hasSize(longArray.length);
        long[] longArrayFromJson = jsonArray.nodeStream().mapToLong(n -> ((JsonNumber) n).longValue()).toArray();
        assertThat(longArrayFromJson).isEqualTo(longArray);
    }

    @Test
    public void shouldHandlePrimitiveFloatingPointNumberArrays() throws Exception {
        double[] doubleArray = DoubleStream.iterate(2.0, Math::sqrt).limit(10).toArray();
        JsonNode generated = JsonGenerator.generate(doubleArray);

        assertThat(generated).isInstanceOf(JsonArray.class);
        JsonArray jsonArray = (JsonArray) generated;
        assertThat(jsonArray).hasSize(doubleArray.length);
        double[] doubleArrayFromJson = jsonArray.nodeStream().mapToDouble(n -> ((JsonNumber) n).doubleValue()).toArray();
        assertThat(doubleArrayFromJson).isEqualTo(doubleArray);

        float[] floatArray = new float[doubleArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = (float) doubleArray[i];
        }
        generated = JsonGenerator.generate(floatArray);

        assertThat(generated).isInstanceOf(JsonArray.class);
        List<Float> floatListFromJson = ((JsonArray) generated).nodeStream().map(node -> (JsonNumber) node).map(JsonNumber::floatValue).collect(Collectors.toList());
        assertThat(floatListFromJson).hasSize(floatArray.length);
        float[] floatArrayFromJson = new float[floatArray.length];
        for (int i = 0; i < floatListFromJson.size(); i++) {
            floatArrayFromJson[i] = floatListFromJson.get(i);
        }
        assertThat(floatArrayFromJson).isEqualTo(floatArray);
    }

    @Test
    public void shouldHandlePrimitiveBooleanArrays() throws Exception {
        boolean[] booleanArray = new boolean[10];
        Iterator<Boolean> it = DoubleStream.iterate(0, d -> Math.random()).mapToObj(d -> d > 0.5).iterator();
        for (int i = 0; i < booleanArray.length; i++) {
            booleanArray[i] = it.next();
        }
        JsonNode generated = JsonGenerator.generate(booleanArray);

        assertThat(generated).isInstanceOf(JsonArray.class);
        List<Boolean> booleanListFromJson = ((JsonArray) generated).nodeStream().map(node -> (JsonBoolean) node).map(JsonBoolean::booleanValue).collect(Collectors.toList());
        assertThat(booleanListFromJson).hasSize(booleanArray.length);
        boolean[] booleanArrayFromJson = new boolean[booleanArray.length];
        for (int i = 0; i < booleanListFromJson.size(); i++) {
            booleanArrayFromJson[i] = booleanListFromJson.get(i);
        }
        assertThat(booleanArrayFromJson).isEqualTo(booleanArray);
    }
}
