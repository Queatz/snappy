package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.PrintingError;
import com.queatz.snappy.service.Search;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/8/15.
 */

public class Me implements Api.Path {
    Api api;

    public Me(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                Document me = api.snappy.search.get(Search.Type.PERSON, user);

                if(me == null) {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - inexistent");
                }

                resp.getWriter().write(api.snappy.things.person.toJson(me, user, false).toString());

                break;
            case PUT:


                break;
            case POST:
                /*if(path.size() == 1) {
                    if(Config.PATH_UPTO.equals(path.get(0))) {
                        //TODO create new upto in cloud storage

                        Object photo = req.getAttribute(Config.PARAM_PHOTO);
                        Object location = req.getAttribute(Config.PARAM_LOCATION);

                        GcsFilename uptoId = new GcsFilename(mAppIdentityService.getDefaultGcsBucketName(), "testing");

                        boolean allGood = false;

                        try {
                            ServletFileUpload upload = new ServletFileUpload();
                            FileItemIterator iterator = upload.getItemIterator(req);
                            while (iterator.hasNext()) {
                                FileItemStream item = iterator.next();
                                InputStream stream = item.openStream();

                                if (!item.isFormField()) {
                                    int len;
                                    byte[] buffer = new byte[8192];

                                    GcsOutputChannel outputChannel = mGCS.createOrReplace(uptoId, GcsFileOptions.getDefaultInstance());

                                    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                                        outputChannel.write(ByteBuffer.wrap(buffer, 0, len));
                                    }

                                    outputChannel.close();

                                    allGood = true;

                                    break;
                                }
                            }
                        }
                        catch (FileUploadException e) {
                            Logger.getLogger(Config.NAME).severe(e.toString());
                            throw new Config.PrintingError(Config.Error.SERVER_ERROR, "couldn't upload because " + e);
                        }

                        if(!allGood)
                            throw new Config.PrintingError(Config.Error.NOT_AUTHENTICATED, "me - not all good");

                        resp.getWriter().write("{\"result\": \"success\"}");
                    }
                }
                else {
                    throw new Config.PrintingError(Config.Error.NOT_AUTHENTICATED, "me - bad method");
                }*/
            case DELETE:


                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "me - bad method");
        }
    }
}
