To start MIAPE Extractor:

On windows:
 - click on START_win_XGB.bat, where X is the amount of memory (in GigaBytes) that the tool will use.
 
On Linux or MAC-OS
 - click on START_macos_XGB.sh, where X is the amount of memory (in GigaBytes) that the tool will use.
 
 
For custom amount of memory use, edit the corresponding file and put the desired amount of memory changing the -Xmx argument.
Note that here the memory is stated in megabytes, so 1024m = 1GB

java -jar -Xms64m -Xmx1024m miape-extractor-*.jar   for 1GB
java -jar -Xms64m -Xmx2048m miape-extractor-*.jar   for 2GB
java -jar -Xms64m -Xmx1536m miape-extractor-*.jar   for 1,5GB
