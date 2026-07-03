package com.example.filestore.service;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.example.filestore.service.impl.Sha256ChecksumService;

import static org.junit.jupiter.api.Assertions.*;

class Sha256ChecksumServiceTest {

    private final Sha256ChecksumService service = new Sha256ChecksumService();

    @Test
    void shouldComputeCorrectChecksum() {
        final var digest = service.createDigest();
        final var data = "hello".getBytes(StandardCharsets.UTF_8);
        service.update(digest, data, data.length);
        final var checksum = service.finish(digest);

        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", checksum);
    }

    @Test
    void shouldSupportIncrementalUpdates() {
        final var digest = service.createDigest();
        final var part1 = "hel".getBytes(StandardCharsets.UTF_8);
        final var part2 = "lo".getBytes(StandardCharsets.UTF_8);
        service.update(digest, part1, part1.length);
        service.update(digest, part2, part2.length);
        final var checksum = service.finish(digest);

        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", checksum);
    }
}
