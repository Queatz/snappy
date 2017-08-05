
# Running Village on a single box

## Backend & Database

#### 1) Setup Environment

Using Debian 8.0...

Domain: `vlllage.com`

    sudo su -
        
    curl -O https://www.arangodb.com/repositories/arangodb31/Debian_8.0/Release.key
    apt-key add - < Release.key
    
    apt-get install software-properties-common apt-transport-https -y --force-yes
    apt-add-repository 'http://deb.debian.org/debian/ sid main contrib'
    echo 'deb https://www.arangodb.com/repositories/arangodb31/Debian_8.0/ /' | sudo tee /etc/apt/sources.list.d/arangodb.list

    apt-get update
    apt-get install openjdk-8-jre openjdk-8-jre-headless openjdk-8-jdk ca-certificates-java -y
    apt-get install apache2 tomcat8 tomcat8-admin git default-jdk -y
    apt-get install arangodb3 -y
    apt-get install libservlet3.1-java -y

Note, if you see `arangodb3 : Depends: libssl1.0.0 (>= 1.0.1) but it is not installable`,
then make sure you have `jessie` in your `/etc/apt/sources.list` file.

    deb http://deb.debian.org/debian/ jessie contrib main

Check https://www.arangodb.com/download-major/debian/ for latest information.

