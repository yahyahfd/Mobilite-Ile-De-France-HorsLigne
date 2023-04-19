var osm_terrain = L.tileLayer('Terrain/{z}/{x}/{y}.png', {
    attribution: 'Map tiles by Stamen Design, under CC BY 3.0. Data by OpenStreetMap, under ODbL.'
});

var bounds = L.latLngBounds([[48.3, 1.5], [49.2, 3.5]]);

var map = L.map('menu2', {
    layers: [osm_terrain],
    maxBounds: bounds,
}).setView([48.856614, 2.3522219], 13);
map.setMaxZoom(15);
map.setMinZoom(10);

map.on('zoomend', function () {
    var currentZoom = map.getZoom();
    console.log('Niveau de zoom actuel : ' + currentZoom);
});

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
            var localisations = station.localisations;
            var markers = [];

            for(let i in localisations){
                var marker = L.marker([localisations[i].latitude, localisations[i].longitude])
                    .bindPopup(station.name + getLinesLogoColor(lines)); //label for each marker
                markers.push(marker);
            }

            markersLayer.addLayer(L.layerGroup(markers));
            // var marker = L.marker([station.localisation.latitude, station.localisation.longitude])
            //     .bindPopup(station.name + '<br>' + " Lignes: " + '<br>' + lines); //label for each marker
            //markersLayer.addLayer(marker);

            // Adding station names + localisation to datalist of both inputs
            departList.push(station.name);
            arriveeList.push(station.name);
        });
        map.addLayer(markersLayer);
    })
    .catch(error => console.error(error));

const departInput = document.getElementById('depart');
const arriveeInput = document.getElementById('arrivee');

const form = document.querySelector('#itinerary_form');
const errorMessage = document.getElementById('error_itinerary');

const itinerary = document.getElementById('itinerary');

const drawing_menu = document.getElementById('second_left');
const main_menu = document.getElementById('first_left');

const back_button = document.getElementById('back_button');
back_button.addEventListener('click', function () {
    main_menu.style.display = 'block';
    drawing_menu.style.display = 'none';
    itinerary.innerHTML = '';
    map.setView([48.856614, 2.3522219], 13);
    map.removeLayer(itineraryLayer);
    map.addLayer(markersLayer);

    tab1.innerHTML = "Menu";
    tab2.innerHTML = "Map";
});

// var resetBtn = document.getElementById('resetZoom');
// // Reset map to initial state
// resetBtn.addEventListener('click', function () {
//     map.setView([48.856614, 2.3522219], 12);
//     map.removeLayer(itineraryLayer);
//     map.addLayer(markersLayer);
// });

