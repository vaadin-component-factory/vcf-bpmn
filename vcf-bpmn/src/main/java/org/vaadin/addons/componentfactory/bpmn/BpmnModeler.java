/*
 * BPMN Add-on
 *
 * Copyright (C) 2026 Vaadin Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.addons.componentfactory.bpmn;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;

import tools.jackson.databind.json.JsonMapper;

/**
 * BPMN 2.0 editor based on <a href="https://bpmn.io/toolkit/bpmn-js/">bpmn-js</a>,
 * with the properties panel on the right and Camunda 7 extensions enabled.
 * In read-only mode it only shows the diagram.
 */
@SuppressWarnings("serial")
@Tag(Tag.DIV)
@NpmPackage(value = "bpmn-js", version = "18.30.1")
@NpmPackage(value = "diagram-js", version = "15.27.1")
@NpmPackage(value = "bpmn-js-properties-panel", version = "5.65.1")
@NpmPackage(value = "@bpmn-io/properties-panel", version = "3.55.0")
@NpmPackage(value = "camunda-bpmn-moddle", version = "8.0.1")
@NpmPackage(value = "camunda-bpmn-js-behaviors", version = "1.18.0")
@NpmPackage(value = "bpmn-js-create-append-anything", version = "2.1.0")
@JsModule("./src/vcf-bpmn-connector.js")
@CssImport("bpmn-js/dist/assets/diagram-js.css")
@CssImport("bpmn-js/dist/assets/bpmn-js.css")
@CssImport("bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css")
@CssImport("@bpmn-io/properties-panel/dist/assets/properties-panel.css")
@CssImport("./styles/vcf-bpmn.css")
public class BpmnModeler extends Component implements HasSize {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    /** The diagram as the browser shows it, edits included. */
    private String diagramXml;

    /** The last diagram the browser imported, to fall back to on an error. */
    private String importedXml;

    /** The diagram whose import is on its way, if any. */
    private String pendingXml;

    private boolean readOnly;

    private final List<String> customModules;

    /** The CSS classes applied to each element, by element id. */
    private final Map<String, Set<String>> elementClassNames = new LinkedHashMap<>();

    /** Creates an editable modeler. */
    public BpmnModeler() {
        this(false);
    }

    /**
     * Creates a modeler, editable or read-only.
     *
     * @param readOnly {@code true} to only view the diagram: pan, zoom and
     *                 select, without palette or properties panel
     */
    public BpmnModeler(boolean readOnly) {
        this(readOnly, List.of());
    }

    /**
     * Creates a modeler with custom bpmn-js modules, loaded only while
     * editing. Each one is registered under its name in a JavaScript file of
     * the application, imported in the view with {@code @JsModule}:
     *
     * <pre>
     * window.Vaadin.Flow.bpmnModules.myModule = {
     *   module: myModule,       // a bpmn-js module, or an array of them
     *   ready: aPromise,        // optional: the editor waits for it
     *   moddleExtensions: {}    // optional: descriptors for custom XML attributes
     * };
     * </pre>
     *
     * Create {@code window.Vaadin.Flow.bpmnModules} first if it does not
     * exist; one file can register several modules. A name that is not
     * registered is reported in the browser console and left out.
     *
     * @param readOnly      {@code true} to only view the diagram
     * @param customModules names of the registered modules to load
     */
    public BpmnModeler(boolean readOnly, List<String> customModules) {
        this.readOnly = readOnly;
        this.customModules = List.copyOf(customModules);
        addClassName("vcf-bpmn");
        registerDiagramListeners();
    }

    /**
     * Builds the editor on every attach, not only the first: a refresh with
     * {@code @PreserveOnRefresh} also reports an initial attach.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
        if (diagramXml != null) {
            // Events only for an import the application asked for.
            callConnector("importXml", diagramXml, pendingXml != null);
        }
        elementClassNames.forEach((elementId, classNames) -> classNames
                .forEach(className -> callConnector("addMarker", elementId,
                        className)));
    }

    /**
     * Loads a BPMN 2.0 diagram and clears the element CSS classes. The outcome
     * arrives as a {@link DiagramImportedEvent} or a
     * {@link DiagramImportErrorEvent}.
     *
     * @param bpmnXml the diagram to load
     */
    public void setDiagramXml(String bpmnXml) {
        diagramXml = bpmnXml;
        pendingXml = bpmnXml;
        boolean hadClassNames = !elementClassNames.isEmpty();
        elementClassNames.clear();
        // When detached, onAttach imports it.
        if (isAttached()) {
            if (hadClassNames) {
                callConnector("clearMarkers");
            }
            callConnector("importXml", bpmnXml, true);
        }
    }

