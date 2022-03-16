## PACOM (Proteomics Assay COMparator)
**PACOM** is a Java stand alone tool that is able to import, integrate, manage and compare numerous proteomics datasets at the same time, offering a rich set of graphical representation of the most common proteomics data features

## How to get PACOM:
We are continuosly trying to improve the software, adding new features and fixing bugs. New versions are automatically uploaded to out server.  
Download the latest versions from **[here](http://sealion.scripps.edu/PACom/)** or in the release page **(https://github.com/smdb21/PACOM/releases)**

## Files supported:
The updated list of input files supported by PACOM can be found at the corresponding [**wiki page**](https://github.com/smdb21/PACOM/wiki/How-to-import-datasets#2-select-the-input-data-files-to-import).  
  
## System requirements
 - **Java Runtime Environment (JRE) version: 1.8** (Download it from [ http://www.oracle.com/technetwork/java/javase/downloads/index.html](http://www.oracle.com/technetwork/java/javase/downloads/index.html))  
 - **Physical Memory (RAM):** As a Java standalone software, you can specify the maximum amount of memory on which it is going to run. It depends on the file (*.bat for Windows and *.sh for Linux and MAC OS) that you use for running it. However, depending on whether the Java Runtime Environment (JRE) you have works on 32 bits or 64 bits (only available for 64 bits OS), you will be able to use more or less amount of memory. For JRE 32 bits version (x86) the maximum amount of memory is only close to 1.4Gb (which is not much).
To import datasets into the tool, 1 Gb or 1.4 Gb of RAM memory would be enough, which can be available for 32 bit machines. However, to inspect and compare data, it is strongly recommended to have more memory, such as 16Gb. 
As an example, 9 experiments containing a total number of 126 fractions (that is, 126 MASCOT search results), will require 8Gb of RAM.
 - **Recommended Screen Resolution:** 1280x700
 - **Recommended CPU:** 2 or more CPU cores (systems with a single core processor will experience a slow performance).

## Instalation guide
There is no need to install anything. Just download it from the link above, decompress the package (.zip or .tar.gz) and run one of the *START_\** files (depending on the amount of memory you want to use).
  
For a more detailed description of these instructions, go to the corresponding [**wiki page**](https://github.com/smdb21/PACOM/wiki/How-to-start#installation)

## Demo
If you want to quickly try PACOM, open it, select one of the example comparison projects (**Inspect examples**) such as *PME6_Reanalysis" and click on the orange button at the right.
  
![Figure 10](https://raw.githubusercontent.com/wiki/smdb21/PACOM/img/inspection/Picture10.png) 

 Then, in about 15 seconds you will see the main interface of PACOM for dataset inspection and comparison:
 
 ![Figure 12](https://raw.githubusercontent.com/wiki/smdb21/PACOM/img/comparison/Picture12.png) 

## Instructions for use:
For a **complete manual** and more detailed information about how to use the tool, go to our [**wiki page**](https://github.com/smdb21/PACOM/wiki).

## Asking for help? Do you have any suggestion?
Write an email to **salvador** at **scripps.edu**. I will try to respond you as soon as possible! 
#### Thanks in advance for your contribution!!
   
### About developers
This tool has been enterely designed and implemented by [Salvador Martinez-Bartolome](https://www.ncbi.nlm.nih.gov/pubmed/?term=Martinez-Bartolome+S) firstly as a member of the [ProteoRed](http://www.proteored.org) Bioinformatics Working Group, under the supervision of Juan Pablo Albar, at the [Proteomics Laboratory](http://proteo.cnb.csic.es/proteomica/) of the [National Center for Biotechnology (CNB-CSIC)](http://www.cnb.csic.es) in Madrid, Spain. Later, the project was continued under the supervision of John R. Yates III at the [John Yates laboratory](http://www.scripps.edu/yates) at [The Scripps Research Institute](http://www.scripps.edu), La Jolla, California, USA.

#### Other people that contributed to this project:
- Miguel Angel Lopez (the best hardware support).
- J. Alberto Medina (software development support and beta tester).
- Gorka Prieto ([@akrogp](https://github.com/akrogp)) (PAnalyzer grouping algorithm developer).
- Rosana Navajas (beta tester and manuscript contributor).
- Carmen Gonzalez (beta tester and manuscript contributor).
- Carolina Fernandez Costa (beta tester and manuscript contributor).
- Emilio Salazar-Donate ([@emiliosalazardonate](https://github.com/emiliosalazardonate)) (First developer contributing to this project in its very early phase).
- Juan Pablo Albar (main supporter of this project. All success of this project is dedicated to him).
- John R. Yates III ([@proteomicsyates](https://github.com/proteomicsyates)) (supporter of this project in its second phase). 
---
This tool has been developed using the **Java MIAPE API** which source code can be fount at this [git hub](https://github.com/smdb21/java-miape-api) web page.
