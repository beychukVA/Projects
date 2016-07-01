# Documentation

* install msm-guide-core library using bat file `lib/_install_libs.bat`
* Configure database connection using config file `src/main/resources/hibernate.cfg.xml`
* Create database
* Open file `pom.xml` and configure path to files data (default it is `D:\workspace\workspace_android\msm-guide\msm-guide-data\`)
* Run importer using command `mvn clean package exec:java` (it should give exception)
* Stop importer
* Open database and update next filed types (for example using HeidySQL): 
- Table `icon` column `data` type BLOB to MEDIUMBLOB
- Table `localized` column `text` type TEXT to MEDIUMTEXT
* Run importer. Now it should work