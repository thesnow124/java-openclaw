package com.openclawlite.adapter.channel.registry;

import com.openclawlite.openclaw.domain.channel.core.ChannelPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for all channel plugins.
 * Manages channel registration, lookup, and iteration.
 */
@Component
public class ChannelRegistry {

    private static final Logger log = LoggerFactory.getLogger(ChannelRegistry.class);

    private final Map<String, RegisteredChannel> channels = new ConcurrentHashMap<>();

    /**
     * Register a channel plugin
     */
    public void register(ChannelPlugin plugin) {
        String id = plugin.getId();
        if (channels.containsKey(id)) {
            log.warn("Channel already registered, overwriting: {}", id);
        }

        RegisteredChannel registered = new RegisteredChannel(
            plugin,
            plugin.getMeta(),
            plugin.getCapabilities(),
            System.currentTimeMillis()
        );

        channels.put(id, registered);
        log.info("Registered channel: {} ({})", id, plugin.getMeta().label());
    }

    /**
     * Unregister a channel plugin
     */
    public void unregister(String channelId) {
        RegisteredChannel removed = channels.remove(channelId);
        if (removed != null) {
            log.info("Unregistered channel: {}", channelId);
        }
    }

    /**
     * Get a channel by ID
     */
    public Optional<ChannelPlugin> getChannel(String channelId) {
        RegisteredChannel registered = channels.get(channelId);
        return Optional.ofNullable(registered != null ? registered.plugin() : null);
    }

    /**
     * Get all registered channels
     */
    public Collection<ChannelPlugin> getAllChannels() {
        return channels.values().stream()
            .map(RegisteredChannel::plugin)
            .toList();
    }

    /**
     * Get all registered channel IDs
     */
    public Set<String> getChannelIds() {
        return new HashSet<>(channels.keySet());
    }

    /**
     * Check if a channel is registered
     */
    public boolean hasChannel(String channelId) {
        return channels.containsKey(channelId);
    }

    /**
     * Get channels sorted by display order
     */
    public List<ChannelPlugin> getChannelsSorted() {
        return channels.values().stream()
            .sorted(Comparator.comparingInt(c -> c.meta().order()))
            .map(RegisteredChannel::plugin)
            .toList();
    }

    /**
     * Get channels by capability
     */
    public List<ChannelPlugin> getChannelsWithCapability(ChannelCapabilityPredicate predicate) {
        return channels.values().stream()
            .filter(c -> predicate.test(c.capabilities()))
            .map(RegisteredChannel::plugin)
            .toList();
    }

    /**
     * Get channels that support a specific chat type
     */
    public List<ChannelPlugin> getChannelsSupportingChatType(com.openclawlite.common.enums.ChannelCapabilities.ChatType chatType) {
        return channels.values().stream()
            .filter(c -> c.capabilities().chatTypes().contains(chatType))
            .map(RegisteredChannel::plugin)
            .toList();
    }

    /**
     * Get channel count
     */
    public int getChannelCount() {
        return channels.size();
    }

    /**
     * Get channel metadata by ID
     */
    public Optional<com.openclawlite.openclaw.domain.channel.core.ChannelMeta> getChannelMeta(String channelId) {
        RegisteredChannel registered = channels.get(channelId);
        return Optional.ofNullable(registered != null ? registered.meta() : null);
    }

    /**
     * Get channel capabilities by ID
     */
    public Optional<com.openclawlite.common.enums.ChannelCapabilities> getChannelCapabilities(String channelId) {
        RegisteredChannel registered = channels.get(channelId);
        return Optional.ofNullable(registered != null ? registered.capabilities() : null);
    }

    /**
     * Clear all registered channels
     */
    public void clear() {
        int count = channels.size();
        channels.clear();
        log.info("Cleared {} channels", count);
    }

    /**
     * Get registry statistics
     */
    public RegistryStats getStats() {
        return new RegistryStats(
            channels.size(),
            channels.values().stream()
                .mapToInt(c -> c.capabilities().chatTypes().size())
                .sum(),
            channels.values().stream()
                .filter(c -> c.capabilities().media())
                .count(),
            channels.values().stream()
                .filter(c -> c.capabilities().polls())
                .count()
        );
    }

    /**
     * Registered channel data
     */
    private record RegisteredChannel(
        ChannelPlugin plugin,
        com.openclawlite.openclaw.domain.channel.core.ChannelMeta meta,
        com.openclawlite.common.enums.ChannelCapabilities capabilities,
        long registeredAt
    ) {}

    /**
     * Registry statistics
     */
    public record RegistryStats(
        int totalChannels,
        int totalChatTypes,
        long channelsWithMedia,
        long channelsWithPolls
    ) {}

    /**
     * Predicate for filtering channels by capability
     */
    @FunctionalInterface
    public interface ChannelCapabilityPredicate {
        boolean test(com.openclawlite.common.enums.ChannelCapabilities capabilities);
    }
}
