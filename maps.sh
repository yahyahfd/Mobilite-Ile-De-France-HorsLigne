#!/bin/bash

file_id="1K5G1SseaitFX2BeANxjCmqeDIsf7Du1N" # ID du zip Terrain
file_name="src/main/resources/static/Terrain.zip" # Chemin d'enregistrement (relatif)

curl -c /tmp/cookies "https://drive.google.com/uc?export=download&id=${file_id}" > /tmp/intermezzo.html
code="$(awk '/_warning_/ {print $NF}' /tmp/cookies)"
curl -Lb /tmp/cookies "https://drive.google.com/uc?export=download&confirm=${code}&id=${file_id}" -o "${file_name}"

echo "Le fichier ${file_name} a été téléchargé avec succès."

unzip "${file_name}" -d "src/main/resources/static/"
echo "Le fichier ${file_name} a été décompressé avec succès."

rm "${file_name}"
echo "Le fichier ${file_name} a été supprimé avec succès."
