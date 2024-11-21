package io.hhplus.concert.app.kafka.domain.event;

public record KafkaSampleEvent(
        String sampleName,
        String someMessage
) {
}
