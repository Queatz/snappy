
# Running Village on a single box

## Backend & Database

#### 1) Setup Environment

Using Debian 8.0...

Domain: `vlllage.com`

    apt-get install software-properties-common apt-transport-https -y --force-yes
    apt-add-repository http://deb.debian.org/debian/ sid main contrib
    apt-add-repository https://www.arangodb.com/repositories/arangodb31/Debian_8.0/ /
    
    apt-get install openjdk-8-jre openjdk-8-jre-headless openjdk-8-jdk ca-certificates-java
    apt-get install tomcat8 tomcat8-admin git default-jdk -y --force-yes
    apt-get install arangodb3 -y --force-yes
    apt-get install libservlet3.1-java -y --force-yes

Modify /etc/tomcat8/tomcat-users.html to include:

    <role rolename="tomcat"/>
    <role rolename="manager-script"/>
    <user username="tomcat" password="tomcat" roles="tomcat,manager-script"/>

Modify /etc/tomcat8/context.html to include:

    <Context antiResourceLocking="false" privilaged="true">
        <Valve className="org.apache.catalina.valves.RemoteAddrValve"
                addConnectorPort="true" allow=".*;8080|.*;8443"/>
        
        ...
        
    </Context>


Modify /etc/tomcat8/server.html to include within <Host>:

    <Context path="" docBase="backend">
        <!-- Default set of monitored resources -->
        <WatchedResource>WEB-INF/web.xml</WatchedResource>
    </Context>

Modify /etc/tomcat8/server.xml to include:

    <Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
               maxThreads="150" SSLEnabled="true" scheme="https" secure="true"
               SSLCertificateFile="/etc/letsencrypt/live/vlllage.com/fullchain.pem"
               SSLCertificateKeyFile="/etc/letsencrypt/live/vlllage.com/privkey.pem"
               SSLVerifyClient="optional" SSLProtocol="TLSv1+TLSv1.1+TLSv1.2" />
               
#### 2) Setup Database

`arangosh`

    arangosh> require('@arangodb/users').save('snappy', 'snappy')
    arangosh> require('@arangodb/users').grantDatabase('snappy', '_system')

Make data dir:

    mkdir /var/lib/village/
    chown tomcat8 /var/lib/village/
    chgrp tomcat8 /var/lib/village/

See more here:
https://docs.arangodb.com/3.0/Manual/Administration/ManagingUsers.html

#### 3) Setup Backend

Compile the backend in `snappy` with `./gradlew :backend:war`

Upload `backend/build/libs/backend.war` to your server

Install the backend in Tomcat8

    curl --upload-file backend.war -u tomcat:tomcat http://tomcat:tomcat@127.0.0.1:8080/manager/text/deploy?path=/backend&update=true
    curl -u tomcat:tomcat http://127.0.0.1:8080/manager/text/reload?path=/

Restart Tomcat8

`/etc/init.d/tomcat8 restart`

#### 4) Setup HTTPS

    apt-get install python-certbot-apache -t jessie-backports python-pip
    sudo pip install ndg_httpsclient
    certbot --apache
    chmod 0666 /etc/letsencrypt
    chmod 0666 /etc/letsencrypt/archive
    chmod 0666 /etc/letsencrypt/archive/vlllage.com
    chmod 0666 /etc/letsencrypt/live/vlllage.com/fullchain.pem
    chmod 0666 /etc/letsencrypt/live/vlllage.com/privkey.pem
    sudo apachectl restart

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
    
    
        ProxyRequests off
    
    
        <Proxy *>
                Require all granted
        </Proxy>
    
    
        SSLProxyEngine on
        ProxyPass / https://127.0.0.1:3000/
        ProxyPassReverse / https://127.0.0.1:3000/
        ProxyPreserveHost on
    </VirtualHost>

## Frontend

#### 1) Setup

    cd Snappy-Web-App/web-app/src/main/webapp
    npm i
    sudo npm i -g typescript
    tsc
    sudo node app.js

#### Production Bundling

If you want production bundling, do:

    cd Snappy-Web-App/web-app/src/main/webapp
    ng build -prod -op dist/ --aot
    
Zip and upload to your box and do:

    cd dist/
    sudo npm i --save-dev express
    cp ../Snappy-Web-App/web-app/src/main/webapp/app.js app.js
    sudo node app.js