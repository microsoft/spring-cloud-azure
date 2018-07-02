/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.telemetry;

import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GetHashMac {
    private static final String MAC_REGEX = "([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}";
    private static final String MAC_REGEX_ZERO = "([0]{2}[:-]){5}[0]{2}";
    private static final String HASHED_MAC_REGEX = "[0-9a-f]{64}";

    private static boolean isValidHashMacFormat(@NonNull String hashMac) {
        if (hashMac.isEmpty()) {
            return false;
        }

        return Pattern.compile(HASHED_MAC_REGEX).matcher(hashMac).matches();
    }

    private static String getRawMac() {
        final List<String> commands;
        final String os = System.getProperty("os.name");
        final StringBuilder macBuilder = new StringBuilder();

        if (os != null && !os.isEmpty() && os.toLowerCase(Locale.US).startsWith("win")) {
            commands = Collections.singletonList("getmac");
        } else {
            commands = Arrays.asList("ifconfig", "-a");
        }

        try {
            String tmp;
            final ProcessBuilder builder = new ProcessBuilder(commands);
            final Process process = builder.start();
            @Cleanup final InputStreamReader streamReader = new InputStreamReader(process.getInputStream(), "utf-8");
            @Cleanup final BufferedReader reader = new BufferedReader(streamReader);

            while ((tmp = reader.readLine()) != null) {
                macBuilder.append(tmp);
            }
        } catch (IOException e) {
            return null;
        }

        return macBuilder.toString();
    }

    private static String getHexDigest(byte digest) {
        final String hex = Integer.toString((digest & 0xff) + 0x100, 16);

        return hex.substring(1);
    }

    private static String hash(@NonNull String mac) {
        if (mac.isEmpty()) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();

        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            messageDigest.update(mac.getBytes("UTF-8"));

            final byte[] digestBytes = messageDigest.digest();

            for (final byte digest : digestBytes) {
                builder.append(getHexDigest(digest));
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            return null;
        }

        Assert.isTrue(isValidHashMacFormat(builder.toString()), "Invalid format for HashMac");

        return builder.toString();
    }

    public static String getHashMac() {
        final String rawMac = getRawMac();

        if (rawMac == null || rawMac.isEmpty()) {
            return null;
        }

        final Pattern pattern = Pattern.compile(MAC_REGEX);
        final Pattern patternZero = Pattern.compile(MAC_REGEX_ZERO);
        final Matcher matcher = pattern.matcher(rawMac);

        String mac = null;

        while (matcher.find()) {
            mac = matcher.group(0);

            if (!patternZero.matcher(mac).matches()) {
                break;
            }
        }

        Assert.notNull(mac, "cannot find any string from matcher:" + MAC_REGEX);

        return hash(mac);
    }
}

