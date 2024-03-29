package com.rnmp;


import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.file.sink.FileSink;

// this was my first attempt to get started with flink
// read the data from kafka and write it to a file
// this is all raw text, no decoding the json to object


public class FirstAttempt {
    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        KafkaSource<String> source = KafkaSource.<String>builder()
                // these are just kafka configs, we can just do fine with first three
                // but the rest of them are just for tuning the performance
                .setBootstrapServers("localhost:9092") // Set your Kafka bootstrap server
                .setGroupId("test-group") // Set your Kafka consumer group
                .setTopics("sensors") // Set your Kafka topic
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        FileSink<String> sink = FileSink
                .forRowFormat(new Path("/tmp/output"), new SimpleStringEncoder<String>("UTF-8"))
                .build();

        stream.sinkTo(sink);

        env.execute("Flink Kafka to File Sink Job");
    }

}
