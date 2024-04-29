package com.dodo.user.dto;

import com.dodo.user.domain.AuthenticationType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserCreateRequestData.PasswordUserCreateRequestData.class, name = "password"),
        @JsonSubTypes.Type(value = UserCreateRequestData.SocialUserCreateRequestData.class, name = "social"),
})
public abstract class UserCreateRequestData {

    @JsonTypeName("social")
    @Getter
    public static class SocialUserCreateRequestData extends UserCreateRequestData {

        // TODO
        //

        String token;
        public SocialUserCreateRequestData() {
            super(AuthenticationType.SOCIAL);
        }
    }

    @JsonTypeName("password")
    @Getter
    public static class PasswordUserCreateRequestData extends UserCreateRequestData {
        private String password1;
        private String password2;
        private String username;

        public PasswordUserCreateRequestData() {
            super(AuthenticationType.PASSWORD);
        }
        public String getPassword() {
            if(password1.equals(password2)) {
                return password1;
            } else {
                return null;
            }
        }
    }

    private AuthenticationType type;
    private String email;
    private String firebaseToken;

    public UserCreateRequestData(AuthenticationType type) {
        this.type = type;
    }

}
