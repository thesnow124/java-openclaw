package com.openclawlite.common.enums;

import java.util.Set;

/**
 * Capabilities supported by a channel.
 */
public record ChannelCapabilities(
    Set<ChatType> chatTypes,
    boolean polls,
    boolean reactions,
    boolean edit,
    boolean unsend,
    boolean reply,
    boolean effects,
    boolean groupManagement,
    boolean threads,
    boolean media,
    boolean nativeCommands,
    boolean blockStreaming
) {
    public ChannelCapabilities(Set<ChatType> chatTypes) {
        this(chatTypes, false, false, false, false, false, false,
              false, false, false, false, false);
    }

    public ChannelCapabilities(Set<ChatType> chatTypes, boolean polls, boolean reactions, boolean edit) {
        this(chatTypes, polls, reactions, edit, false, false, false,
              false, false, false, false, false);
    }

    public static ChannelCapabilities of(ChatType... types) {
        return new ChannelCapabilities(Set.of(types));
    }

    public static Builder builder() {
        return new Builder();
    }

    public ChannelCapabilities {
        if (chatTypes == null) chatTypes = Set.of();
    }

    public static class Builder {
        private Set<ChatType> chatTypes = Set.of();
        private boolean polls;
        private boolean reactions;
        private boolean edit;
        private boolean unsend;
        private boolean reply;
        private boolean effects;
        private boolean groupManagement;
        private boolean threads;
        private boolean media;
        private boolean nativeCommands;
        private boolean blockStreaming;

        public Builder chatTypes(Set<ChatType> chatTypes) {
            this.chatTypes = chatTypes;
            return this;
        }

        public Builder polls(boolean polls) {
            this.polls = polls;
            return this;
        }

        public Builder reactions(boolean reactions) {
            this.reactions = reactions;
            return this;
        }

        public Builder edit(boolean edit) {
            this.edit = edit;
            return this;
        }

        public Builder unsend(boolean unsend) {
            this.unsend = unsend;
            return this;
        }

        public Builder reply(boolean reply) {
            this.reply = reply;
            return this;
        }

        public Builder effects(boolean effects) {
            this.effects = effects;
            return this;
        }

        public Builder groupManagement(boolean groupManagement) {
            this.groupManagement = groupManagement;
            return this;
        }

        public Builder threads(boolean threads) {
            this.threads = threads;
            return this;
        }

        public Builder media(boolean media) {
            this.media = media;
            return this;
        }

        public Builder nativeCommands(boolean nativeCommands) {
            this.nativeCommands = nativeCommands;
            return this;
        }

        public Builder blockStreaming(boolean blockStreaming) {
            this.blockStreaming = blockStreaming;
            return this;
        }

        public ChannelCapabilities build() {
            return new ChannelCapabilities(chatTypes, polls, reactions, edit, unsend,
                    reply, effects, groupManagement, threads, media,
                    nativeCommands, blockStreaming);
        }
    }

    /**
     * Chat types supported by channels
     */
    public enum ChatType {
        DIRECT,
        GROUP,
        CHANNEL,
        THREAD,
        BROADCAST
    }
}
