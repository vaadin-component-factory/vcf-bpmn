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

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

/** The modeler in a view that keeps its state when the browser is refreshed. */
@SuppressWarnings("serial")
@PreserveOnRefresh
@Route(value = "preserve-on-refresh", layout = MainLayout.class)
public class PreserveOnRefreshDemoView extends VerticalLayout {

    public PreserveOnRefreshDemoView() {
        setSizeFull();

        BpmnModeler modeler = new BpmnModeler();
        modeler.setSizeFull();
        modeler.setDiagramXml(DemoSupport.readResource(DemoSupport.SAMPLE_DIAGRAM));

        add(new H2("Preserve on refresh"), description(), modeler);
        setFlexGrow(1, modeler);
    }

    private static DemoDescription description() {
        return new DemoDescription(
                "This view is annotated with @PreserveOnRefresh: refreshing the "
                        + "browser keeps the same view and modeler instead of "
                        + "creating new ones.",
                new DemoDescription.Step(
                        "Rename \"Review order\" in the properties panel, "
                                + "then refresh the browser (F5).",
                        "The diagram shows again, with the new name."));
    }
}
