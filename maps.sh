#!/bin/bash

# Demande à l'utilisateur de saisir un choix (1 ou 2)
echo "Choisissez un dossier à télécharger :"
echo "1. Map Terrain"
echo "Aperçu 1: src/main/resources/static/Terrain.png" 
echo "Aperçu 2: src/main/resources/static/Terrain2.png" 
echo "2. Map Toner"
echo "Aperçu 1: src/main/resources/static/Toner.png" 
echo "Aperçu 2: src/main/resources/static/Toner2.png" 
read choix

# Vérifie si le choix est 1 ou 2, sinon affiche un message d'erreur
if [ $choix -eq 1 ]
then
    # Télechargement de la map Terrain
    wget --no-check-certificate --no-proxy --recursive --level=0 --no-parent --no-clobber --directory-prefix="src/main/resources/static" "https://drive.google.com/drive/folders/DOSSIER_A_ID"
    echo "La map Terrain a été téléchargé"
elif [ $choix -eq 2 ]
then
    # Télechargement de la map Toner
    wget --no-check-certificate --no-proxy --recursive --level=0 --no-parent --no-clobber --directory-prefix="src/main/resources/static" "https://drive.google.com/drive/folders/DOSSIER_B_ID"
    echo "La map Toner a été téléchargé"
else
    echo "Choix invalide. Veuillez saisir 1 ou 2."
fi