Install `node` (see https://github.com/nodesource/distributions#debinstall)

    curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -
    sudo apt-get install -y nodejs

Modify `/etc/tomcat8/tomcat-users.xml` to include within `<tomcat-users>`:

    <role rolename="tomcat"/>
    <role rolename="manager-script"/>
    <user username="TOMCATSECRETUSER293" password="TOMCATSECRETPASSWORD3984" roles="tomcat,manager-script"/>

Modify `/etc/tomcat8/context.xml` to include:

    <Context antiResourceLocking="false" privilaged="true">
        <Valve className="org.apache.catalina.valves.RemoteAddrValve"
                addConnectorPort="true" allow=".*;8080|.*;8443"/>
        
        ...
        
    </Context>


Modify `/etc/tomcat8/server.xml` to include within `<Service name="Catalina">`:

    <Connector port="8443" address="0.0.0.0"
               protocol="org.apache.coyote.http11.Http11NioProtocol"
               maxThreads="150" SSLEnabled="true" scheme="https" secure="true"
               SSLCertificateFile="/etc/letsencrypt/archive/vlllage.com/fullchain1.pem"
               SSLCertificateKeyFile="/etc/letsencrypt/archive/vlllage.com/privkey1.pem"
               SSLVerifyClient="none" SSLProtocol="TLSv1+TLSv1.1+TLSv1.2" />
               
    <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000"
               URIEncoding="UTF-8"
               redirectPort="8443"
               address="0.0.0.0" />
             
#### 2) Setup Database

`arangosh`

    arangosh> require('@arangodb/users').save('snappy', 'snappy')
    arangosh> require('@arangodb/users').grantDatabase('snappy', '_system')

See more here:
https://docs.arangodb.com/3.0/Manual/Administration/ManagingUsers.html

Make data dir:

    mkdir /var/lib/village/
    chown tomcat8 /var/lib/village/
    chgrp tomcat8 /var/lib/village/

#### 3) Setup Backend

Compile the backend in `snappy` with `./gradlew :backend:war`

Upload `backend/build/libs/backend.war` to your server


(Optional: If you already have the backend deployed, you may need to undeploy it)

    curl -u TOMCATSECRETUSER293:TOMCATSECRETPASSWORD3984 http://127.0.0.1:8080/manager/text/undeploy?path=/backend

Install the backend in Tomcat8

    curl --upload-file backend.war -u TOMCATSECRETUSER293:TOMCATSECRETPASSWORD3984 http://127.0.0.1:8080/manager/text/deploy?path=/backend&update=true
    curl -u TOMCATSECRETUSER293:TOMCATSECRETPASSWORD3984 http://127.0.0.1:8080/manager/text/reload?path=/


Modify `/etc/tomcat8/server.xml` to include within `<Host>`:

    <Context path="" docBase="backend">
        <!-- Default set of monitored resources -->
        <WatchedResource>WEB-INF/web.xml</WatchedResource>
    </Context>

Restart Tomcat8

`/etc/init.d/tomcat8 restart`

#### 4) Setup HTTPS

    apt-get install python-certbot-apache python-pip -y
    sudo pip install ndg_httpsclient
    certbot --apache
    chmod 0777 /etc
    chmod 0777 /etc/letsencrypt
    chmod 0777 /etc/letsencrypt/archive
    chmod 0777 /etc/letsencrypt/archive/vlllage.com
    chmod 0777 /etc/letsencrypt/archive/vlllage.com/fullchain1.pem
    chmod 0777 /etc/letsencrypt/archive/vlllage.com/privkey1.pem
    chmod 0777 /etc/letsencrypt/live
    chmod 0777 /etc/letsencrypt/live/vlllage.com
    chmod 0777 /etc/letsencrypt/live/vlllage.com/fullchain.pem
    chmod 0777 /etc/letsencrypt/live/vlllage.com/privkey.pem
    sudo apachectl restart

Enable ports:

    iptables -F
    iptables -X
    iptables -t nat -F
    iptables -t nat -X
    iptables -t mangle -F
    iptables -t mangle -X
    iptables -P INPUT ACCEPT
    iptables -P OUTPUT ACCEPT
    iptables -P FORWARD ACCEPT

Edit `CATALINA_HOME/webapps/manager/WEB-INF/web.xml` to contain:

    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>

Also, for safety, undeploy Tomcat manager:

      curl -u TOMCATSECRETUSER293:TOMCATSECRETPASSWORD3984 http://127.0.0.1:8080/manager/text/undeploy?path=/host-manager
      curl -u TOMCATSECRETUSER293:TOMCATSECRETPASSWORD3984 http://127.0.0.1:8080/manager/text/undeploy?path=/manager

Later, you can replace the file, like such:

    mv backend.war /var/lib/tomcat8/webapps/backend.war

Edit /etc/apache2/sites-enabled/000-default-le-ssl.conf 

    <IfModule mod_ssl.c>
    
    <VirtualHost *:443>
        SSLCertificateFile /etc/letsencrypt/live/vlllage.com/fullchain.pem
        SSLCertificateKeyFile /etc/letsencrypt/live/vlllage.com/privkey.pem
        Include /etc/letsencrypt/options-ssl-apache.conf
        ServerName vlllage.com
    
        ProxyRequests off
    
        <Proxy *>
                Require all granted
        </Proxy>
    
        SSLProxyEngine on
        ProxyPass / https://127.0.0.1:3000/
        ProxyPassReverse / https://127.0.0.1:3000/
        ProxyPreserveHost on
    </VirtualHost>

    </IfModule>

Edit /etc/apache2/sites-enabled/000-default.conf 

    <VirtualHost *:80>
        ServerName vlllage.com
        Redirect permanent / https://vlllage.com/
    </VirtualHost>

Restart apache:

    a2enmod proxy proxy_http rewrite
    apachectl restart

Make sure your firewall allows the following ports:

    80;443;8443

## Frontend

#### 1) Setup

    npm install -g @angular/cli
    cd Snappy-Web-App/web-app/src/main/webapp
    npm i
    sudo npm i -g typescript
    tsc
    sudo node app.js

#### Production Bundling

If you want production bundling, do:

    cd Snappy-Web-App/web-app/src/main/webapp
    ng build -prod -op dist/ --aot
    
Zip and upload `dist/` to your box and do:

    cd dist/
    sudo npm i --save-dev express
    cp ../Snappy-Web-App/web-app/src/main/webapp/app.js app.js
    sudo node app.js &

## Backup

If you want to backup all Village data

    arangodump --output-directory "dump"
    zip -r dump.zip dump/
    zip -r village-data.zip /var/lib/village