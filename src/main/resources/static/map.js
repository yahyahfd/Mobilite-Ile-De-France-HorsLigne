var osm_humanitarian = L.tileLayer('http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href = "https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
});

var map = L.map('map', {
    layers: [osm_humanitarian]
}).setView([48.856614, 2.3522219], 12);

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
                .bindPopup(station.name + '<br>' + " Lignes: " + '<br>' + lines); // Ajouter les lignes voisines au nom de la station
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
            if(data.length == 0){
                errorMessage.style.display = "block";
                errorMessage.textContent = "Aucun chemin trouvé suivant les stations spécifiées.";
            }else{
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

var resetBtn = document.getElementById('resetZoom');
// Reset map to initial state
resetBtn.addEventListener('click', function () {
    map.setView([48.856614, 2.3522219], 12);
    map.removeLayer(itineraryLayer);
    map.addLayer(markersLayer);
});
