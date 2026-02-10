package com.openclawlite.adapter.protocol.auth;

import com.openclawlite.common.dto.ErrorShape;
import com.openclawlite.adapter.protocol.dto.GatewayConstants;

import java.util.List;
import java.util.Set;

/**
 * Gateway authorization manager
 * Handles role and scope-based access control
 */
public class GatewayAuthManager {

    // Methods that require specific scopes
    private static final Set<String> READ_METHODS = Set.of(
        "health",
        "channels.status",
        "status",
        "agents.list",
        "agent.identity.get",
        "skills.status",
        "sessions.list",
        "sessions.preview",
        "models.list"
    );

    private static final Set<String> WRITE_METHODS = Set.of(
        "send",
        "agent",
        "agent.wait",
        "wake",
        "chat.send",
        "chat.abort"
    );

    private static final Set<String> APPROVALS_METHODS = Set.of(
        "exec.approval.request",
        "exec.approval.resolve"
    );

    private static final Set<String> PAIRING_METHODS = Set.of(
        "node.pair.request",
        "node.pair.list",
        "node.pair.approve",
        "node.pair.reject",
        "node.pair.verify",
        "device.pair.list",
        "device.pair.approve",
        "device.pair.reject",
        "device.token.rotate",
        "device.token.revoke",
        "node.rename"
    );

    // Methods that require admin scope
    private static final Set<String> ADMIN_METHOD_PREFIXES = Set.of("exec.approvals.");
    private static final Set<String> ADMIN_METHODS = Set.of(
        "agents.create",
        "agents.update",
        "agents.delete",
        "skills.install",
        "skills.update",
        "config.set",
        "config.apply",
        "config.patch",
        "channels.logout",
        "cron.add",
        "cron.update",
        "cron.remove",
        "cron.run",
        "sessions.patch",
        "sessions.reset",
        "sessions.delete",
        "sessions.compact",
        "agents.files.set",
        "update.run"
    );

    /**
     * Check if a method can be called by a client with given role and scopes
     *
     * @return null if authorized, ErrorShape if unauthorized
     */
    public ErrorShape authorizeMethod(String method, GatewayClient client) {
        if (client == null || client.getConnect() == null) {
            return null; // No auth required for local connections
        }

        String role = client.getConnect().role();
        List<String> scopes = client.getConnect().scopes();

        // Node role has specific methods
        if ("node".equals(role)) {
            if (isNodeMethod(method)) {
                return null;
            }
            return ErrorShape.unauthorized("unauthorized role: " + role);
        }

        // Only operator role allowed for other methods
        if (!"operator".equals(role)) {
            return ErrorShape.unauthorized("unauthorized role: " + role);
        }

        // Admin scope can do everything
        if (scopes.contains(GatewayConstants.Scopes.ADMIN)) {
            return null;
        }

        // Check scope-specific methods
        if (APPROVALS_METHODS.contains(method)) {
            if (!scopes.contains(GatewayConstants.Scopes.APPROVALS)) {
                return ErrorShape.unauthorized("missing scope: " + GatewayConstants.Scopes.APPROVALS);
            }
            return null;
        }

        if (PAIRING_METHODS.contains(method)) {
            if (!scopes.contains(GatewayConstants.Scopes.PAIRING)) {
                return ErrorShape.unauthorized("missing scope: " + GatewayConstants.Scopes.PAIRING);
            }
            return null;
        }

        if (READ_METHODS.contains(method)) {
            if (!(scopes.contains(GatewayConstants.Scopes.READ) || scopes.contains(GatewayConstants.Scopes.WRITE))) {
                return ErrorShape.unauthorized("missing scope: " + GatewayConstants.Scopes.READ);
            }
            return null;
        }

        if (WRITE_METHODS.contains(method)) {
            if (!scopes.contains(GatewayConstants.Scopes.WRITE)) {
                return ErrorShape.unauthorized("missing scope: " + GatewayConstants.Scopes.WRITE);
            }
            return null;
        }

        // Check admin methods
        for (String prefix : ADMIN_METHOD_PREFIXES) {
            if (method.startsWith(prefix)) {
                return ErrorShape.unauthorized("missing scope: " + GatewayConstants.Scopes.ADMIN);
            }
        }

        if (ADMIN_METHODS.contains(method)) {
            return ErrorShape.unauthorized("missing scope: " + GatewayConstants.Scopes.ADMIN);
        }

        // Unknown methods require admin scope
        return ErrorShape.unauthorized("missing scope: " + GatewayConstants.Scopes.ADMIN);
    }

    private boolean isNodeMethod(String method) {
        return PAIRING_METHODS.contains(method);
    }

    /**
     * Gateway client connection info
     */
    public static class GatewayClient {
        private final String id;
        private final ConnectInfo connect;

        public GatewayClient(String id, ConnectInfo connect) {
            this.id = id;
            this.connect = connect;
        }

        public String getId() { return id; }
        public ConnectInfo getConnect() { return connect; }
    }

    /**
     * Connection info from client
     */
    public static class ConnectInfo {
        private final String role;
        private final List<String> scopes;

        public ConnectInfo(String role, List<String> scopes) {
            this.role = role != null ? role : "operator";
            this.scopes = scopes != null ? scopes : List.of();
        }

        public String role() { return role; }
        public List<String> scopes() { return scopes; }
    }
}
