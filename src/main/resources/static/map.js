var osm_online = L.tileLayer('http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href = "https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
});

// var osm_toner = L.tileLayer('Toner/{z}/{x}/{y}.png', {
//     attribution: 'Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL.'
// });

// var osm_terrain = L.tileLayer('Terrain/{z}/{x}/{y}.png', {
//     attribution: 'Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL.'
// });

// var bounds = L.latLngBounds([[48.3, 1.5], [49.2, 3.5]]);

var map = L.map('map', {
    layers: [osm_online],
    // maxBounds: bounds
}).setView([48.856614, 2.3522219], 12);
// map.setMaxZoom(15);
// map.setMinZoom(10);

map.on('zoomend', function () {
    var currentZoom = map.getZoom();
    console.log('Niveau de zoom actuel : ' + currentZoom);
    // Mettez à jour votre interface utilisateur avec le niveau de zoom actuel ici
});

// var baseMaps = {
//     "OSM Online": osm_online,
//     "OSM Toner": osm_toner,
//     "OSM Terrain": osm_terrain
// };
// var layerControl = L.control.layers(baseMaps).addTo(map);

var markersLayer = L.markerClusterGroup();
var itineraryLayer = L.layerGroup();
var departList = [];
var arriveeList = [];

// GET stations list and adding them to the map (to layergroup then layergroup to map)
fetch('http://localhost:8080/stations')
    .then(response => response.json())
    .then(data => {
        data.forEach(station => {
            var lines = station.neighboringLines.join('<br>');
            var marker = L.marker([station.localisation.latitude, station.localisation.longitude])
                .bindPopup(station.name + '<br>' + " Lignes: " + '<br>' + lines); //label for each marker
            markersLayer.addLayer(marker);

            // Adding station names + localisation to datalist of both inputs
            departList.push(station.name + " [" + station.neighboringLines.join('|') + "] (" + station.localisation.latitude + "," + station.localisation.longitude + ")");
            arriveeList.push(station.name + " [" + station.neighboringLines.join('|') + "] (" + station.localisation.latitude + "," + station.localisation.longitude + ")");
        });
        map.addLayer(markersLayer);
    })
    .catch(error => console.error(error));

const departInput = document.getElementById('depart');
const arriveeInput = document.getElementById('arrivee');

const form = document.querySelector('#itinerary_form');
const errorMessage = document.getElementById('error_itinerary');

form.addEventListener('submit', function (event) {
    event.preventDefault();

    const departValue = encodeURIComponent(departInput.value);
    const arriveeValue = encodeURIComponent(arriveeInput.value);
    const url = `/shortest-way?depart=${departValue}&arrivee=${arriveeValue}`;

    console.log(url);
    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (data.length == 0) {
                errorMessage.style.display = "block";
                errorMessage.textContent = "Aucun chemin trouvé suivant les stations spécifiées.";
            } else {
                errorMessage.style.display = "none";
                itineraryLayer.clearLayers();
                var current_station = null;
                const latLngs = [];
                data.forEach(station => {
                    var name = station.name;
                    var latitude = station.localisation.latitude;
                    var longitude = station.localisation.longitude;
                    latLngs.push([latitude, longitude]);
                    var lines = station.neighboringLines.join('<br>');
                    var marker = L.marker([latitude, longitude])
                        .bindPopup(name + '<br>' + " Lignes: " + '<br>' + lines);
                    if (current_station != null) {
                        var polyline = L.polyline([current_station.getLatLng(), marker.getLatLng()], { color: 'blue' });
                        itineraryLayer.addLayer(polyline);
                    }
                    current_station = marker;
                    itineraryLayer.addLayer(marker);
                });
                map.removeLayer(markersLayer);
                map.addLayer(itineraryLayer);
                map.fitBounds(latLngs);
            }
        })
        .catch(error => {
            console.error(error);
            errorMessage.style.display = "block";
            errorMessage.textContent = "Tout les champs sont obligatoire. Suivez la syntaxe imposée dans les suggestions!";
        });
});

function autoComplete(inputElement, datalistElement, optionsList) {
    inputElement.addEventListener('input', function () {
        datalistElement.innerHTML = '';
        const inputValue = this.value;
        // Filtering names in input each time user presses key (accents not handled like terminal mode)
        const suggestions = optionsList.filter(function (option) {
            return option.toLowerCase().startsWith(inputValue.toLowerCase());
        });

        // Adding suggestions to datalist
        suggestions.forEach(function (suggestion) {
            const option = document.createElement('option');
            option.value = suggestion;
            datalistElement.appendChild(option);
        });
    })
}

const departDatalist = document.getElementById('depart-list');
autoComplete(departInput, departDatalist, departList);
const arriveeDatalist = document.getElementById('arrivee-list');
autoComplete(arriveeInput, arriveeDatalist, arriveeList);

var resetBtn = document.getElementById('resetZoom');
// Reset map to initial state
resetBtn.addEventListener('click', function () {
    map.setView([48.856614, 2.3522219], 12);
    map.removeLayer(itineraryLayer);
    map.addLayer(markersLayer);
});
