# Snappy Open Source Project

Snappy is a Social Network Engine that supports the concepts listed below.

You are free to fork and modify it and build your own social network using its source code.

It is licensed under the Apache License 2.0.

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
* Endorsements
    * Offers can be endorsed.
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

* Snappy is built on top of Google App Engine
* Snappy uses Google's Material Design
* Snappy has been developed in Android Studio

# Project Notes

* Files referenced like "gateway.*" need to be manually created and filled out with project keys and secrets, your IDE should complain.
* `python3 gen.py` needs to be run once initially and after editing things in the shared/ project