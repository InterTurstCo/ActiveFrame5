JBOSS_HOME=D:/Java/JBoss/jboss-eap-6.2
JAVA_HOME=C:/Program Files/Java/jdk1.7.0_25
POSTGRESQL_JDBC4=postgresql-9.3-1100-jdbc41.jar
EAR=cm-sochi-solr-0.5.11-6.ear

cp $POSTGRESQL_JDBC4 postgresql-jdbc4.jar
cp $EAR cm-sochi.ear


"$JBOSS_HOME/bin/jboss-cli.sh" --file=setup.cli --properties=deploy.properties --connect

rm postgresql-jdbc4.jar
rm cm-sochi.ear