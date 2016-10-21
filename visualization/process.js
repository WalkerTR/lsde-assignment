#!/usr/bin/env node

var fs = require('fs');
var Smooth = require('./Smooth.js');

var std_routes = require('./' + process.argv[2]);
var diverging_routes = require('./' + process.argv[3]);


/*
std_routes
    .filter(function (route) {
        return route.route.length >= 2;
    })
    .map(function (route) {
        route.route = route.route.map(function (point) {
            return [point.lat, point.lon];
        })
        return route;
    })
    .map(function (route) {
        var s = Smooth.Smooth(route.route, {
            method: 'lanczos',
            sincFilterSize: 40,
            sincWindow: function(x) { return Math.abs(x); }
        });
        var path = [];
        for (i = 0; i < route.route.length; i++) {
            for (j = 0; j < 8; j++) {
                path.push(s(i + (j/8)));
            }
        }
        route.route = path;
        return route;
    })
    .map(function (route) {
        route.route = route.route.map(function (point) {
            return {lat: point[0], lon: point[1]};
        })
        return route;
    });
*/
/*
std_routes.map(function (route) {
    route.route.map(function (point) {
        return {
            lat: point.lat/10,
            lon: point.lon/10
        }
    });
});
diverging_routes.map(function (routes) {
    routes.routes.map(function (route) {
        route.map(function (point) {
            return {
                lat: point.lat/10,
                lon: point.lon/10
            }
        });
    });
});
*/



fs.writeFileSync('./std_routes.js', 'var std_routes = ' + JSON.stringify(std_routes));
fs.writeFileSync('./diverging_routes.js', 'var diverging_routes = ' + JSON.stringify(diverging_routes));