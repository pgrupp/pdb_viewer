## Purpose

This is a simple yet useful viewer for Protein Data Bank (PDB) files. It should be able 
to read any .pdb file exported from the website. It includes three example files which 
can instantly be loaded via the menu.

## Dependencies
 - [Apache Maven 3](http://maven.apache.org/) for the build.
 - JavaFX runtime. This is included in the 
 [Oracle JRE 8 or Oracle JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
 - [Git](https://git-scm.com/) (not needed if you download a .tar.gz or .zip file from the website).

## Donwload

Navigate to some directory in a terminal. This is where you want the code base (in the following <PROJECT_DIR>) to
lie. Then run the following:

`git clone https://github.com/pgrupp/pdb_viewer.git`

## Usage

The application runs using JavaFX and should be self-explanatory.

## Build

To build the application Oracle Java SDK 8 and Maven 3 is required. Other dependencies are resolved by Maven.

### Complete build
1. Compile the runnable JAR file and run tests.

`mvn package`

2. Run the application using the generated JAR file. Which should be located in <PROJECT_DIR>/target.

java -jar <PROJECT_DIR>/target/pdb_viewer-1.0.jar

Or just double-click the generated JAR file.

### Run tests only

In order to only run the provided tests run the following command in the <PROJECT_DIR>.

`mvn test`

### Compile only binaries
In order to simply compile the source code run the following command in the <PROJECT_DIR>.

`mvn compile`
