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
java -jar target/clabsi-algorithm-1.0-SNAPSHOT.jar <FOLDER>
java -jar target/clabsi-algorithm-1.0-SNAPSHOT.jar praise-mds
```

## Run with dockerhub (easiest)

```bash
# Select a folder with BLOODCULTURES.CSV file inside
FOLDER=./praise-mds 
docker run --rm -v $FOLDER:/data ddtxra/bsi-algo /data
```
An output should be showed like this:
```
ch.hcuge.spci.bsi.BSIApp - Reading from /data
ch.hcuge.spci.bsi.BSIApp - BLOODCULTURES.CSV file found in the given folder.
ch.hcuge.spci.bsi.BSIApp - - 255 cultures processed using the BSIClassifierPRAISE implementation
ch.hcuge.spci.bsi.BSIApp - - 101 'episodes?' computed 
ch.hcuge.spci.bsi.BSIApp - --- 15 COB (community-onset bacteremia)
ch.hcuge.spci.bsi.BSIApp - --- 30 contaminations (solitary commensals)
ch.hcuge.spci.bsi.BSIApp - --- 56 HOB episodes
ch.hcuge.spci.bsi.BSIApp - ----- 23 polymicrobial HOB episodes
ch.hcuge.spci.bsi.BSIApp - ----- 17 CSC HOB episodes
ch.hcuge.spci.bsi.BSIApp - File /data/OUTPUT_PRAISE_20230702151512.CSV saved
```

### Build and run the image with docker from jar pre-compiled
```bash
docker build -t bsi-algo .
FOLDER=`pwd`/praise-mds
docker run --rm -v $FOLDER:/data bsi-algo /data
```

### Build and run the image with docker from Java sources
```bash
docker build -t bsi-algo-src -f DockerfileSRC .
FOLDER=`pwd`/praise-mds
docker run --rm -v $FOLDER:/data bsi-algo-src /data
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
