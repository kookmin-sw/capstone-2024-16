package com.dodo.member.dto;

import com.dodo.member.domain.AuthenticationType;
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
        @JsonSubTypes.Type(value = MemberLoginRequestData.PasswordLoginRequestData.class, name = "password")
//        @JsonSubTypes.Type(value =..class, name = "social"),
})
public abstract class MemberLoginRequestData {
    private AuthenticationType authenticationType;

    @JsonTypeName("social")
    @Getter
    public static class SocialLoginRequestData extends MemberLoginRequestData {
        private String token;

        public SocialLoginRequestData() {
            super(AuthenticationType.SOCIAL);
        }
    }

    @JsonTypeName("password")
    @Getter
    public static class PasswordLoginRequestData extends MemberLoginRequestData {
        private String email;
        private String password;

        public PasswordLoginRequestData() {
            super(AuthenticationType.PASSWORD);
        }
    }

    public MemberLoginRequestData(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }
}
