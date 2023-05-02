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

var neighboringLinesWithoutVariant = new Map();
var locationsMap = new Map();

// GET stations list and adding them to the map (to layergroup then layergroup to map)
fetch('http://localhost:8080/stations')
    .then(response => response.json())
    .then(data => {
        data.forEach(station => {

            var location;
        
            var lines = station.neighboringLinesWithoutVariant.join('<br>');

            if(!departList.includes(station.name)){
                
                location = station.location;
                locationsMap.set(station.name, location);

                neighboringLinesWithoutVariant.set(station.name, lines);
                // Adding station names + location to datalist of both inputs
                departList.push(station.name);
                arriveeList.push(station.name);
            
            }else {
                neighboringLinesWithoutVariant.set(station.name, Array.from(new Set((neighboringLinesWithoutVariant.get(station.name) + '<br>' + lines).split('<br>'))).join('<br>'));
            }
        });

        for (let [station, lines] of neighboringLinesWithoutVariant) {
            var location = locationsMap.get(station);
            var marker = L.marker([location.latitude, location.longitude])
            .bindPopup(station + getLinesLogoColor(lines)); //label for each marker

            markersLayer.addLayer(marker);
        }

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
const schedules = document.getElementById('schedules');
const schedules_form = document.getElementById('schedule_form');

let isDrawed = false;


const table_sc = document.getElementById('table_schedules');

schedules_form.addEventListener('submit', function (event) {
    event.preventDefault();
    const station_list = document.getElementById('station-list');
    const line_list = document.getElementById('line-list');
    const station_sc_input = document.getElementById('station_sc');
    autoComplete(station_sc_input, station_list,departList)
    const line_sc_input = document.getElementById('line_sc');

    const station_sc = encodeURIComponent(station_sc_input.value);
    const line_sc = encodeURIComponent(line_sc_input.value);

    const horairesTable = document.createElement('table');
    const horairesTableHead = document.createElement('thead');
    const trhead = document.createElement('tr');
    const horairesTableBody = document.createElement('tbody');

    const url = `/schedules?stationName=${station_sc}&lineName=${line_sc}`;

    fetch(url)
        .then(response => response.json())
        .then(data => {

            if(data.length == 0){
            
                window.alert("Aucun horaire n'a été trouvé pour cette station et cette ligne, il se peut que la ligne ne passe pas par cette station ou que la station n'existe pas.");
            
            } else {

                const win = window.open('', 'Schedules', 'width='+screen.width+',height='+screen.height);

                const nb_col = Math.ceil(data.length/100);

                for(let i = 0; i < nb_col; i++){
                    const th = document.createElement('th');
                    th.textContent = `Colonne ${i+1}`;
                    trhead.appendChild(th);
                }
                horairesTableHead.appendChild(trhead);
                horairesTable.appendChild(horairesTableHead);
    
                let row = document.createElement('tr');
                let i = 0;
                data.forEach(function(horaire){
                    const cell = document.createElement('td');
                    cell.textContent = horaire;
                    row.appendChild(cell);
                    i++;
                    if(i%nb_col == 0){
                        horairesTableBody.appendChild(row);
                        row = document.createElement('tr');
                    }
                });
                horairesTableBody.appendChild(row);
    
                horairesTable.appendChild(horairesTableBody);
    
                win.document.body.appendChild(horairesTable);
                
                //stylé le tableau
                win.document.body.style.backgroundColor = "#93a897";

            }

        })
        .catch(error => console.error(error));

});


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
    isDrawed = false;
});

