package com.queatz.snappy.team;

import com.loopj.android.http.RequestParams;

public enum EarthGraphQuery {
    SELECT_CLUBS("name,about,photo"),

    SELECT_FORM("name,about,data,photo"),

    SELECT_THING("\n" +
            "        message,\n" +
            "        token,\n" +
            "        data,\n" +
            "        type,\n" +
            "        role,\n" +
            "        date,\n" +
            "        firstName,\n" +
            "        hidden,\n" +
            "        lastName,\n" +
            "        googleUrl,\n" +
            "        imageUrl,\n" +
            "        name,\n" +
            "        going,\n" +
            "        action,\n" +
            "        photo,\n" +
            "        liked,\n" +
            "        likers,\n" +
            "        likes(\n" +
            "            source(\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                imageUrl,\n" +
            "                googleUrl\n" +
            "            )   \n" +
            "        ),\n" +
            "        in(\n" +
            "            target(\n" +
            "                name,\n" +
            "                photo,\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                imageUrl,\n" +
            "                googleUrl\n" +
            "            )\n" +
            "        ),\n" +
            "        joins(\n" +
            "            source(\n" +
            "                name,\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                photo,\n" +
            "                imageUrl,\n" +
            "                googleUrl\n" +
            "            )\n" +
            "        ),\n" +
            "        about,\n" +
            "        target(\n" +
            "            name,\n" +
            "            photo,\n" +
            "            firstName,\n" +
            "            lastName,\n" +
            "            googleUrl,\n" +
            "            imageUrl,\n" +
            "            around,\n" +
            "            infoDistance\n" +
            "        ),\n" +
            "        source(\n" +
            "            photo,\n" +
            "            name,\n" +
            "            imageUrl,\n" +
            "            googleUrl,\n" +
            "            firstName,\n" +
            "            lastName\n" +
            "        ),\n" +
            "        members(\n" +
            "            source(\n" +
            "                about,\n" +
            "                name,\n" +
            "                photo,\n" +
            "                date,\n" +
            "                source(\n" +
            "                    name,\n" +
            "                    photo,\n" +
            "                    firstName,\n" +
            "                    lastName,\n" +
            "                    imageUrl,\n" +
            "                    googleUrl\n" +
            "                )\n" +
            "            )\n" +
            "        ),\n" +
            "        clubs(\n" +
            "            name\n" +
            "        )\n" +
            "   "),

    SELECT_THING_2("\n" +
            "        name,\n" +
            "        photo,\n" +
            "        about,\n" +
            "        address,\n" +
            "        hidden,\n" +
            "        geo,\n" +
            "        infoDistance,\n" +
            "        data,\n" +
            "        members(\n" +
            "            role,\n" +
            "            source(\n" +
            "                message,\n" +
            "                token,\n" +
            "                data,\n" +
            "                type,\n" +
            "                role,\n" +
            "                date,\n" +
            "                firstName,\n" +
            "                hidden,\n" +
            "                lastName,\n" +
            "                googleUrl,\n" +
            "                imageUrl,\n" +
            "                name,\n" +
            "                photo,\n" +
            "                going,\n" +
            "                action,\n" +
            "                want,\n" +
            "                liked,\n" +
            "                likers,\n" +
            "                likes(\n" +
            "                    source(\n" +
            "                        firstName,\n" +
            "                        lastName,\n" +
            "                        imageUrl,\n" +
            "                        googleUrl\n" +
            "                    )\n" +
            "                ),\n" +
            "                about,\n" +
            "                in(\n" +
            "                    target(\n" +
            "                        name,\n" +
            "                        photo,\n" +
            "                        firstName,\n" +
            "                        lastName,\n" +
            "                        imageUrl,\n" +
            "                        googleUrl\n" +
            "                    )\n" +
            "                ),\n" +
            "                target(\n" +
            "                    firstName,\n" +
            "                    lastName,\n" +
            "                    googleUrl,\n" +
            "                    imageUrl,\n" +
            "                    around,\n" +
            "                    infoDistance\n" +
            "                ),\n" +
            "                source(\n" +
            "                    imageUrl,\n" +
            "                    googleUrl,\n" +
            "                    firstName,\n" +
            "                    lastName\n" +
            "                ),\n" +
            "                clubs(\n" +
            "                    name\n" +
            "                )\n" +
            "            ),\n" +
            "            target(\n" +
            "                name,\n" +
            "                owner,\n" +
            "                imageUrl,\n" +
            "                googleUrl,\n" +
            "                photo,\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                about\n" +
            "            )\n" +
            "        ),\n" +
            "        clubs(\n" +
            "            name\n" +
            "        )\n" +
            "   "),

