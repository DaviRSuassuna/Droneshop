package br.com.droneshop.keycloak;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.directgrant.ValidateOTP;

public class OtpAeria extends ValidateOTP {

    @Override
    public String getId() {
        return "aeria-direct-grant-otp";
    }

    @Override
    public void authenticate(AuthenticationFlowContext contexto) {
        var formulario = contexto.getHttpRequest().getDecodedFormParameters();
        if (formulario.getFirst("otp") == null && formulario.getFirst("totp") == null) {
            contexto.forceChallenge(errorResponse(400, "otp-necessario", "Informe o código OTP."));
            return;
        }
        super.authenticate(contexto);
    }
}
