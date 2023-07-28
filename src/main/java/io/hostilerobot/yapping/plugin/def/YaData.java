package io.hostilerobot.yapping.plugin.def;

/**
 * represents data that we can
 */
public record YaData<T>(YaDataType<T> data, YaLookup lookup) {
}
