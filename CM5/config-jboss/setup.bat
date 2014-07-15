set JBOSS_HOME=D:\Java\JBoss\jboss-eap-6.2
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_25
set POSTGRESQL_JDBC4=postgresql-9.3-1100-jdbc41.jar
set EAR=cm-sochi-solr-0.5.11-6.ear

copy %POSTGRESQL_JDBC4% postgresql-jdbc4.jar
copy %EAR% cm-sochi.ear


"%JBOSS_HOME%\bin\jboss-cli" --file=setup.cli --properties=deploy.properties --connect

del postgresql-jdbc4.jar
del cm-sochi.ear