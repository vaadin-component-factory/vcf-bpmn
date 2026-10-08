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

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

/** The frame of the demo pages, with the navigation between them. */
@SuppressWarnings("serial")
public class MainLayout extends AppLayout {

    public MainLayout() {
        H3 title = new H3("BPMN Modeler");
        title.getStyle().set("margin", "var(--lumo-space-m)");

        VerticalLayout menu = new VerticalLayout(
                new RouterLink("Basic modeler", BpmnModelerDemoView.class),
                new RouterLink("Read-only mode", ReadOnlyDemoView.class),
                new RouterLink("CSS classes", ElementClassNamesDemoView.class),
                new RouterLink("Custom modules", CustomModulesDemoView.class),
                new RouterLink("Modeler in a dialog", DialogDemoView.class),
                new RouterLink("Preserve on refresh",
                        PreserveOnRefreshDemoView.class),
                new RouterLink("Camunda 7 properties", CamundaDemoView.class));
        menu.setSpacing(false);

        addToDrawer(title, menu);
        addToNavbar(new DrawerToggle());
    }
}
