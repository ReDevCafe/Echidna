package org.momento.echidna.network;

import org.bson.Document;

public interface MongoDTO {

    Document toDocument();

}
