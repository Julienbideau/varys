package org.varys.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.varys.common.model.GitConfig;
import org.varys.common.model.LoggingConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public final class ConfigFactory {

    private static final ObjectMapper OBECT_MAPPER = new ObjectMapper(new YAMLFactory());

    private ConfigFactory() {
        super();
    }

    private static JsonNode findChild(JsonNode node, String path) {
        final String[] pathSegments = path.split("\\.");

        JsonNode currentNode = node;

        for (String segment: pathSegments) {
            if (currentNode != null) {
                currentNode = currentNode.get(segment);
            }
        }

        return currentNode;
    }

    private static <T> T getValue(JsonNode root, String path, Function<JsonNode, T> extractor, T defaultValue) {
        final JsonNode configValueNode = findChild(root, path);

        if (configValueNode != null) {
            return extractor.apply(configValueNode);
        } else {
            Log.info("Undefined configuration value '{}', using default value: '{}'", path, defaultValue);
            return defaultValue;
        }
    }

    private static JsonNode getConfigRootNode(File configFile) throws IOException {
        if (!configFile.getAbsolutePath().endsWith(".yml")) {
            throw new IllegalArgumentException("Configuration file must be in YAML format");
        }

        return OBECT_MAPPER.readValue(configFile, JsonNode.class);
    }

    static String getString(JsonNode root, String path, String defaultValue) {
        return getValue(root, path, JsonNode::asText, defaultValue);
    }

    static boolean getBoolean(JsonNode root, String path, boolean defaultValue) {
        return getValue(root, path, JsonNode::asBoolean, defaultValue);
    }

    static long getLong(JsonNode root, String path, long defaultValue) {
        return getValue(root, path, JsonNode::asLong, defaultValue);
    }

    public static int getThreadPoolSize(File configFile) throws IOException {
        final JsonNode configRootNode = getConfigRootNode(configFile);
        return (int) getLong(configRootNode,"thread_pool_size", Runtime.getRuntime().availableProcessors());
    }

    public static LoggingConfig createLoggingConfig(File configFile) throws IOException {
        Log.debug("Retreiving logging config from file: {}...", configFile);
        final JsonNode configRootNode = getConfigRootNode(configFile);
        final String loggingFilePath = getString(configRootNode, "logging.file", "varys.log");
        final String loggingLevel = getString(configRootNode, "logging.level", "INFO");
        return new LoggingConfig(loggingFilePath, loggingLevel);
    }

    public static GitConfig createGitConfig(File configFile) throws IOException {
        Log.debug("Retreiving git config from file: {}...", configFile);
        final JsonNode configRootNode = getConfigRootNode(configFile);
        final String rootDirectoryString = getString(configRootNode, "git_projects_directory", "");
        final File rootDirectory = new File(rootDirectoryString);
        return new GitConfig(rootDirectory);
    }

    public static Collection<JsonNode> findModuleNodes(File configFile) throws IOException {
        Log.debug("Retreiving modules from config file: {}...", configFile);
        final JsonNode configRootNode = getConfigRootNode(configFile);
        final Iterator<JsonNode> moduleConfigNodesIterator = configRootNode.get("modules").elements();
        final List<JsonNode> moduleConfigNodesList = new ArrayList<>();
        moduleConfigNodesIterator.forEachRemaining(moduleConfigNodesList::add);
        Log.debug("Retreived {} modules from config file", moduleConfigNodesList.size());
        return moduleConfigNodesList;
    }
}
