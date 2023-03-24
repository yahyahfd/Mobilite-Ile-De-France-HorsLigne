var osm_humanitarian = L.tileLayer('http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href = "https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
});

var map = L.map('map', {
    layers: [osm_humanitarian]
}).setView([48.856614, 2.3522219], 12);

var markersLayer = L.markerClusterGroup();
var departList = [];
var arriveeList = [];

// GET stations list and adding them to the map (to layergroup then layergroup to map)
fetch('http://localhost:8080/stations')
    .then(response => response.json())
    .then(data => {
        data.forEach(station => {
            var marker = L.marker([station.localisation.latitude, station.localisation.longitude])
                .bindPopup(station.name);
            markersLayer.addLayer(marker);
            
            // Adding station names + localisation to datalist of both inputs
            departList.push(station.name + " (" + station.localisation.latitude + "," + station.localisation.longitude + ")");
            arriveeList.push(station.name + " (" + station.localisation.latitude + "," + station.localisation.longitude + ")");
        });
        map.addLayer(markersLayer);
    })
    .catch(error => console.error(error));

const departInput = document.getElementById('depart');
const arriveeInput = document.getElementById('arrivee');

departInput.addEventListener('input', function () {
    const departDatalist = document.getElementById('depart-list');
    departDatalist.innerHTML = '';
    const departValue = this.value;

    // Filtering names in input each time user presses key (accents not handled like terminal mode)
    const departSuggestions = departList.filter(function (station) {
        return station.toLowerCase().startsWith(departValue.toLowerCase());
    });

    // Adding suggestions to datalist
    departSuggestions.forEach(function (suggestion) {
        const option = document.createElement('option');
        option.value = suggestion;
        departDatalist.appendChild(option);
    });
});

arriveeInput.addEventListener('input', function () {
    const arriveeDatalist = document.getElementById('arrivee-list');
    arriveeDatalist.innerHTML = '';
    const arriveeValue = this.value;

    // Filtre les noms de station en fonction de la saisie utilisateur pour l'arrivée
    const arriveeSuggestions = arriveeList.filter(function (station) {
        return station.toLowerCase().startsWith(arriveeValue.toLowerCase());
    });

    // Ajouter les suggestions à la datalist pour l'arrivée
    arriveeSuggestions.forEach(function (suggestion) {
        const option = document.createElement('option');
        option.value = suggestion;
        arriveeDatalist.appendChild(option);
    });
});

// When we click on checkbox we show/hide stations
document.getElementById('toggleMarkers').addEventListener('change', function () {
    if (this.checked) {
        map.addLayer(markersLayer);
    } else {
        map.removeLayer(markersLayer);
    }
});

var resetBtn = document.getElementById('resetZoom');
// Reset map to initial state
resetBtn.addEventListener('click', function () {
    map.setView([48.856614, 2.3522219], 12);
});
