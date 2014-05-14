var geomFilters = [
    "INTERSECTS",
    "DISJOINT",
    "CROSSES",
    "WITHIN",
    "OVERLAPS"
];

var filters = [
    "DWITHIN(geometry, POINT(-77.04 38.85), .5, meters)",
    "BEYOND(geometry, POINT(-77.04 38.85), .5, meters)",
    "PERSONS > 15000000",
    "PERSONS BETWEEN 1000000 AND 3000000",
    "STATE_NAME LIKE 'N%'",
    "MALE > FEMALE",
    "UNEMPLOY / (EMPLOYED + UNEMPLOY) > 0.07",
    "IN (12,13)",
    "STATE_NAME IN ('New York', 'California', 'Montana', 'Texas')",
    "STATE_NAME = 'New York' OR STATE_NAME = 'Montana'",
    "STATE_NAME = 'Maryland' AND STATE_ABBR = 'MD'",
    "STATE_NAME <> 'Maryland'"
];

var feat, activeGeomFilter;

function status(msg) {
    $("#status").html(msg || "");
}

function updateGeom() {
    if (feat == null) {
        status("Draw a geometry");
    } else if (activeGeomFilter == null) {
        status("Select a geometry filter");
    } else {
        var params = {cql_filter:activeGeomFilter.textContent+"(geometry,"+feat.geometry+")"};
        layer.mergeNewParams(params);
    }
}

function update(what) {
    layer.mergeNewParams({cql_filter:what.textContent});
}

function select(what) {
    $(what).parent().children().removeClass("selected");
    $(what).addClass("selected");
}

function customFilter() {
    $("#filters").children().removeClass("selected");
    layer.mergeNewParams({cql_filter:$("#filter input").val()});
}

function init() {
    map = new OpenLayers.Map({
        div: "map"
    });

    map.addLayer(new OpenLayers.Layer.WMS("OSM 4326", "http://maps.opengeo.org/geowebcache/service/wms",
            {layers: "openstreetmap", crs: "EPSG:4326", format: "image/png"}));
    map.addLayer(layer = new OpenLayers.Layer.WMS("states", "/wms",
            {layers: "states", crs: "EPSG:4326", format: "image/png", version:"1.3.0"}, {isBaseLayer: false, singleTile:true}));
            map.addLayer(vLayer = new OpenLayers.Layer.Vector("vectors"));
    layer.events.register("loadstart", null, function() {
        status("Loading...");
    });
    layer.events.register("loadend", null, function(ev) {
        var err = $(ev.element).find(".olImageLoadError");
        if (err.length > 0) {
            $.get(err.eq(0).attr('src'), {}, function(doc) {
                status($(doc).find("ServiceException").text());
            }).fail(function() {
                status("An error occurred, check the device logs");
            });
        } else {
            status();
        }
    });
    map.zoomToExtent([-124.731422, 24.955967, -66.969849, 49.371735]);

    drawControls = {
        point: new OpenLayers.Control.DrawFeature(vLayer,
                OpenLayers.Handler.Point),
        line: new OpenLayers.Control.DrawFeature(vLayer,
                OpenLayers.Handler.Path),
        polygon: new OpenLayers.Control.DrawFeature(vLayer,
                OpenLayers.Handler.Polygon),
        box: new OpenLayers.Control.DrawFeature(vLayer,
                OpenLayers.Handler.RegularPolygon, {
                    handlerOptions: {
                        sides: 4,
                        irregular: true
                    }
                }
        )
    };

    for (var key in drawControls) {
        var control = drawControls[key];
        control.featureAdded = function(f) {
            vLayer.removeAllFeatures();
            vLayer.addFeatures([f]);
            feat = f;
            updateGeom();
        };
        map.addControl(control);
        $("#" + key).click(function() {
            for (var id in drawControls) {
                drawControls[id].deactivate();
            }
            var control = drawControls[this.id];
            if (!control.active) {
                control.activate();
                select(this);
            }
        });
    }
    $("#box").click();

    for (var i=0; i< geomFilters.length; i++) {
        var filter = $("<div>" + geomFilters[i] + "</div>");
        filter.appendTo("#filters");
        filter.click(function() {
            activeGeomFilter = this;
            updateGeom();
            select(this);
        });
    }
    
    $("#filters").append("<hr>");

    for (var i=0; i< filters.length; i++) {
        var filter = $("<div>" + filters[i] + "</div>");
        filter.appendTo("#filters");
        filter.click(function() {
            vLayer.removeAllFeatures();
            feat = null;
            update(this);
            select(this);
        });
    }

    $("#filter button").click(customFilter);
    $("#filter input").keypress(function(ev) {
        if (ev.which == 13) {
            customFilter();
        }
    });

}


