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
        @JsonSubTypes.Type(value = MemberCreateRequestData.PasswordMemberCreateRequestData.class, name = "password"),
        @JsonSubTypes.Type(value = MemberCreateRequestData.SocialMemberCreateRequestData.class, name = "social"),
})
public abstract class MemberCreateRequestData {

    @JsonTypeName("social")
    @Getter
    public static class SocialMemberCreateRequestData extends MemberCreateRequestData {

        // TODO
        //

        String token;
        public SocialMemberCreateRequestData() {
            super(AuthenticationType.SOCIAL);
        }
    }

    @JsonTypeName("password")
    @Getter
    public static class PasswordMemberCreateRequestData extends MemberCreateRequestData {
        private String password1;
        private String password2;
        private String membername;

        public PasswordMemberCreateRequestData() {
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

    public MemberCreateRequestData(AuthenticationType type) {
        this.type = type;
    }

}
