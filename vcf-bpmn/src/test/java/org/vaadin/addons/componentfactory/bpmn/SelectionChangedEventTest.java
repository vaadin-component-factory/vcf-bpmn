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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.vaadin.addons.componentfactory.bpmn.SelectionChangedEvent.SelectedElement;

import com.vaadin.flow.component.ComponentUtil;

public class SelectionChangedEventTest {

    private final BpmnModeler modeler = new BpmnModeler();

    @Test
    public void singleSelection() {
        SelectionChangedEvent event = event(
                "[{\"id\":\"Task_Review\",\"type\":\"bpmn:UserTask\"}]");

        assertEquals("Task_Review", event.getElementId());
        assertEquals("bpmn:UserTask", event.getElementType());
        assertEquals(List.of(new SelectedElement("Task_Review", "bpmn:UserTask")),
                event.getSelectedElements());
    }

    @Test
    public void multipleSelection_firstElementIsTheSingleOne() {
        SelectionChangedEvent event = event(
                "[{\"id\":\"Task_Review\",\"type\":\"bpmn:UserTask\"},"
                        + "{\"id\":\"Task_Charge\",\"type\":\"bpmn:ServiceTask\"}]");

        assertEquals("Task_Review", event.getElementId());
        assertEquals("bpmn:UserTask", event.getElementType());
        assertEquals(List.of(new SelectedElement("Task_Review", "bpmn:UserTask"),
                new SelectedElement("Task_Charge", "bpmn:ServiceTask")),
                event.getSelectedElements());
    }

    @Test
    public void nothingSelected() {
        SelectionChangedEvent event = event("[]");

        assertNull(event.getElementId());
        assertNull(event.getElementType());
        assertTrue(event.getSelectedElements().isEmpty());
    }

    @Test
    public void listenerIsNotified() {
        AtomicReference<String> selected = new AtomicReference<>();
        modeler.addSelectionChangedListener(e -> selected.set(e.getElementId()));

        ComponentUtil.fireEvent(modeler, event(
                "[{\"id\":\"Task_Charge\",\"type\":\"bpmn:ServiceTask\"}]"));

        assertEquals("Task_Charge", selected.get());
    }

    private SelectionChangedEvent event(String elementsJson) {
        return new SelectionChangedEvent(modeler, true, elementsJson);
    }
}
