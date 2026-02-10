package com.openclawlite.adapter.protocol.dto;

import java.util.Map;

public record ChannelsStatusResult(Map<String, Map<String, ChannelStatusInfo>> channels) {}
