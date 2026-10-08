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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Fired after the diagram is edited in the browser. Listeners get the edited
 * diagram from {@link BpmnModeler#getDiagramXml()}.
 */
@SuppressWarnings("serial")
@DomEvent("vcf-bpmn-diagram-changed")
public class DiagramChangedEvent extends ComponentEvent<BpmnModeler> {

    private final String xml;

    public DiagramChangedEvent(BpmnModeler source, boolean fromClient,
            @EventData("event.detail.xml") String xml) {
        super(source, fromClient);
        this.xml = xml;
    }

    /** The diagram sent by the browser; read by {@link BpmnModeler} only. */
    String xml() {
        return xml;
    }
}
