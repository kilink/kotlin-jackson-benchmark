package net.kilink.perf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.google.common.collect.ImmutableList;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

public class JacksonKotlinPerfTest {

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        public ObjectMapper mapper = new ObjectMapper()
                .registerModules(new AfterburnerModule(), new KotlinModule());

        public String serializedObject;
        public KotlinObject kotlinObject = buildKotlinObject();
        public JavaObject javaObject = buildJavaObject();

        @Setup
        public void setUp() throws IOException {
            kotlinObject = buildKotlinObject();
            javaObject = buildJavaObject();
            serializedObject = buildMapper().writeValueAsString(javaObject);
        }

        private static ObjectMapper buildMapper() {
            return new ObjectMapper()
                    .registerModules(new AfterburnerModule(), new KotlinModule());
        }

        private static KotlinObject buildKotlinObject() {
            String values[] = {"foo", "bar", "baz", "quux"};
            KotlinObject obj = new KotlinObject("last", ImmutableList.of());
            for (String value : values) {
                List<KotlinObject> children = new ArrayList<>();
                for (String val : values) {
                    children.add(new KotlinObject(val, ImmutableList.of(obj)));
                }
                obj = new KotlinObject(value, children);
            }
            return obj;
        }

        private static JavaObject buildJavaObject() {
            String values[] = {"foo", "bar", "baz", "quux"};
            JavaObject obj = new JavaObject("last", ImmutableList.of());
            for (String value : values) {
                List<JavaObject> children = new ArrayList<>();
                for (String val : values) {
                    children.add(new JavaObject(val, ImmutableList.of(obj)));
                }
                obj = new JavaObject(value, children);
            }
            return obj;
        }
    }

    private static class JavaObject {

        private final String foo;
        private final List<JavaObject> bar;

        @JsonCreator
        private JavaObject(@JsonProperty("foo") String foo,
                           @JsonProperty("bar") List<JavaObject> bar) {
            this.foo = foo;
            this.bar = bar;
        }

        public String getFoo() {
            return foo;
        }

        public List<JavaObject> getBar() {
            return bar;
        }
    }
/*
    public static class JavaFooSerializer extends StdSerializer<JavaObject> {

        public JavaFooSerializer() {
            super(JavaObject.class);
        }

        @Override
        public void serialize(JavaObject value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("bar", value.getBar());
            gen.writeArrayFieldStart("baz");
            for (String item : value.getBaz()) {
                gen.writeString(item);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public String serializeJavaObject(BenchmarkState state) throws JsonProcessingException {
        return state.mapper.writeValueAsString(state.javaObject);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public String serializeKotlinObject(BenchmarkState state) throws JsonProcessingException {
        return state.mapper.writeValueAsString(state.kotlinObject);
    }*/

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public JavaObject deserializeJavaObject(BenchmarkState state) throws IOException {
        return state.mapper.readValue(state.serializedObject, JavaObject.class);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public KotlinObject deserializeKotlinObject(BenchmarkState state) throws IOException {
        return state.mapper.readValue(state.serializedObject, KotlinObject.class);
    }
}