    SELECT_HOME("\n" +
            "        date,\n" +
            "        photo,\n" +
            "        about,\n" +
            "        action,\n" +
            "        going,\n" +
            "        want,\n" +
            "        owner,\n" +
            "        likers,\n" +
            "        liked,\n" +
            "        likes(\n" +
            "            source(\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                imageUrl,\n" +
            "                googleUrl\n" +
            "            )\n" +
            "        ),\n" +
            "        members(\n" +
            "            source(\n" +
            "                about,\n" +
            "                date,\n" +
            "                likers,\n" +
            "                liked,\n" +
            "                likes(\n" +
            "                    source(\n" +
            "                        firstName,\n" +
            "                        lastName,\n" +
            "                        imageUrl,\n" +
            "                        googleUrl\n" +
            "                    )\n" +
            "                ),\n" +
            "                source(\n" +
            "                    name,\n" +
            "                    firstName,\n" +
            "                    lastName,\n" +
            "                    imageUrl,\n" +
            "                    googleUrl\n" +
            "                )\n" +
            "            )\n" +
            "        ),\n" +
            "        in(\n" +
            "            target(\n" +
            "                name,\n" +
            "                photo,\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                imageUrl,\n" +
            "                googleUrl\n" +
            "            )\n" +
            "        ),\n" +
            "        joins(\n" +
            "            target(\n" +
            "                name,\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                googleUrl,\n" +
            "                imageUrl,\n" +
            "                photo\n" +
            "            ),\n" +
            "            source(\n" +
            "                name,\n" +
            "                firstName,\n" +
            "                imageUrl,\n" +
            "                photo,\n" +
            "                lastName,\n" +
            "                imageUrl,\n" +
            "                googleUrl\n" +
            "            )\n" +
            "        ),\n" +
            "        source(\n" +
            "            imageUrl,\n" +
            "            googleUrl,\n" +
            "            firstName,\n" +
            "            lastName\n" +
            "        ),\n" +
            "        target(\n" +
            "            name,\n" +
            "            photo,\n" +
            "            firstName,\n" +
            "            lastName,\n" +
            "            imageUrl,\n" +
            "            googleUrl\n" +
            "        ),\n" +
            "        clubs(\n" +
            "            name\n" +
            "        )\n" +
            "   "),

