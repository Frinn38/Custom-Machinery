package fr.frinn.custommachinery.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.RecordBuilder;

import java.util.Comparator;
import java.util.Map;

public class MachineJsonOps extends JsonOps {

    public static final MachineJsonOps INSTANCE = new MachineJsonOps(false);
    private MachineJsonOps(boolean compressed) {
        super(compressed);
    }

    @Override
    public RecordBuilder<JsonElement> mapBuilder() {
        return new JsonRecordBuilder();
    }

    private class JsonRecordBuilder extends RecordBuilder.AbstractStringBuilder<JsonElement, JsonObject> {
        protected JsonRecordBuilder() {
            super(MachineJsonOps.this);
        }

        @Override
        protected JsonObject initBuilder() {
            return new JsonObject();
        }

        @Override
        protected JsonObject append(final String key, final JsonElement value, final JsonObject builder) {
            builder.add(key, value);
            return builder;
        }

        @Override
        protected DataResult<JsonElement> build(final JsonObject builder, final JsonElement prefix) {
            final JsonObject output = new JsonObject();
            builder.entrySet().stream().sorted(Comparator.comparingInt(entry -> this.compareKeys(entry.getKey())))
                    .forEach(entry -> output.add(entry.getKey(), entry.getValue()));
            if (prefix == null || prefix instanceof JsonNull) {
                return DataResult.success(output);
            }
            if (prefix instanceof JsonObject) {
                final JsonObject result = new JsonObject();
                for (final Map.Entry<String, JsonElement> entry : prefix.getAsJsonObject().entrySet()) {
                    result.add(entry.getKey(), entry.getValue());
                }
                for (final Map.Entry<String, JsonElement> entry : output.entrySet()) {
                    result.add(entry.getKey(), entry.getValue());
                }
                return DataResult.success(result);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
        }

        private int compareKeys(String key) {
            return -switch (key) {
                case "type" -> 10000;
                case "name" -> 1000;
                case "appearance" -> 900;
                case "components" -> 800;
                case "gui" -> 700;
                default -> 0;
            };
        }
    }
}
