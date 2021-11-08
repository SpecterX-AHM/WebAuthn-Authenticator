package app.webauthn

import com.yubico.internal.util.JacksonCodecs
import com.yubico.webauthn.RegistrationTestData
import com.yubico.webauthn.data.AuthenticatorAttestationResponse
import app.webauthn.data.RegistrationResponse
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.Matchers
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonSerializationSpec extends FunSpec with Matchers {

  private val jsonMapper = JacksonCodecs.json()

  val testData = RegistrationTestData.FidoU2f.BasicAttestation
  val authenticationAttestationResponseJson =
    s"""{"attestationObject":"${testData.response.getResponse.getAttestationObject.getBase64Url}","clientDataJSON":"${testData.response.getResponse.getClientDataJSON.getBase64Url}","transports":["usb"]}"""
  val publicKeyCredentialJson =
    s"""{"id":"${testData.response.getId.getBase64Url}","response":${authenticationAttestationResponseJson},"clientExtensionResults":{},"type":"public-key"}"""
  val registrationResponseJson =
    s"""{"requestId":"request1","credential":${publicKeyCredentialJson}}"""

  it("RegistrationResponse can be deserialized from JSON.") {
    val parsed = jsonMapper.readValue(
      registrationResponseJson,
      classOf[RegistrationResponse],
    )
    parsed.getCredential should equal(testData.response)
  }

  it("AuthenticatorAttestationResponse can be deserialized from JSON.") {
    val parsed = jsonMapper.readValue(
      authenticationAttestationResponseJson,
      classOf[AuthenticatorAttestationResponse],
    )
    parsed should not be null
  }

}
