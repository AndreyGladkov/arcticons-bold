package com.donnnno.arcticons.helper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoldIconRendererTest {

    @Test
    void setsInheritedStrokeWidthOnRootElement() {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 48 48\"><path d=\"M1,1\"/></svg>";

        assertEquals(
                "<svg stroke-width=\"2.0\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 48 48\"><path d=\"M1,1\"/></svg>",
                BoldIconRenderer.withStrokeWidth(svg, "2.0"));
    }

    @Test
    void replacesExplicitUnitStrokeWidthInCssAndAttributes() {
        String svg = "<svg><style>.a{stroke-width:1;}.b{stroke-width: 1}</style><path stroke-width=\"1\"/></svg>";

        assertEquals(
                "<svg stroke-width=\"2.0\"><style>.a{stroke-width:2.0;}.b{stroke-width: 2.0}</style><path stroke-width=\"2.0\"/></svg>",
                BoldIconRenderer.withStrokeWidth(svg, "2.0"));
    }

    @Test
    void keepsZeroAndNonUnitStrokeWidths() {
        String svg = "<svg><style>.f{stroke-width:0;}.g{stroke-width:1.5;}.h{stroke-width:10;}</style></svg>";

        assertEquals(
                "<svg stroke-width=\"2.0\"><style>.f{stroke-width:0;}.g{stroke-width:1.5;}.h{stroke-width:10;}</style></svg>",
                BoldIconRenderer.withStrokeWidth(svg, "2.0"));
    }
}
