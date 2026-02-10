package com.openclawlite.openclaw.domain.channel.core;

import java.util.List;

/**
 * Metadata about a channel plugin.
 */
public record ChannelMeta(
    String id,                         // Unique channel ID
    String label,                      // Display name
    String selectionLabel,             // Label for selection UI
    String docsPath,                   // Path to documentation
    String docsLabel,                  // Label for docs link
    String blurb,                      // Short description
    int order,                         // Display order
    List<String> aliases,              // Alternative names
    String selectionDocsPrefix,        // Prefix for docs links
    boolean selectionDocsOmitLabel,    // Omit label in docs
    List<String> selectionExtras,      // Extra selection options
    String detailLabel,                // Detailed label
    String systemImage,                // System icon/image
    boolean showConfigured,            // Show configured status
    boolean quickstartAllowFrom,       // Allow quickstart from source
    boolean forceAccountBinding,       // Require account binding
    boolean preferSessionLookupForAnnounceTarget,  // Prefer session lookup for announces
    List<String> preferOver            // Prefer over these channels
) {
    // Simplified constructor
    public ChannelMeta(String id, String label, String blurb) {
        this(id, label, label, null, null, blurb, 0, List.of(), null, false,
             List.of(), null, null, false, false, false, false, List.of());
    }

    public ChannelMeta {
        if (aliases == null) aliases = List.of();
        if (selectionExtras == null) selectionExtras = List.of();
        if (preferOver == null) preferOver = List.of();
    }
}
