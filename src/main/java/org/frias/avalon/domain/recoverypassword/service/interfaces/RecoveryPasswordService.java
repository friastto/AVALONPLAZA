package org.frias.avalon.domain.recoverypassword.service.interfaces;

import java.util.List;

public interface RecoveryPasswordService {

    List<String> initiateRecovery(String identification, String method);
    void finishRecovery(String userName, String token, String newPassword);
}
