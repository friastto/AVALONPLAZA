package org.frias.avalon.domain.user.domain.model;


import lombok.Getter;

@Getter
public class UserAvalonDomain {
    private Long id;

    private Long personId;

    private String userName;

    private String hashSalt;

    private String hashPassword;

    private Long statusId;

    private String statusCode;

    public UserAvalonDomain(Long id, String userName, Long statusId) {
        this.id = id;
        this.userName = userName;
        this.statusId = statusId;
    }

    public UserAvalonDomain(
            Long id,
            Long personId,
            String userName,
            String hashSalt,
            String hashPassword,
            Long statusId
    ) {
        this.id = id;
        this.personId = personId;
        this.userName = userName;
        this.hashSalt = hashSalt;
        this.hashPassword = hashPassword;
        this.statusId = statusId;
    }

    public UserAvalonDomain(
            String userName,
            String hashSalt,
            String hashPassword,
            Long statusId
    ) {
        this.userName = userName;
        this.hashSalt = hashSalt;
        this.hashPassword = hashPassword;
        this.statusId = statusId;
    }

    public UserAvalonDomain(Long id, Long personId, String userName, Long statusId) {
        this.id = id;
        this.personId = personId;
        this.userName = userName;
        this.hashSalt = hashSalt;
        this.hashPassword = hashPassword;
        this.statusId = statusId;
    }

    public static UserAvalonDomain create(
            String userName,
            String hashSalt,
            String hashPassword,
            Long statusId
    ) {

        return new UserAvalonDomain(
                userName,
                hashSalt,
                hashPassword,
                statusId
        );
    }


    public static UserAvalonDomain fromPersistenceBasic(Long id, Long personId, String userName, Long statusId) {

        return new UserAvalonDomain(id, personId, userName, statusId);

    }

    public static UserAvalonDomain fromPersistenceAdvanced(
            Long id,
            Long personId,
            String userName,
            String hashSalt,
            String hashPassword,
            Long statusId
    ) {

        return new UserAvalonDomain(
                id,
                personId,
                userName,
                hashSalt,
                hashPassword,
                statusId);

    }


}
