To build and install the services locally, just run the following command:

  mvn clean install -DskipTests

This assumes some environment variables are set, including:

BPS_JEESERVER_HOME - the location of the Tomcat installation.
BPS_WEBROOT - the location of the Apache content root
DB_USER - the name of the master user for creating DBs, etc. 
DB_PASSWORD - password for DB_USER

If this is the first time you are setting up bps, you will need to run
the additional target:

  mvn create_db

Once the maven builds are complete, start tomcat, and verify that the 
services are working by issuing the following get (e.g., in your browser):

   http://localhost:8180/bps.services.webapp/version

This should return an XML payload with version information.

If you are building locally and deploying remotely, you just need to ensure
that the environment variables are set for the remote host, build, and then
copy the war file to the server. 
