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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.List;

import org.vaadin.addons.componentfactory.bpmn.BpmnModeler;
import org.vaadin.addons.componentfactory.bpmn.SelectionChangedEvent;
import org.vaadin.addons.componentfactory.bpmn.SelectionChangedEvent.SelectedElement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** What the demo pages share. */
final class DemoSupport {

    static final String SAMPLE_DIAGRAM = "/diagrams/order-process.bpmn";

    private DemoSupport() {
    }

    /** Shows a notification for every event of the modeler. */
    static void showNotifications(BpmnModeler modeler) {
        modeler.addDiagramImportedListener(event -> Notification
                .show(event.getWarnings().isEmpty() ? "Diagram imported"
                        : "Diagram imported with warnings: "
                                + event.getWarnings()));
        modeler.addDiagramImportErrorListener(event -> Notification
                .show("Import failed: " + event.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR));
        modeler.addDiagramChangedListener(event -> Notification.show(
                "Diagram changed at " + LocalTime.now().withNano(0) + " ("
                        + modeler.getDiagramXml().length()
                        + " characters of XML)",
                2000, Notification.Position.BOTTOM_START));
        modeler.addSelectionChangedListener(event -> Notification.show(
                selectionText(event), 2000, Notification.Position.BOTTOM_START));
    }

    private static String selectionText(SelectionChangedEvent event) {
        List<SelectedElement> selected = event.getSelectedElements();
        if (selected.isEmpty()) {
            return "Selection cleared";
        }
        String first = event.getElementId() + " (" + event.getElementType() + ")";
        return selected.size() == 1 ? "Selected " + first
                : "Selected " + selected.size() + " elements, first " + first;
    }

    static void showXml(String title, String xml) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setWidth("80vw");
        dialog.add(new Pre(xml));
        dialog.getFooter().add(new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    static String readResource(String path) {
        try (InputStream in = DemoSupport.class.getResourceAsStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
