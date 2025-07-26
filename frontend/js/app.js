// Minimal stub for OSRS-Script-Map app.js. For full logic, see https://github.com/Paradoxis/OSRS-Script-Map/blob/master/js/app.js
require.config({
    shim : {
        "bootstrap" : { "deps" :['jquery'] }
    },
    paths: {
        jquery: "external/jquery-2.1.4",
        leaflet: "external/leaflet-src",
        bootstrap: "external/bootstrap.min",
        domReady: "external/domReady"
    }
});

requirejs(['main']);
