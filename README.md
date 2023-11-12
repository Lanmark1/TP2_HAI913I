# HAI913I - TP2 Compréhension des programmes
TP réalisé par :
- BENTOLILA Jérémie

## Pré-requis

- Maven
- Java JDK 11

## Installation / Exécution

- Extraire l'archive du TP dans un dossier 
- Ouvrir une console de commande à la racine du TP
### Partie Standard :
- Exécuter les commandes suivantes : 
  ``` 
  cd TP2_HAI913I_Standard
  mvn compile exec:java
  ``` 
- Choisir l'option souhaitée à appliquer au TP
### Partie Spoon :
- Exécuter les commandes suivantes : 
  ``` 
  cd TP2_HAI913I_Spoon
  mvn compile exec:java
  ``` 
- Choisir l'option souhaitée à appliquer au TP

## Utilisation

- Possibilité de choisir ou non un projet à analyser. (à déposer au préalable dans projectsToParse)
    - Si un nom de projet est précisé lors de l'exécution il sera récupéré dans le dossier projectsToParse
    - Sinon le projet *dummyProject* sera analysé par défaut
    
---
- Possibilité de choisir une options parmi les options suivantes lors de l'exécution :
    1. Graphe d'appel
	2. Graphe de couplage
	3. Arbre de clustering
	4. Modules identifiés selon un cp fixé (0.1, 0.2)
	5. Toutes les fonctionnalités listées ci-dessus