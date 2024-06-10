package com.nickwellman.collections.models.config.repository;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RepositoryConfigParser {

    private static final Gson gson = new Gson();

    public static ItemDescriptor parse(final InputStream inputStream) {
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return gson.fromJson(br, ItemDescriptor.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
