package org.jumpmind.symmetric.is.ui.views.flows;

import java.util.List;
import java.util.Map;

import org.jumpmind.symmetric.is.core.config.Component;
import org.jumpmind.symmetric.is.core.config.ComponentFlowNode;
import org.jumpmind.symmetric.is.core.config.ComponentFlowNodeLink;
import org.jumpmind.symmetric.is.core.config.ComponentFlowVersion;
import org.jumpmind.symmetric.is.core.config.ComponentVersion;
import org.jumpmind.symmetric.is.core.config.data.ComponentData;
import org.jumpmind.symmetric.is.core.config.data.ComponentFlowNodeData;
import org.jumpmind.symmetric.is.core.config.data.ComponentVersionData;
import org.jumpmind.symmetric.is.core.persist.IConfigurationService;
import org.jumpmind.symmetric.is.core.runtime.component.ComponentCategory;
import org.jumpmind.symmetric.is.core.runtime.component.IComponentFactory;
import org.jumpmind.symmetric.is.core.runtime.connection.IConnectionFactory;
import org.jumpmind.symmetric.is.ui.diagram.ConnectionEvent;
import org.jumpmind.symmetric.is.ui.diagram.Diagram;
import org.jumpmind.symmetric.is.ui.diagram.Node;
import org.jumpmind.symmetric.is.ui.diagram.NodeMovedEvent;
import org.jumpmind.symmetric.is.ui.diagram.NodeSelectedEvent;
import org.jumpmind.symmetric.is.ui.views.flows.ComponentSettingsSheet.IComponentSettingsChangedListener;
import org.jumpmind.symmetric.ui.common.ResizableWindow;
import org.jumpmind.symmetric.ui.common.UiComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@UiComponent
@Scope(value = "ui")
public class EditFlowWindow extends ResizableWindow implements IComponentSettingsChangedListener {

    private static final long serialVersionUID = 1L;

    @Autowired
    IConfigurationService configurationService;

    @Autowired
    IComponentFactory componentFactory;

    @Autowired
    IConnectionFactory connectionFactory;

    ComponentFlowVersion componentFlowVersion;

    VerticalLayout flowLayout;

    Accordion componentAccordian;

    TabSheet tabs;

    Tab palleteTab;

    Tab propertiesTab;

    ComponentSettingsSheet componentSettingsSheet;

    Diagram diagram;

    public EditFlowWindow() {

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);

        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        splitPanel.setSplitPosition(350, Unit.PIXELS);
        splitPanel.setSizeFull();

        flowLayout = new VerticalLayout();
        flowLayout.setSizeFull();

        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setWidth(100, Unit.PERCENTAGE);
        flowLayout.addComponent(actionLayout);

