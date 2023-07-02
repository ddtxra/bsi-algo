Different implementations developed by the PCI team in Geneva to build Blood Stream Infection algorithms.
Currently the repository contains 2 implementations (and it is a work in progress):

* ch.hcuge.spci.bsi.impl.hugv2023.PositiveHemoCultureHUGv2023 For a semi-automated surveillance done by PCI nurses at HUG
* ch.hcuge.spci.bsi.impl.praise.BSIClassifierPRAISE For a fully-automated surveillance in collaboration with PRAISE
* (CLABSI )

The application is written in Java and can be executed in Java directly (11 or above) or in Docker
# Run the application
2 modes: Java or Docker

## Run with java
Specify a folder where you will have a file 
```bash
java -jar target/bsi-algorithm-1.0-SNAPSHOT.jar <FOLDER>
java -jar target/bsi-algorithm-1.0-SNAPSHOT.jar praise-mds
```

## Run with dockerhub (easiest)

```bash
# Select a folder with BLOODCULTURES.CSV file inside
docker run -v .\praise-mds:/data bsi-algo-src
docker run -v ./praise-mds:/data ddtxra/bsi-algo
```
An output should be showed like this:
```
Reading cultures from: C:\git\github\bsi-algo\praise-mds
BLOODCULTURES.CSV file found in the given folder.
Algorithm implementation: PRAISE (BSIClassifierPRAISE)
- 255 cultures processed between 2021/01/01 and 2021/01/29 for 57 patients
- 101 episodes computed for 57 patients
--- 15 COB (community-onset bacteremia) episodes for 14 patients
--- 30 Solitary commensals (contaminations) for 18 patients
--- 56 HOB (hospital-onset bacteremia) episodes for 43 patients
----- 23 polymicrobial HOB episodes
----- 17 CSC HOB episodes
Saving all episodes file in praise-mds/OUTPUT_PRAISE_ALL_20230702234934.CSV for debug
Saving HOB episodes file in praise-mds/OUTPUT_PRAISE_HOBS_20230702234934.CSV
```

### Build and run the image with docker from jar pre-compiled
```bash
docker build -t bsi-algo .
FOLDER=`pwd`/praise-mds
docker run --rm -v $FOLDER:/data bsi-algo
```

### Build and run the image with docker from Java sources
```bash
docker build -t bsi-algo-src -f DockerfileSRC .
FOLDER=./praise-mds
docker run --rm ./praise-mds:/data bsi-algo-src
```


# To help
# Build the image for dockerhub
```
mvn package shade:shade
docker build -t bsi-algo .
docker tag bsi-algo ddtxra/bsi-algo  
docker push ddtxra/bsi-algo
```

# GLOSSARY 

### copy strains

COPY STRAINS exists for HOB and COB (COmmunity Onset )

### solitary commensal

•	A « solitary commensal » is the same as a « contamination » for you ?


### simple rules

o	An episode can either be a « solitary commensal » or a « OB » = onset-bacteremia.
o	In case of beeing a « OB » it can be a « COB » or a « HOB «
o	Only OBs (COBs and HOBs) can have polymicrobial episodes
o	Only OBs (COBs and HOBs) can generate  copy strains
