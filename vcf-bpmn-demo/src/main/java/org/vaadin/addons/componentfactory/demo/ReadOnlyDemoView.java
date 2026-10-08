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
import com.vaadin.flow.router.Route;

/** The modeler created read-only, and switched between viewing and editing. */
@SuppressWarnings("serial")
@Route(value = "read-only", layout = MainLayout.class)
public class ReadOnlyDemoView extends VerticalLayout {

    public ReadOnlyDemoView() {
        setSizeFull();

        BpmnModeler modeler = new BpmnModeler(true);
        modeler.setSizeFull();
        modeler.setDiagramXml(DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM));

        Button toggle = new Button("Switch to editing");
        toggle.addClickListener(e -> {
            modeler.setReadOnly(!modeler.isReadOnly());
            toggle.setText(modeler.isReadOnly() ? "Switch to editing"
                    : "Switch to read-only");
        });
        Button showXml = new Button("Show XML", e -> DemoSupport
                .showXml("Diagram XML", modeler.getDiagramXml()));

        add(new H2("Read-only mode"), description(),
                new HorizontalLayout(toggle, showXml), modeler);
        setFlexGrow(1, modeler);
    }

    private static DemoDescription description() {
        return new DemoDescription(
                "The modeler is created read-only, with new BpmnModeler(true), "
                        + "and setReadOnly switches it between viewing and "
                        + "editing.",
                new DemoDescription.Step(
                        "Open the page. Drag an empty spot of the canvas, use "
                                + "Ctrl + mouse wheel, then click \"Review "
                                + "order\".",
                        "No palette and no properties panel. Dragging moves "
                                + "the whole diagram and Ctrl + wheel zooms "
                                + "it; clicking an element selects it, but "
                                + "nothing can be edited."),
                new DemoDescription.Step(
                        "Click \"Switch to editing\" and rename \"Review "
                                + "order\". Right away, click \"Switch to "
                                + "read-only\".",
                        "The palette and the panel appear, then disappear "
                                + "again. The diagram keeps the new name, and "
                                + "\"Show XML\" has it too."),
                new DemoDescription.Step(
                        "Switch modes a few times.",
                        "The diagram stays the same."));
    }
}
