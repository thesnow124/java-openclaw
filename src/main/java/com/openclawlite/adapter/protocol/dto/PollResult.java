package com.openclawlite.adapter.protocol.dto;
import java.util.List;
public record PollResult(String pollId, String question, List<String> options) {}
