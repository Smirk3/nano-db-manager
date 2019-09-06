# nano-db-manager
Backup/restore nano databases EventStore and Mongo DB.  
"nano-db-manager" can: 
- backup current nano databases state
- restore latest backup

You should backup first your current nano database state with "nano-db-manager" before running restore action.

## Configuration
This application provides executable JAR.  
  
Yuo have to provide parameters:
1. action: BACKUP or RESTORE (RESTORE - restores latest found backup in backup directory)
2. EventStore OS service name
3. MongoDB OS service name
4. EventStore database directory
5. MongoDB database directory
6. Backup store directory

## How to execute
NSSM must be installed on your machine (https://nssm.cc).  
Open windows command line as administrator (administrator permissions need to manage windows OS services). Execute JAR file `nano-db-manager-jar-with-dependencies.jar`  
 
Backup example:  
`java -jar ./nano-db-manager.jar backup nano-es nano-mongo "c:\development\es" "c:\development\mongo" "c:\development\backup-nano-db"`

Restore latest example:  
`java -jar ./nano-db-manager.jar restore nano-es nano-mongo "c:\development\es" "c:\development\mongo" "c:\development\backup-nano-db"`