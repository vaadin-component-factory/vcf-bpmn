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

import java.util.List;

import org.vaadin.addons.componentfactory.bpmn.BpmnModeler;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/** A custom bpmn-js module, registered in JavaScript and enabled from Java. */
@SuppressWarnings("serial")
@JsModule("./demo-modules/task-badges.js")
@Route(value = "custom-modules", layout = MainLayout.class)
public class CustomModulesDemoView extends VerticalLayout {

    public CustomModulesDemoView() {
        setSizeFull();

        BpmnModeler modeler = new BpmnModeler(false, List.of("taskBadges"));
        modeler.setSizeFull();
        modeler.setDiagramXml(DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM));

        Button toggle = new Button("Switch to read-only");
        toggle.addClickListener(e -> {
            modeler.setReadOnly(!modeler.isReadOnly());
            toggle.setText(modeler.isReadOnly() ? "Switch to editing"
                    : "Switch to read-only");
        });

        add(new H2("Custom modules"), description(),
                new HorizontalLayout(toggle), modeler);
        setFlexGrow(1, modeler);
    }

    private static DemoDescription description() {
        return new DemoDescription(
                "demo-modules/task-badges.js registers a bpmn-js module as "
                        + "taskBadges, and this view enables it with new "
                        + "BpmnModeler(false, List.of(\"taskBadges\")). The "
                        + "module adds a badge with the kind of task above "
                        + "every task, and makes the editor wait half a second "
                        + "for it to be ready.",
                new DemoDescription.Step(
                        "Open the page.",
                        "After a short wait the diagram shows, with a \"User\" "
                                + "badge on \"Review order\" and a \"Service\" "
                                + "badge on \"Charge payment\"."),
                new DemoDescription.Step(
                        "Append a task to \"Order received\" from the context "
                                + "pad, then change its type with the wrench.",
                        "The new task gets a badge too, and a new one when its "
                                + "type changes."),
                new DemoDescription.Step(
                        "Click \"Switch to read-only\", then back to editing.",
                        "The badges disappear in read-only mode, where custom "
                                + "modules are not loaded, and come back when "
                                + "editing."));
    }
}
