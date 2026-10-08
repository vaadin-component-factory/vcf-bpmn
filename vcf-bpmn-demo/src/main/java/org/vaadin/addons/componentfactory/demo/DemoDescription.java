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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

/** What a demo page shows, and the steps to try with their expected result. */
@SuppressWarnings("serial")
public class DemoDescription extends Div {

    /** One thing to try on the page, and what should happen. */
    public record Step(String action, String expected) {
    }

    /**
     * @param summary what the page demonstrates
     * @param steps   things to try on the page, in order
     */
    public DemoDescription(String summary, Step... steps) {
        getStyle().set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        Paragraph intro = new Paragraph(summary);
        intro.getStyle().set("margin", "0");
        add(intro);

        if (steps.length > 0) {
            OrderedList list = new OrderedList();
            list.getStyle().set("margin", "var(--lumo-space-xs) 0 0 0")
                    .set("padding-left", "1.4em");
            for (Step step : steps) {
                Span expected = new Span(" Expected: " + step.expected());
                expected.getStyle().set("color",
                        "var(--lumo-secondary-text-color)");
                list.add(new ListItem(new Span(step.action()), expected));
            }
            add(list);
        }
    }
}
