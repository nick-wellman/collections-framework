package com.nickwellman.collections.models.config.component;

import com.nickwellman.collections.GenericService;
import com.nickwellman.collections.jdbc.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Properties;

public class ComponentConfigParser {
    private static final String CLASS = "$class";

    public static GenericService parse(final InputStream is) {
        final Properties prop = new Properties();
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            prop.load(br);

            final String clazzPath = Optional.ofNullable(prop.getProperty(CLASS)).orElseThrow();
            final Class<GenericService> clazz = (Class<GenericService>) Class.forName(clazzPath);
            final GenericService genericService = clazz.getDeclaredConstructor().newInstance();

            final Field[] fields = genericService.getClass().getDeclaredFields();
            for (final Field field : fields) {
                field.setAccessible(true);
                field.set(genericService, Optional.ofNullable(prop.getProperty(field.getName())).orElseThrow());
            }

            return genericService;
        } catch (final IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException |
                       NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static DataSource parseDataSource(final InputStream is) {
        final Properties properties = new Properties();
        try {
            properties.load(is);
            final String clazzPath = Optional.of(properties.getProperty(CLASS)).orElseThrow();
            final Class<DataSource> clazz = (Class<DataSource>) Class.forName(clazzPath);
            return clazz.getDeclaredConstructor(String.class, String.class, String.class, String.class)
                        .newInstance(properties.getProperty("host"),
                                     properties.getProperty("database"),
                                     properties.getProperty("username"),
                                     properties.getProperty("password"));
        } catch (final IOException | ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                       NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }
}
