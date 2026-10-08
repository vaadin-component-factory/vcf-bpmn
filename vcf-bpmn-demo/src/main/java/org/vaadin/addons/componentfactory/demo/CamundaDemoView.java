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

/** Camunda 7 settings read from and written to the XML without loss. */
@SuppressWarnings("serial")
@Route(value = "camunda", layout = MainLayout.class)
public class CamundaDemoView extends VerticalLayout {

    public CamundaDemoView() {
        setSizeFull();

        BpmnModeler modeler = new BpmnModeler();
        modeler.setSizeFull();
        DemoSupport.showNotifications(modeler);
        modeler.setDiagramXml(DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM));

        Button showXml = new Button("Show XML", e -> DemoSupport
                .showXml("Diagram XML", modeler.getDiagramXml()));
        Button reset = new Button("Reset to sample diagram",
                e -> modeler.setDiagramXml(
                        DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM)));
        HorizontalLayout toolbar = new HorizontalLayout(showXml, reset);

        add(new H2("Camunda 7 properties"), description(), toolbar, modeler);
        setFlexGrow(1, modeler);
    }

    private static DemoDescription description() {
        return new DemoDescription(
                "The properties panel shows the Camunda 7 settings of the "
                        + "selected element. The sample diagram already has "
                        + "some; the steps add more of the kinds applications "
                        + "use, and \"Show XML\" checks that they are in the "
                        + "diagram as Camunda expects them.",
                new DemoDescription.Step(
                        "Select \"Review order\" and open User assignment. "
                                + "Then select \"Charge payment\" and open "
                                + "Implementation.",
                        "The values the diagram was loaded with: assignee "
                                + "demo and candidate group sales; Java class "
                                + "com.example.ChargePaymentDelegate."),
                new DemoDescription.Step(
                        "Click an empty spot of the canvas to select the "
                                + "process. Under Execution listeners click "
                                + "\"+\", keep \"start\" and \"Java class\", "
                                + "type com.example.OrderStartListener as the "
                                + "Java class and press Tab. Click \"Show XML\".",
                        "The process has a camunda:executionListener with "
                                + "event start and that class."),
                new DemoDescription.Step(
                        "Select \"Review order\". Under Extension properties "
                                + "click \"+\" and enter name priority and "
                                + "value high. Click \"Show XML\".",
                        "The user task has camunda:properties with a "
                                + "camunda:property named priority, value "
                                + "high. Custom modules that store their own "
                                + "settings commonly do it this way."),
                new DemoDescription.Step(
                        "Select \"Charge payment\". Under Inputs click \"+\", "
                                + "name it amount and set the value to "
                                + "${order.total}. Click \"Show XML\".",
                        "The service task has camunda:inputOutput with a "
                                + "camunda:inputParameter named amount."),
                new DemoDescription.Step(
                        "Select \"Charge payment\", click the wrench in the "
                                + "context pad and change it to a User Task. "
                                + "Click \"Show XML\".",
                        "The task is now a bpmn:userTask and no longer has "
                                + "camunda:class, which only applies to "
                                + "service tasks."),
                new DemoDescription.Step(
                        "Click \"Reset to sample diagram\" and select "
                                + "\"Review order\" again.",
                        "The added settings are gone and the loaded ones are "
                                + "back, as in the first step."));
    }
}