    SELECT_PERSON("\n" +
            "        firstName,\n" +
            "        lastName,\n" +
            "        imageUrl,\n" +
            "        googleUrl,\n" +
            "        infoDistance,\n" +
            "        around,\n" +
            "        hidden,\n" +
            "        backing,\n" +
            "        backers,\n" +
            "        backs(\n" +
            "            source(\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                imageUrl,\n" +
            "                googleUrl\n" +
            "            )\n" +
            "        ),\n" +
            "        photo,\n" +
            "        cover,\n" +
            "        about,\n" +
            "        members(\n" +
            "            role,\n" +
            "            source(\n" +
            "                date,\n" +
            "                name,\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                imageUrl,\n" +
            "                photo,\n" +
            "                about,\n" +
            "                hidden,\n" +
            "                owner,\n" +
            "                going,\n" +
            "                action,\n" +
            "                want,\n" +
            "                liked,\n" +
            "                likers,\n" +
            "                likes(\n" +
            "                    source(\n" +
            "                        firstName,\n" +
            "                        lastName,\n" +
            "                        imageUrl,\n" +
            "                        googleUrl\n" +
            "                    )\n" +
            "                ),\n" +
            "                members(\n" +
            "                    source(\n" +
            "                        name,\n" +
            "                        photo,\n" +
            "                        about,\n" +
            "                        firstName,\n" +
            "                        lastName,\n" +
            "                        imageUrl,\n" +
            "                        date,\n" +
            "                        likers,\n" +
            "                        liked,\n" +
            "                        likes(\n" +
            "                            source(\n" +
            "                                firstName,\n" +
            "                                lastName,\n" +
            "                                imageUrl,\n" +
            "                                googleUrl\n" +
            "                            )\n" +
            "                        ),\n" +
            "                        source(\n" +
            "                            name,\n" +
            "                            photo,\n" +
            "                            firstName,\n" +
            "                            lastName,\n" +
            "                            imageUrl,\n" +
            "                            googleUrl\n" +
            "                        ),\n" +
            "                        target(\n" +
            "                            name,\n" +
            "                            photo,\n" +
            "                            firstName,\n" +
            "                            lastName,\n" +
            "                            imageUrl,\n" +
            "                            googleUrl\n" +
            "                        )\n" +
            "                    )\n" +
            "                ),\n" +
            "                in(\n" +
            "                    target(\n" +
            "                        name,\n" +
            "                        photo,\n" +
            "                        firstName,\n" +
            "                        lastName,\n" +
            "                        imageUrl,\n" +
            "                        googleUrl\n" +
            "                    )\n" +
            "                ),\n" +
            "                joins(\n" +
            "                    source(\n" +
            "                        name,\n" +
            "                        firstName,\n" +
            "                        lastName,\n" +
            "                        photo,\n" +
            "                        imageUrl,\n" +
            "                        googleUrl\n" +
            "                    )\n" +
            "                ),\n" +
            "                source(\n" +
            "                    imageUrl,\n" +
            "                    googleUrl,\n" +
            "                    photo,\n" +
            "                    firstName,\n" +
            "                    lastName\n" +
            "                ),\n" +
            "                target(\n" +
            "                    name,\n" +
            "                    photo,\n" +
            "                    firstName,\n" +
            "                    lastName,\n" +
            "                    imageUrl,\n" +
            "                    googleUrl\n" +
            "                ),\n" +
            "                clubs(\n" +
            "                    name\n" +
            "                )\n" +
            "            ),\n" +
            "            target(\n" +
            "                name,\n" +
            "                owner,\n" +
            "                imageUrl,\n" +
            "                googleUrl,\n" +
            "                photo,\n" +
            "                firstName,\n" +
            "                lastName,\n" +
            "                about\n" +
            "            )\n" +
            "        ),\n" +
            "        clubs(\n" +
            "            name\n" +
            "        )\n" +
            "    "),

    SELECT_THINGS("name,about,hidden,photo,infoDistance,in(target(name,photo,googleUrl,imageUrl,firstName,lastName)),clubs(name)"),
    SELECT_THINGS_WITH_MEMBERS("name,about,hidden,photo,infoDistance,members(source(name,photo,googleUrl,imageUrl,firstName,lastName,source(name,photo,googleUrl,imageUrl,firstName,lastName),target(name,photo,googleUrl,imageUrl,firstName,lastName))),in(target(name,photo,googleUrl,imageUrl,firstName,lastName)),clubs(name)"),
    SELECT_PEOPLE("googleUrl,imageUrl,infoDistance,around,firstName,lastName,about,clubs(name)"),
    SELECT_PEOPLE_MINIMAL("name,photo,about,firstName,lastName,imageUrl,googleUrl,around,infoDistance"),
    SELECT_PERSON_MINIMAL("firstName,lastName,imageUrl"),
    SELECT_ME("auth,googleUrl,imageUrl,firstName,lastName,modes(source),clubs(name)"),
    SELECT_SEARCH("name,about,photo,imageUrl,firstName,lastName,googleUrl,infoDistance"),
    SELECT_MESSAGES("latest,seen,updated,firstName,lastName,photo,message,source(firstName,lastName,googleUrl,imageUrl),target(firstName,lastName,googleUrl,imageUrl)"),
    SELECT_PERSON_MESSAGES("date,photo,message,source(firstName,lastName,googleUrl,imageUrl),target(firstName,lastName,googleUrl,imageUrl)"),

    ;

    private final String query;

    EarthGraphQuery(String query) {
        this.query = query.replaceAll("\\s+", "");
    }

    public RequestParams params() {
        return appendTo(new RequestParams());
    }

    public RequestParams appendTo(RequestParams params) {
        params.put("select", query);
        return params;
    }
}
