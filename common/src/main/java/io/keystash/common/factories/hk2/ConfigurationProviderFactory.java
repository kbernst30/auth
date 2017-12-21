package io.keystash.common.factories.hk2;

import io.keystash.common.annotation.ConfigFile;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.compose.FallbackConfigurationSource;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.files.FilesConfigurationSource;
import org.glassfish.hk2.api.Factory;
import org.reflections.Reflections;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationProviderFactory implements Factory<ConfigurationProvider> {

    private static final String CONFIG_DIRECTORY = ""; // TODO this should be a real value

    @Override
    public ConfigurationProvider provide() {
        Set<Path> classpathFiles = new HashSet<>();
        Set<Path> serverFiles = new HashSet<>();

        Reflections reflections = new Reflections("io.keystash.common.configuration");
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(ConfigFile.class);
        annotatedClasses.forEach(clazz -> {
            ConfigFile configFile = clazz.getAnnotation(ConfigFile.class);
            classpathFiles.add(Paths.get(configFile.value()));
            serverFiles.add(Paths.get(CONFIG_DIRECTORY + configFile.value()));
        });

        ConfigFilesProvider classPathFilesProvider = () -> classpathFiles;
        ConfigFilesProvider serverFilesProvider = () -> serverFiles;

        ClasspathConfigurationSource classpathConfigurationSource = new ClasspathConfigurationSource(classPathFilesProvider);
        FilesConfigurationSource serverFileConfigurationSource = new FilesConfigurationSource(serverFilesProvider);
        FallbackConfigurationSource fallbackConfigurationSource = new FallbackConfigurationSource(serverFileConfigurationSource, classpathConfigurationSource);

        return new ConfigurationProviderBuilder()
                .withConfigurationSource(fallbackConfigurationSource)
                .build();
    }

    @Override
    public void dispose(ConfigurationProvider configurationProvider) {}
}
