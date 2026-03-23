package org.frias.avalon.temp.util;

import java.util.UUID;

public class UtillConversorTypes {

    public static UUID stringToUuid(String uuid) {
        try {
            return UUID.fromString(uuid);
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("verificcar codigo : " + uuid);
        }


    }
}
