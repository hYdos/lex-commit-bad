package org.bukkit.craftbukkit.help;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.help.*;

import java.util.*;

/**
 * Standard implementation of {@link HelpMap} for CraftBukkit servers.
 */
public class SimpleHelpMap implements HelpMap {
    
    private HelpTopic defaultTopic;
    private Map<String, HelpTopic> helpTopics;
    private Set<HelpTopic> pluginIndexes; 
    private Map<Class, HelpTopicFactory<Command>> topicFactoryMap;
    private CraftServer server;

    public SimpleHelpMap(CraftServer server) {
        helpTopics = new TreeMap<String, HelpTopic>(new HelpTopicComparator.TopicNameComparator()); // Using a TreeMap for its explicit sorting on key
        pluginIndexes = new TreeSet<HelpTopic>(new HelpTopicComparator());
        defaultTopic = new IndexHelpTopic("Index", null, null, Collections2.filter(helpTopics.values(), Predicates.not(Predicates.instanceOf(CommandAliasHelpTopic.class))));
        topicFactoryMap = new HashMap<Class, HelpTopicFactory<Command>>();
        this.server = server;

        registerHelpTopicFactory(MultipleCommandAlias.class, new MultipleCommandAliasHelpTopicFactory());
    }
    
    public synchronized HelpTopic getHelpTopic(String topicName) {
        if (topicName.equals("")) {
            return defaultTopic;
        }

        if (helpTopics.containsKey(topicName)) {
            return helpTopics.get(topicName);
        }

        return null;
    }

    public Collection<HelpTopic> getHelpTopics() {
        return helpTopics.values();
    }

    public synchronized void addTopic(HelpTopic topic) {
        // Existing topics take priority
        if (!helpTopics.containsKey(topic.getName())) {
            helpTopics.put(topic.getName(), topic);
        }
    }

    public synchronized void clear() {
        helpTopics.clear();
    }

    public List<String> getIgnoredPlugins() {
        return new HelpYamlReader(server).getIgnoredPlugins();
    }

    /**
     * Reads the general topics from help.yml and adds them to the help index.
     */
    public synchronized void initializeGeneralTopics() {
        HelpYamlReader reader = new HelpYamlReader(server);

        // Initialize general help topics from the help.yml file
        for (HelpTopic topic : reader.getGeneralTopics()) {
            addTopic(topic);
        }
    }

    /**
     * Processes all the commands registered in the server and creates help topics for them.
     */
    public synchronized void initializeCommands() {
        // ** Load topics from highest to lowest priority order **
        HelpYamlReader helpYamlReader = new HelpYamlReader(server);
        List<String> ignoredPlugins = helpYamlReader.getIgnoredPlugins();

        // Initialize help topics from the server's command map
        outer: for (Command command : server.getCommandMap().getCommands()) {
            if (commandInIgnoredPlugin(command, ignoredPlugins)) {
                continue outer;
            }

            // Register a topic
            for (Class c : topicFactoryMap.keySet()) {
                if (c.isAssignableFrom(command.getClass())) {
                    HelpTopic t = topicFactoryMap.get(c).createTopic(command);
                    if (t != null) addTopic(t);
                    continue outer;
                }
                if (command instanceof PluginCommand && c.isAssignableFrom(((PluginCommand)command).getExecutor().getClass())) {
                    HelpTopic t = topicFactoryMap.get(c).createTopic(command);
                    if (t != null) addTopic(t);
                    continue outer;
                }
            }
            addTopic(new GenericCommandHelpTopic(command));
        }
        
        // Initialize command alias help topics
        for (Command command : server.getCommandMap().getCommands()) {
            if (commandInIgnoredPlugin(command, ignoredPlugins)) {
                continue;
            }
            for (String alias : command.getAliases()) {
                addTopic(new CommandAliasHelpTopic(alias, command.getLabel(), this));
            }
        }

        // Initialize help topics from the server's fallback commands
        for (VanillaCommand command : server.getCommandMap().getFallbackCommands()) {
            if (!commandInIgnoredPlugin(command, ignoredPlugins)) {
                addTopic(new GenericCommandHelpTopic(command));
            }
        }

        // Add alias sub-index
        addTopic(new IndexHelpTopic("Aliases", "Lists command aliases", null, Collections2.filter(helpTopics.values(), Predicates.instanceOf(CommandAliasHelpTopic.class))));
        
        // Initialize plugin-level sub-topics
        Map<String, Set<HelpTopic>> pluginIndexes = new HashMap<String, Set<HelpTopic>>();
        fillPluginIndexes(pluginIndexes, server.getCommandMap().getCommands());
        fillPluginIndexes(pluginIndexes, server.getCommandMap().getFallbackCommands());

        for (Map.Entry<String, Set<HelpTopic>> entry : pluginIndexes.entrySet()) {
            addTopic(new IndexHelpTopic(entry.getKey(), "All commands for " + entry.getKey(), null, entry.getValue(), ChatColor.GRAY + "Below is a list of all " + entry.getKey() + " commands:"));
        }

        // Amend help topics from the help.yml file
        for (HelpTopicAmendment amendment : helpYamlReader.getTopicAmendments()) {
            if (helpTopics.containsKey(amendment.getTopicName())) {
                helpTopics.get(amendment.getTopicName()).amendTopic(amendment.getShortText(), amendment.getFullText());
                if (amendment.getPermission() != null) {
                    helpTopics.get(amendment.getTopicName()).amendCanSee(amendment.getPermission());
                }
            }
        }
    }
    
    private void fillPluginIndexes(Map<String, Set<HelpTopic>> pluginIndexes, Collection<? extends Command> commands) {
        for (Command command : commands) {
            String pluginName = getCommandPluginName(command);
            if (pluginName != null) {
                HelpTopic topic = getHelpTopic("/" + command.getLabel());
                if (topic != null) {
                    if (!pluginIndexes.containsKey(pluginName)) {
                        pluginIndexes.put(pluginName, new TreeSet<HelpTopic>(new HelpTopicComparator())); //keep things in topic order
                    }
                    pluginIndexes.get(pluginName).add(topic);
                }
            }
        }
    }
    
    private String getCommandPluginName(Command command) {
        if (command instanceof BukkitCommand || command instanceof VanillaCommand) {
            return "Bukkit";
        }
        if (command instanceof PluginIdentifiableCommand) {
            return ((PluginIdentifiableCommand)command).getPlugin().getName();
        }
        return null;
    }
    
    private boolean commandInIgnoredPlugin(Command command, List<String> ignoredPlugins) {
        if ((command instanceof BukkitCommand || command instanceof VanillaCommand) && ignoredPlugins.contains("Bukkit")) {
            return true;
        }
        if (command instanceof PluginIdentifiableCommand && ignoredPlugins.contains(((PluginIdentifiableCommand)command).getPlugin().getName())) {
            return true;
        }
        return false;
    }

    public void registerHelpTopicFactory(Class commandClass, HelpTopicFactory factory) {
        if (!Command.class.isAssignableFrom(commandClass) && !CommandExecutor.class.isAssignableFrom(commandClass)) {
            throw new IllegalArgumentException("commandClass must implement either Command or CommandExecutor!");
        }
        topicFactoryMap.put(commandClass, factory);
    }
}