    /**
     * Switches between editing and viewing. The diagram and any pending edit
     * are kept.
     *
     * @param readOnly {@code true} to only view the diagram
     */
    public void setReadOnly(boolean readOnly) {
        if (this.readOnly == readOnly) {
            return;
        }
        this.readOnly = readOnly;
        // When detached, onAttach builds the editor in the new mode.
        if (isAttached()) {
            callConnector("setReadOnly", readOnly);
        }
    }

    /** @return {@code true} if the diagram can only be viewed */
    public boolean isReadOnly() {
        return readOnly;
    }

    /** @return the names of the custom modules given at construction time */
    public List<String> getCustomModules() {
        return customModules;
    }

    /**
     * Adds a CSS class to the SVG of a diagram element; unknown ids are
     * ignored. Kept across mode switches and re-attaches, cleared by
     * {@link #setDiagramXml}, so add classes after setting the diagram.
     *
     * @param elementId the id of the element, such as Task_1
     * @param className one CSS class name, without spaces
     */
    public void addElementClassName(String elementId, String className) {
        Objects.requireNonNull(elementId, "elementId");
        checkClassName(className);
        boolean added = elementClassNames
                .computeIfAbsent(elementId, id -> new LinkedHashSet<>())
                .add(className);
        if (added && isAttached()) {
            callConnector("addMarker", elementId, className);
        }
    }

    /**
     * Removes a CSS class added with {@link #addElementClassName}.
     *
     * @param elementId the id of the element
     * @param className the CSS class name
     */
    public void removeElementClassName(String elementId, String className) {
        Set<String> classNames = elementClassNames.get(elementId);
        if (classNames == null || !classNames.remove(className)) {
            return;
        }
        if (classNames.isEmpty()) {
            elementClassNames.remove(elementId);
        }
        if (isAttached()) {
            callConnector("removeMarker", elementId, className);
        }
    }

    /**
     * Returns the current diagram, including the edits made in the browser.
     *
     * @return the diagram XML, or {@code null} if none has been set
     */
    public String getDiagramXml() {
        return diagramXml;
    }

    /** Adds a listener for the edits made in the browser. */
    public Registration addDiagramChangedListener(
            ComponentEventListener<DiagramChangedEvent> listener) {
        return addListener(DiagramChangedEvent.class, listener);
    }

    /** Adds a listener for selection changes, in editing and read-only mode. */
    public Registration addSelectionChangedListener(
            ComponentEventListener<SelectionChangedEvent> listener) {
        return addListener(SelectionChangedEvent.class, listener);
    }

    /** Adds a listener for diagrams imported by {@link #setDiagramXml}. */
    public Registration addDiagramImportedListener(
            ComponentEventListener<DiagramImportedEvent> listener) {
        return addListener(DiagramImportedEvent.class, listener);
    }

    /** Adds a listener for diagrams {@link #setDiagramXml} failed to import. */
    public Registration addDiagramImportErrorListener(
            ComponentEventListener<DiagramImportErrorEvent> listener) {
        return addListener(DiagramImportErrorEvent.class, listener);
    }

    /**
     * Keeps the server-side copy of the diagram in sync. Registered before any
     * application listener, so those already see the updated copy.
     */
    private void registerDiagramListeners() {
        addListener(DiagramImportedEvent.class, event -> {
            importedXml = pendingXml;
            pendingXml = null;
        });
        addListener(DiagramImportErrorEvent.class, event -> {
            diagramXml = importedXml;
            pendingXml = null;
        });
        addListener(DiagramChangedEvent.class, event -> {
            diagramXml = event.xml();
            importedXml = diagramXml;
        });
    }

    private static void checkClassName(String className) {
        Objects.requireNonNull(className, "className");
        if (className.isBlank()
                || className.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(
                    "Not a single CSS class name: \"" + className + "\"");
        }
    }

    /** Through {@code executeJs}, since this is what installs $connector. */
    private void initConnector() {
        runBeforeClientResponse(ui -> getElement().executeJs(
                "window.Vaadin.Flow.bpmnConnector.initLazy(this, $0, $1)",
                readOnly, MAPPER.writeValueAsString(customModules)));
    }

    private void callConnector(String function, Object... arguments) {
        runBeforeClientResponse(ui -> getElement()
                .callJsFunction("$connector." + function, arguments));
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }
}