form.addEventListener('submit', function (event) {

    event.preventDefault();

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


    fetch(url)
        .then(response => response.json())
        .then(data => {
            console.log(data);
            if (data['stations'].length <= 1 || data['lines'].length == 0) { // No path found
                let syntaxeMessage = " Suivez la syntaxe imposée dans les suggestions !";
                if (departValue.length == 0 && arriveeValue.length == 0) {
                    errorMessage.innerHTML = "Le départ et l'arrivée ne sont pas spécifiés !" +syntaxeMessage;
                } else if (departValue.length == 0) {
                    errorMessage.innerHTML = "Le départ n'est pas spécifié ! " +syntaxeMessage;
                } else if (arriveeValue.length == 0) {
                    errorMessage.innerHTML = "L'arrivée n'est pas spécifiée ! " +syntaxeMessage;
                } else if(arriveeValue == departValue){
                    errorMessage.innerHTML = "Vous ne pouvez pas voyager vers la même station." +syntaxeMessage;
                }
                else {
                    errorMessage.innerHTML = "Aucun chemin trouvé suivant les stations spécifiées. " +syntaxeMessage;
                }
                errorMessage.style.display = "block";
            } else {// We draw a path on our map (need to add a written path later here)
                itineraryLayer.clearLayers();
                const latLngs = [];

                var nextStation = null;
                var length = data['stations'].length;
                var dates = data['times'];
                var distCountTime = data['distCountTime'];
                var dist_totale = distCountTime[length-1].left;
                var time_totale = distCountTime[length-1].right;
                itinerary.innerHTML += "<span class='station_name'>-- Trajet : "+time_totale+" min. ~ "+dist_totale+" Km.</span>";
                // var distTimeLines = data['distTimeLines'];
                // We place each station on the map and draw a line between each two consecutive stations
                // var previousLineName = null;
                for (let i = 0; i < length-1; i++) {

                    var station = data['stations'][i];
                    var line = data['lines'][i+1];

                    var date = dates[i];
                    nextStation = data['stations'][i+1];
                    var nextStationName = nextStation.name;
                
                    var stationName = station.name;
                    var lineName = line.lineNameWithoutVariant;
                    // if(lineName != previousLineName){ // On print la durée totale de la ligne
                    //     var distTimeLine = distTimeLines.pop;
                    //     var distLine = distTimeLine.left;
                    //     var timeLine = distTimeLine.right;
                    //     console.log(lineName+"---------");
                    //     console.log(distLine);
                    //     console.log(timeLine);
                    //     previousLineName = lineName;
                    // }

                    if(! isDrawed) {                                        
                        if (station != data['stations'][length - 1]) {
                            let lineNumber = "<span id='linename'>&nbsp;"+lineName+"</span>";
                            if(lineName == '--MARCHE--') {
                                itinerary.innerHTML += "<span class='station_name'><i class='fa-solid fa-location-dot'></i>"+ stationName+
                                "</span><span id='line' class='separator'> <i class='fa-solid fa-person-walking fa-lg'></i>"+
                                lineNumber+"</span>";
                            } else {
                                itinerary.innerHTML += "<span class='station_name'><i class='fa-solid fa-location-dot'></i>"+ stationName+
                                " - take the train at "+ date+ "</span><span id='line' class='separator'> <i class='fa-solid fa-down-long'></i>"+
                                lineNumber+"</span>";
                            }
                                
                            const idLine = document.getElementById('line');
                            idLine.id += lineName;
                            idLine.style.color = getColorByLineName(lineName);
                            const idLineNumber = document.getElementById('linename');
                            idLineNumber.id += lineName;
                            idLineNumber.style.color = "#000";
                        }
                    }

                    var latitude = station.location.latitude;
                    var longitude = station.location.longitude;
                    var nextStationLatitude = nextStation.location.latitude;
                    var nextStationLongitude = nextStation.location.longitude;
                    latLngs.push([latitude, longitude]);
                    var lines = neighboringLinesWithoutVariant.get(stationName);
                    var linesNextStation = neighboringLinesWithoutVariant.get(nextStationName);
                    var marker = L.marker([latitude, longitude])
                        .bindPopup(
                            station.name + getLinesLogoColor(lines)
                        );
                    var markerNextStation = L.marker([nextStationLatitude, nextStationLongitude])
                        .bindPopup(
                            nextStation.name + getLinesLogoColor(linesNextStation)
                        );
                    var polyline = L.polyline(
                        [marker.getLatLng(), markerNextStation.getLatLng()], { color: getColorByLineName(lineName), weight: 10, opacity:3 }
                    );
                    if(lineName == '--MARCHE--') {
                        polyline.setStyle({color: getColorByLineName(lineName), dashArray: '5, 15'});
                    }
                    itineraryLayer.addLayer(polyline);
                    polyline.bindPopup(lineName, {permanent: false, direction: "center"});
                    itineraryLayer.addLayer(marker);
                
                }

                var lastStation = nextStation;
                var lastStationName = lastStation.name;
                var lastStationLatitude = lastStation.location.latitude;
                var lastStationLongitude = lastStation.location.longitude;
                var linesLastStation = neighboringLinesWithoutVariant.get(lastStationName);
                var markerLastStation = L.marker([lastStationLatitude, lastStationLongitude])
                    .bindPopup(
                        lastStation.name + getLinesLogoColor(linesLastStation)
                    );
                itineraryLayer.addLayer(markerLastStation);

                itinerary.innerHTML += "<span class='station_name'><i class='fa-solid fa-location-dot'></i>" + 
                lastStationName + '</span>';



                isDrawed = true;

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
                
                errorMessage.style.display = "none";
                main_menu.style.display = "none";
                drawing_menu.style.display = "block";
            }
            
        })
            
    .catch(error => { // Bad syntax or empty inputs
        console.log(error);
        errorMessage.style.display = "block";
        errorMessage.textContent = "Une erreur s'est produite. Vous ne pouvez pas chercher un itinéraire vers la même station.";
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
        if(line != '--MARCHE--'){
            result += '<br> <i class="fa-solid fa-train fa-beat fa-xl" style="color: '+getColorByLineName(line)+ ';"></i> &emsp;' + line+'<br>';
        }
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
