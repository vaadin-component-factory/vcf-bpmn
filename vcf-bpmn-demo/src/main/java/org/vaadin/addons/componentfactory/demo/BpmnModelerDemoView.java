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
package org.vaadin.addons.componentfactory.demo;

import org.vaadin.addons.componentfactory.bpmn.BpmnModeler;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.router.Route;

/** Loads a diagram from the server and reads the edited one back. */
@SuppressWarnings("serial")
@Route(value = "", layout = MainLayout.class)
public class BpmnModelerDemoView extends VerticalLayout {

    public BpmnModelerDemoView() {
        setSizeFull();

        BpmnModeler modeler = new BpmnModeler();
        modeler.setSizeFull();

        Button resetToSample = new Button("Reset to sample diagram",
                e -> modeler.setDiagramXml(
                        DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM)));
        Button loadInvalid = new Button("Load invalid XML",
                e -> modeler.setDiagramXml("<not-bpmn>"));
        Button showXml = new Button("Show XML", e -> DemoSupport
                .showXml("Diagram XML", modeler.getDiagramXml()));
        // Removing and adding back rebuilds the editor; edits must survive.
        Button removeEditor = new Button("Remove editor");
        Button addEditorBack = new Button("Add editor back");

        // getParent, not isAttached: this view is not attached yet when built.
        SerializableRunnable updateButtons = () -> {
            boolean editorShown = modeler.getParent().isPresent();
            boolean diagramShown = editorShown
                    && modeler.getDiagramXml() != null;
            resetToSample.setEnabled(editorShown);
            showXml.setEnabled(diagramShown);
            loadInvalid.setEnabled(diagramShown);
            removeEditor.setEnabled(editorShown);
            addEditorBack.setEnabled(!editorShown);
        };

        removeEditor.addClickListener(e -> {
            remove(modeler);
            updateButtons.run();
        });
        addEditorBack.addClickListener(e -> {
            add(modeler);
            setFlexGrow(1, modeler);
            updateButtons.run();
        });

        DemoSupport.showNotifications(modeler);
        modeler.addDiagramImportedListener(event -> updateButtons.run());
        modeler.addDiagramImportErrorListener(event -> updateButtons.run());

        modeler.setDiagramXml(DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM));

        HorizontalLayout toolbar = new HorizontalLayout(resetToSample,
                loadInvalid, showXml, removeEditor, addEditorBack);
        toolbar.setWrap(true);

        add(new H2("Basic modeler"), description(), toolbar, modeler);
        setFlexGrow(1, modeler);
        updateButtons.run();
    }

    private static DemoDescription description() {
        return new DemoDescription(
                "The modeler with the Camunda 7 properties panel. The sample "
                        + "diagram is loaded from the server when the page "
                        + "opens, and the edited diagram is read back from it. "
                        + "Every listener of the modeler shows a notification: "
                        + "\"Diagram imported\" and \"Import failed\" for "
                        + "loading, \"Diagram changed\" for each edit, "
                        + "\"Selected\" for the selection.",
                new DemoDescription.Step(
                        "Open the page.",
                        "The diagram shows, centered in the editor, and a "
                                + "\"Diagram imported\" notification appears. "
                                + "Loading a diagram is not an edit, so no "
                                + "\"Diagram changed\" notification appears."),
                new DemoDescription.Step(
                        "Click \"Review order\", then Shift+click \"Charge "
                                + "payment\", then click an empty spot of the "
                                + "canvas.",
                        "\"Selected Task_Review (bpmn:UserTask)\", then "
                                + "\"Selected 2 elements, first Task_Review "
                                + "(bpmn:UserTask)\", then \"Selection "
                                + "cleared\". Clicking a task's name selects "
                                + "the task itself."),
                new DemoDescription.Step(
                        "Select \"Review order\" and change its name, or its "
                                + "assignee under User assignment, in the "
                                + "properties panel. Then click \"Show XML\" "
                                + "right away.",
                        "A \"Diagram changed\" notification appears, with the "
                                + "size of the edited XML, and the XML in the "
                                + "dialog already has the new value."),
                new DemoDescription.Step(
                        "Drag an element, then undo with Ctrl+Z.",
                        "One \"Diagram changed\" notification for the drag, "
                                + "however long it took, and one for the "
                                + "undo."),
                new DemoDescription.Step(
                        "Click \"Remove editor\", then \"Add editor back\".",
                        "While the editor is removed, only \"Add editor back\" "
                                + "is enabled. The editor comes back with the "
                                + "diagram as you left it, edits included, "
                                + "and no \"Diagram imported\" notification "
                                + "appears."),
                new DemoDescription.Step(
                        "Click \"Load invalid XML\", then \"Show XML\".",
                        "An \"Import failed\" notification appears, the "
                                + "editor keeps the previous diagram, and the "
                                + "XML is still the edited one."),
                new DemoDescription.Step(
                        "Click \"Reset to sample diagram\".",
                        "Your edits are discarded and the original sample "
                                + "diagram is loaded again, with a "
                                + "\"Diagram imported\" notification."));
    }
}
