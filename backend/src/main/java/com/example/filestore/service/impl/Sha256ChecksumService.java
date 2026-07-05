package com.example.filestore.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import com.example.filestore.service.ChecksumService;

/**
 * SHA-256 implementation of {@link ChecksumService}.
 */
@Service
public class Sha256ChecksumService implements ChecksumService {

    /**
     * Algorithm name used for digest creation.
     */
    private static final String ALGORITHM = "SHA-256";

    @Override
    public MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public void update(MessageDigest digest, byte[] data, int length) {
        digest.update(data, 0, length);
    }

    @Override
    public String finish(MessageDigest digest) {
        return HexFormat.of().formatHex(digest.digest());
    }
}
