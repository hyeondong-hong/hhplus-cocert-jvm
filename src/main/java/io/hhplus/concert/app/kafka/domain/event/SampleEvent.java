package io.hhplus.concert.app.kafka.domain.event;

public record SampleEvent(
        String sampleName,
        String someMessage
) {
}
