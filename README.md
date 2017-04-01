# Snappy Open Source Project

Snappy is a Social Network Engine that supports the concepts listed below.

You are free to fork and modify it and build your own social network using its source code.

It is licensed under the Apache License 2.0.

# Quickstart

Follow these steps to get your own version of Village up and running locally.

Assumes Debian Linux environment.

1) Install ArangoDB and Tomcat

```bash
apt-get install arangodb3 tomcat8
```

Make sure you can visit http://localhost:8529 and see the ArangoDB admin.
Make sure you can also visit http://localhost:8080/manager/html and see the Tomcat admin.

If either of these doesn't work, you may need to run either `sudo service arangodb start` or `sudo service tomcat8 start`.

For images and file uploads to work, you'll need to run the following command as superuser:

```bash
mkdir /var/lib/village && cd /var/lib/village
chmod 755 . && chgrp tomcat8 . && chown tomcat8 .
```

This allows Village to write images and files to disk.

2) Build & upload the Village server

From the projects root directory, run:

```bash
./gradlew :backend:war
```

This will build the Village backend into `backend/build/libs/backend.war`.  Upload this using the Tomcat admin.

Note: You'll need to configure either Tomcat to point the backend to `/`, or Village to point to `/backend` on `localhost`.

To configure Tomcat to treat the backend as the root server, modify `/etc/tomcat/server.xml` to contain these lines:

```xml
<Context path="" docBase="backend">
    <!-- Default set of monitored resources -->
    <WatchedResource>WEB-INF/web.xml</WatchedResource>
</Context>
```

To configure the Village app, modify the `API_URL` constant in `Config.java` in the `shared` lib.

Note: You'll need to modify the IP address in this constant anyways, to point to the Village server.

See https://github.com/Queatz/Snappy-Web-App for setting up the website.

3) Run the Village Android application

To run the Village app, connect an Android device or emulator and run:

```bash
./gradlew :app:installDebug
```

Or use the Android Studio Run button.

## Supported Concepts

### Things

* Offers
    * Your skills, talents, or services that you, as a human being, offer to other human beings.
* Hubs
    * Your favorite physical locations on Earth, or places that you own, such as your house or your farm land.
* Clubs
    * Social groups that you are interested in.  The only thing that is not tied to your geolocation.
* Projects
    * Your projects, such as building a house or making a film.
* Resources
    * Your resources, such as your lawnmower, that you want to make publicly discoverable.
* Parties
    * Allows users to publicly throw parties.
* Location
    * A point on the map with an associated image and name.
* Person
    * A person, or user of the network.

### Relations

* Hierarchy
    * For example, if you are preparing a film project, you may have various sub-projects that support the main project, such as location scouting.
* Following
    * You can follow things to be updated when something happens with them.
* Updates
    * Most things can have updates posted to them.
* Likes
    * Most things can accumulate likes.
* Contacts
    * Most things have a Main Contact.  This is the person who is managing access to that thing.
* Messaging
    * Standard messaging is supported between users.
* Visibility
    * Visibility of things can be restricted to specific social groups.
* Membership
    * Allows people to be "members" of things, such as parties and clubs.
* Recents
    * Useful for knowing who you've recently talked to.


![Village Screenshot](https://lh3.googleusercontent.com/CaHQrKG7odam96KPp2V1EhQETfhc_joJxhOEbTKAv2VpVSLAa_EcHkuhAqd6-06FKqM=h900-rw)

Screenshot from the Android application.

# Project Overview

* Snappy currently depends on ArangoDB (http://arangodb.com)
* Snappy uses Google's Material Design
* Snappy has been developed in Android Studio

# Project Notes

* Files referenced like "gateway.*" need to be manually created and filled out with project keys and secrets, your IDE should complain.