        MenuBar actionBar = new MenuBar();
        actionBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);
        actionBar.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
        MenuItem actions = actionBar.addItem("", FontAwesome.COG, null);
        actions.addItem("Deploy", FontAwesome.COGS, null);
        actions.addItem("Execute", FontAwesome.PLAY, null);

        actionLayout.addComponent(actionBar);
        actionLayout.setComponentAlignment(actionBar, Alignment.MIDDLE_RIGHT);

        componentSettingsSheet = new ComponentSettingsSheet();

        tabs = new TabSheet();
        tabs.addStyleName(ValoTheme.TABSHEET_COMPACT_TABBAR);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabs.setSizeFull();
        palleteTab = tabs.addTab(buildPalette(), "Design Palette");
        propertiesTab = tabs.addTab(componentSettingsSheet, "Property Sheet");

        splitPanel.addComponents(tabs, flowLayout);

        content.addComponent(splitPanel);
        content.setExpandRatio(splitPanel, 1);

        content.addComponent(buildButtonFooter(null, new Button[] { buildCloseButton() }));

    }

    public ComponentFlowVersion getComponentFlowVersion() {
        return componentFlowVersion;
    }

    public void show(ComponentFlowVersion componentFlowVersion) {
        this.componentFlowVersion = componentFlowVersion;

        setCaption("Edit Flow - Name: "
                + componentFlowVersion.getComponentFlow().getData().getName() + ", Version: "
                + componentFlowVersion.getVersion());

        populateComponentPalette();

        this.componentSettingsSheet.show(componentFactory, connectionFactory, configurationService,
                componentFlowVersion, this);

        redrawFlow();

        tabs.setSelectedTab(palleteTab);

        showAtSize(.8);

    }

    @Override
    public void componentSettingsChanges(ComponentFlowNode node, boolean deleted) {
        redrawFlow();
    }

    protected VerticalLayout buildPalette() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        layout.setSpacing(true);

        TextField tf = new TextField();
        tf.setInputPrompt("Search Components");
        tf.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        tf.setIcon(FontAwesome.SEARCH);
        layout.addComponent(tf);

        componentAccordian = new Accordion();
        componentAccordian.setSizeFull();
        layout.addComponent(componentAccordian);
        layout.setExpandRatio(componentAccordian, 1);
        return layout;

    }

    protected void populateComponentPalette() {
        componentAccordian.removeAllComponents();
        Map<ComponentCategory, List<String>> componentTypesByCategory = componentFactory
                .getComponentTypes();
        for (ComponentCategory category : componentTypesByCategory.keySet()) {
            List<String> componentTypes = componentTypesByCategory.get(category);
            VerticalLayout componentLayout = new VerticalLayout();
            componentAccordian.addTab(componentLayout, category.name() + "S");
            if (componentTypes != null) {
                for (String componentType : componentTypes) {
                    Button button = new Button(componentType);
                    button.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
                    button.addStyleName("leftAligned");
                    button.setWidth(100, Unit.PERCENTAGE);
                    button.addClickListener(new AddComponentClickListener(componentType));
                    componentLayout.addComponent(button);
                }
            }
        }
    }

    protected void redrawFlow() {
        if (diagram != null) {
            flowLayout.removeComponent(diagram);
        }

        diagram = new Diagram();
        diagram.addListener(new DiagramChangedListener());
        flowLayout.addComponent(diagram);
        flowLayout.setExpandRatio(diagram, 1);

        List<ComponentFlowNodeLink> links = componentFlowVersion.getComponentFlowNodeLinks();

        List<ComponentFlowNode> flowNodes = componentFlowVersion.getComponentFlowNodes();
        for (ComponentFlowNode flowNode : flowNodes) {
            Node node = new Node();
            String name = flowNode.getComponentVersion().getData().getName();
            String type = flowNode.getComponentVersion().getComponent().getData().getType();
            node.setText(name + "<br><i>" + type + "</i>");
            node.setId(flowNode.getData().getId());
            node.setX(flowNode.getData().getX());
            node.setY(flowNode.getData().getY());
            diagram.addNode(node);

            for (ComponentFlowNodeLink link : links) {
                if (link.getData().getSourceNodeId().equals(node.getId())) {
                    node.getTargetNodeIds().add(link.getData().getTargetNodeId());
                }
            }

        }

    }

    private int countComponentsOfType(String type) {
        int count = 0;
        List<ComponentFlowNode> nodes = componentFlowVersion.getComponentFlowNodes();
        for (ComponentFlowNode componentFlowNode : nodes) {
            if (componentFlowNode.getComponentVersion().getComponent().getData().getType()
                    .equals(type)) {
                count++;
            }
        }
        return count;
    }

    class DiagramChangedListener implements Listener {
        private static final long serialVersionUID = 1L;

        @Override
        public void componentEvent(Event e) {
            if (e instanceof NodeSelectedEvent) {
                NodeSelectedEvent event = (NodeSelectedEvent) e;
                Node node = event.getNode();
                ComponentFlowNode flowNode = componentFlowVersion.findComponentFlowNodeWithId(node
                        .getId());
                componentSettingsSheet.refresh(flowNode);
                tabs.setSelectedTab(propertiesTab);

            } else if (e instanceof NodeMovedEvent) {
                NodeMovedEvent event = (NodeMovedEvent) e;
                Node node = event.getNode();
                ComponentFlowNode flowNode = componentFlowVersion.findComponentFlowNodeWithId(node
                        .getId());
                if (flowNode != null) {
                    flowNode.getData().setX(node.getX());
                    flowNode.getData().setY(node.getY());
                }
                configurationService.save(componentFlowVersion);

            } else if (e instanceof ConnectionEvent) {
                ConnectionEvent event = (ConnectionEvent) e;
                if (!event.isRemoved()) {
                    componentFlowVersion.getComponentFlowNodeLinks().add(
                            new ComponentFlowNodeLink(event.getSourceNodeId(), event
                                    .getTargetNodeId()));
                    configurationService.save(componentFlowVersion);
                } else {
                    ComponentFlowNodeLink link = componentFlowVersion.removeComponentFlowNodeLink(
                            event.getSourceNodeId(), event.getTargetNodeId());
                    if (link != null) {
                        configurationService.delete(link);
                    }

                }
            }
        }
    }

    class AddComponentClickListener implements ClickListener {

        private static final long serialVersionUID = 1L;

        String type;

        public AddComponentClickListener(String type) {
            this.type = type;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            Component component = new Component(new ComponentData(type, false));

            ComponentVersion componentVersion = new ComponentVersion(component, null,
                    new ComponentVersionData());

            componentVersion.setName(type + " " + (countComponentsOfType(type) + 1));

            ComponentFlowNodeData nodeData = new ComponentFlowNodeData(componentVersion.getData()
                    .getId(), componentFlowVersion.getData().getId());

            ComponentFlowNode componentFlowNode = new ComponentFlowNode(componentVersion, nodeData);
            componentFlowVersion.getComponentFlowNodes().add(componentFlowNode);

            configurationService.save(componentFlowNode);

            redrawFlow();

            componentSettingsSheet.refresh(componentFlowNode);

            tabs.setSelectedTab(componentSettingsSheet);
        }
    }

}