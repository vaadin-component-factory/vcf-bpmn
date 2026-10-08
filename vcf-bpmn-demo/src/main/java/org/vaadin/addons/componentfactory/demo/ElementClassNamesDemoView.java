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
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/** CSS classes added to diagram elements, styled by the application. */
@SuppressWarnings("serial")
@CssImport("./styles/bpmn-status.css")
@Route(value = "css-classes", layout = MainLayout.class)
public class ElementClassNamesDemoView extends VerticalLayout {

    private static final String FAILED = "bpmn-status-failed";
    private static final String DONE = "bpmn-status-done";

    public ElementClassNamesDemoView() {
        setSizeFull();

        BpmnModeler modeler = new BpmnModeler();
        modeler.setSizeFull();
        modeler.setDiagramXml(DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM));

        Button failed = new Button("Mark Review order as failed",
                e -> modeler.addElementClassName("Task_Review", FAILED));
        Button done = new Button("Mark Charge payment as done",
                e -> modeler.addElementClassName("Task_Charge", DONE));
        Button clear = new Button("Clear marks", e -> {
            modeler.removeElementClassName("Task_Review", FAILED);
            modeler.removeElementClassName("Task_Charge", DONE);
        });
        Button toggle = new Button("Switch to read-only");
        toggle.addClickListener(e -> {
            modeler.setReadOnly(!modeler.isReadOnly());
            toggle.setText(modeler.isReadOnly() ? "Switch to editing"
                    : "Switch to read-only");
        });
        Button reset = new Button("Reset to sample diagram",
                e -> modeler.setDiagramXml(
                        DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM)));

        HorizontalLayout toolbar = new HorizontalLayout(failed, done, clear,
                toggle, reset);
        toolbar.setWrap(true);

        add(new H2("CSS classes"), description(), toolbar, modeler);
        setFlexGrow(1, modeler);
    }

    private static DemoDescription description() {
        return new DemoDescription(
                "addElementClassName adds a CSS class to a diagram element, and "
                        + "the application's own stylesheet styles it. This "
                        + "page colors bpmn-status-failed red and "
                        + "bpmn-status-done green.",
                new DemoDescription.Step(
                        "Click \"Mark Review order as failed\" and \"Mark "
                                + "Charge payment as done\".",
                        "\"Review order\" turns red and \"Charge payment\" "
                                + "green."),
                new DemoDescription.Step(
                        "Click \"Switch to read-only\" and back to editing.",
                        "Both tasks keep their colors: the classes stay on the "
                                + "elements across mode switches."),
                new DemoDescription.Step(
                        "Click \"Clear marks\". Mark both tasks again, then "
                                + "click \"Reset to sample diagram\".",
                        "\"Clear marks\" removes both colors. Loading a "
                                + "diagram also removes them: setDiagramXml "
                                + "clears the classes of the previous diagram."));
    }
}