form.addEventListener('submit', function (event) {
    event.preventDefault();

    // if (selectedOption === 'plus-rapide') {
    //   // Code pour trouver l'itinéraire le plus rapide
    // } else if (selectedOption === 'plus-court') {
    //   // Code pour trouver l'itinéraire le plus court
    // } else if (selectedOption === 'moins-marche') {
    //   // Code pour trouver l'itinéraire avec moins de marche
    // }

    // We get out selected travel option
    var optionsSelect = document.getElementById('options');
    var selectedOption = optionsSelect.value;
    var travel_option = 2;
    switch(selectedOption){
        case 'dist':
            travel_option = 0
            break;
        case 'time':
            travel_option = 1
            break;
        default:
            travel_option = 2;
    }

    // We calculate a path (make a request to get a path)
    const departValue = encodeURIComponent(departInput.value);
    const arriveeValue = encodeURIComponent(arriveeInput.value);
    const url = `/shortest-way?depart=${departValue}&arrivee=${arriveeValue}&preference=${travel_option}`;


    var urlLines = `/shortest-way/lines?depart=${departValue}&arrivee=${arriveeValue}&preference=${travel_option}`;

    var linesItinerary = null;

    fetch(urlLines)
        .then(response => response.json())
        .then(dataLines => {
            linesItinerary = dataLines;
            fetch(url)
            .then(response => response.json())
            .then(data => {
                if (data.length == 0) { // No path found
                    errorMessage.style.display = "block";
                    errorMessage.textContent = "Aucun chemin trouvé suivant les stations spécifiées.";
                } else {// We draw a path on our map (need to add a written path later here)
                    errorMessage.style.display = "none";
                    main_menu.style.display = "none";
                    drawing_menu.style.display = "block";
                    itineraryLayer.clearLayers();
                    var current_station = null;
                    const latLngs = [];

                    // We place each station on the map and draw a line between each two consecutive stations
                    data.forEach(station => {
                        var stationName = station.name;
                        var lineName = linesItinerary[stationName].lineNameWithoutVariant;
                        itinerary.innerHTML += "<span class='station_name'><i class='fa-solid fa-location-dot'></i>" + stationName + '</span>';
                        if (station != data[data.length - 1]) itinerary.innerHTML += "<span class='separator'> <i class='fa-solid fa-down-long'></i></span>";
                        var latitude = station.localisation.latitude;
                        var longitude = station.localisation.longitude;
                        latLngs.push([latitude, longitude]);
                        var lines = station.neighboringLines.join('<br>');
                        var marker = L.marker([latitude, longitude])
                            .bindPopup(
                                stationName + getLinesLogoColor(lines));
                        if (current_station != null) {
                            var polyline = L.polyline([current_station.getLatLng(), marker.getLatLng()], { color: getColorByLineName(lineName), weight: 10, opacity:3 });
                            itineraryLayer.addLayer(polyline);
                            polyline.bindTooltip(lineName, {permanent: false, direction: "center"});
                        }
                        current_station = marker;
                        itineraryLayer.addLayer(marker);
                    });
                    map.removeLayer(markersLayer);
                    map.addLayer(itineraryLayer);
                    map.fitBounds(latLngs);
                    map.setZoom(13);
                    document.getElementById("tab2").classList.add("active");
                    document.getElementById("tab1").classList.remove("active");
                    document.getElementById("menu2").classList.add("active");
                    document.getElementById("menu1").classList.remove("active");
                    tab1.innerHTML = "Textuel";
                    tab2.innerHTML = "Visuel";
                    map.invalidateSize();
                }
            })
        })
        .catch(error => { // Bad syntax or empty inputs
               console.log(error);
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

function getColorByLineName(lineName) {
    switch (lineName) {
        case '1':
            return '#FFBE00';
        case '2':
            return '#0055C8';
        case '3':
            return '#6E6E00';
        case '3bis':
            return '#82C8E6';
        case '4':
            return '#A0006E';
        case '5':
            return '#FF5A00';
        case '6':
            return '#82DC73';
        case '7':
            return '#FF82B4';
        case '7bis':
            return '#82DC73';
        case '8':
            return '#D282BE';
        case '9':
            return '#D2D200';
        case '10':
            return '#DC9600';
        case '11':
            return '#6E491E';
        case '12':
            return '#00643C';
        case '13':
            return '#82C8E6';
        case '14':
            return '#640082';
        default:
            return 'blue';
    }
}

function getLinesLogoColor(lines){
    var result = '';
    lines.split('<br>').forEach(line => {
        result += '<br> <i class="fa-solid fa-train fa-beat fa-xl" style="color: '+getColorByLineName(line)+ ';"></i> &emsp;' + line+'<br>';
    });
    return result;
}

const departDatalist = document.getElementById('depart-list');
autoComplete(departInput, departDatalist, departList);
const arriveeDatalist = document.getElementById('arrivee-list');
autoComplete(arriveeInput, arriveeDatalist, arriveeList);

const tabs = document.querySelectorAll('.tabs button');
const menus = document.querySelectorAll('.menu');

tabs.forEach(tab => {
  tab.addEventListener('click', () => {
    // Retirer la classe "active" de tous les boutons et menus
    tabs.forEach(tab => tab.classList.remove('active'));
    menus.forEach(menu => menu.classList.remove('active'));
    // Ajouter la classe "active" au bouton cliqué et au menu correspondant
    tab.classList.add('active');
    const menu = document.querySelector(`#${tab.id.replace('tab', 'menu')}`);
    menu.classList.add('active');
    map.invalidateSize();
  });
});
