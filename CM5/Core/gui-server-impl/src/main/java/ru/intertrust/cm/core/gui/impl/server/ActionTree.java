package ru.intertrust.cm.core.gui.impl.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.intertrust.cm.core.config.gui.action.AbstractActionEntryConfig;
import ru.intertrust.cm.core.config.gui.action.ActionEntryConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.action.ActionSeparatorConfig;

/**
 *
 * Helper class to merge actions.
 *
 * @author Sergey.Okolot
 *         Created on 23.04.2014 16:07.
 */
public class ActionTree {

    private final Map<String, ActionEntryConfig> actionMap = new HashMap<>();
    private final Map<String, ActionNode> groupMap = new HashMap<>();
    private final Map<Class<? extends AbstractActionEntryConfig>, ConfigHandler> handlerMap;
    private final Map<ActionRefConfig, ActionNode> unresolvedRefs = new HashMap<>();
    private final Collection<ActionNode> unresolvedGroups = new ArrayList<>();
    private final ActionNode root = new ActionNode(null);

    public ActionTree(final Collection<ActionEntryConfig> actionConfigs) {
        handlerMap = new HashMap<>();
        handlerMap.put(ActionSeparatorConfig.class, new SeparatorConfigHandler());
        handlerMap.put(ActionRefConfig.class, new ActionRefConfigHandler());
        handlerMap.put(ActionEntryConfig.class, new ActionEntryConfigHandler());
        for (ActionEntryConfig config: actionConfigs) {
            if (config.getId() != null) {
                actionMap.put(config.getId(), config);
            }
        }
    }

    public void addAction(final AbstractActionEntryConfig config) {
        if (config.isRendered()) {
            final ConfigHandler handler = handlerMap.get(config.getClass());
            handler.handleConfig(root, config);
        }
    }

    public Collection getActions() {
        final Collection<AbstractActionEntryConfig> result = new ArrayList<>();
        for (ActionRefConfig actionRef : unresolvedRefs.keySet()) {
            if (actionMap.get(actionRef.getActionId()) != null) {
                final ConfigHandler resolver = handlerMap.get(ActionRefConfig.class);
                resolver.handleConfig(unresolvedRefs.get(actionRef), actionRef);
            } else {
                throw new IllegalArgumentException("Unresolved reference with id='"
                        + actionRef.getId() + "'"); // for developers only
            }
        }
        for (ActionNode node : unresolvedGroups) {
            if (groupMap.get(node.config.getGroupId()) != null) {
                final ActionNode parentGroup = groupMap.get(node.config.getGroupId());
                parentGroup.addChild(node);
            } else {
                root.children.add(node);
            }
        }
        return root.children;
    }

    /**
     * fixme will be used {@link #getActions()} method.
     * Helper method for unit test with package scope.
     * @return root of tree.
     */
    ActionNode getRoot() {
        return root;
    }

    class ActionNode { // fixme scope package
        private final AbstractActionEntryConfig config;
        private ActionNode parent;
        private final Collection<ActionNode> children = new ArrayList<>();

        public ActionNode(final AbstractActionEntryConfig config) {
            this.config = config;
        }

        public void addChild(final ActionNode node) {
            if (node.getId() != null) { // check cycle
                for (ActionNode parentNode = parent; parentNode != null; parentNode = parentNode.parent) {
                    if (node.getId().equals(parentNode.getId())) {
                        return;
                    }
                }
            }
            if (node.config.getGroupId() != null && !node.config.getGroupId().equals(node.getId())) {
                if (groupMap.get(node.config.getGroupId()) == null) {
                    unresolvedGroups.add(node);
                } else {
                    final ActionNode groupParent = groupMap.get(node.config.getGroupId());
                    groupParent.children.add(node);
                    node.parent = groupParent;
                }
            } else {
                children.add(node);
                node.parent = this;
            }
        }

        public Collection<ActionNode> getChildren() { // fixme remove method
            return children;
        }

        public String getId() {
            return config == null ? null : config.getId();
        }

        @Override
        public String toString() {
            return new StringBuilder(ActionNode.class.getSimpleName())
                    .append(": id=").append(getId()).append(" config=").append(config)
                    .toString();

        }
    }

    private interface ConfigHandler<T extends AbstractActionEntryConfig> {
        void handleConfig(ActionNode parent, T config);
    }

    private class SeparatorConfigHandler implements ConfigHandler<ActionSeparatorConfig> {

        @Override
        public void handleConfig(final ActionNode parent, final ActionSeparatorConfig config) {
            parent.addChild(new ActionNode(config));
        }
    }

    private class ActionRefConfigHandler implements ConfigHandler<ActionRefConfig> {

        @Override
        public void handleConfig(final ActionNode parent, final ActionRefConfig config) {
            if (actionMap.get(config.getActionId()) == null) {
                unresolvedRefs.put(config, parent);
                return;
            } else {
                final ActionEntryConfig actionConfig = actionMap.get(config.getActionId()).clone();
                if (!config.isShowText()) {
                    actionConfig.clearText();
                }
                if (!config.isShowImage()) {
                    actionConfig.clearImage();
                }
                if (config.getOrder() != null) {
                    actionConfig.setOrder(config.getOrder());
                }
                final ConfigHandler resolver = handlerMap.get(actionConfig.getClass());
                resolver.handleConfig(parent, actionConfig);
            }
        }
    }

    private class ActionEntryConfigHandler implements ConfigHandler<ActionEntryConfig> {

        @Override
        public void handleConfig(final ActionNode parent, final ActionEntryConfig config) {
            if (config.getId() != null && actionMap.get(config.getId()) == null) {
                actionMap.put(config.getId(), config);
            }
            final ActionNode node = new ActionNode(config);
            parent.addChild(node);
            if (node != null && node.getId() != null) {
                groupMap.put(node.getId(), node);
            }
            for (AbstractActionEntryConfig childConfig : config.getChildren()) {
                final ConfigHandler handler = handlerMap.get(childConfig.getClass());
                handler.handleConfig(node, childConfig);
            }
        }
    }
}
