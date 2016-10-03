package br.jus.trf2.assijus;

import org.json.JSONObject;

import br.jus.trf2.assijus.IAssijus.AuthPostRequest;
import br.jus.trf2.assijus.IAssijus.AuthPostResponse;
import br.jus.trf2.assijus.IAssijus.IAuthPost;

import com.crivano.restservlet.PresentableException;
import com.crivano.restservlet.RestUtils;
import com.crivano.swaggerservlet.SwaggerUtils;

public class AuthPost implements IAuthPost {

	@Override
	public void run(AuthPostRequest req, AuthPostResponse resp)
			throws Exception {
		String authkey = req.authkey;
		String token = req.token;
		String payload = null;

		if (authkey != null) {
			payload = RestUtils.dbRetrieve(authkey, false);

			if (payload.startsWith("TOKEN-"))
				// A token is stored
				token = payload;
			else if (payload.startsWith("{")) {
				// A client-cert authentication is stored
				JSONObject json = new JSONObject(payload);
				resp.certificate = SwaggerUtils.base64Decode(json
						.getString("certificate"));
				resp.name = json.getString("name");
				resp.cpf = json.getString("cpf");
				resp.kind = "client-cert";
				return;
			}
		}

		if (token != null) {
			JSONObject json = Utils.validateToken(token,
					Utils.getUrlBluCServer());
			String cpf = null;
			cpf = json.getJSONObject("certdetails").getString("cpf0");

			// Produce response
//			resp.certificate = SwaggerUtils.base64Decode(json
//					.getString("certificate"));
			resp.name = json.getString("cn");
			resp.cpf = cpf;
			resp.token = token;
			resp.kind = "signed-token";

			String key = RestUtils.dbStore(SwaggerUtils.toJson(resp));
			resp.authkey = key;
			return;
		}

		throw new PresentableException(
				"Não foi possível realizar a autenticação. Por favor, lance novamente o aplicativo Assijus.Exe. Se o problema persistir, tente reiniciar o computador");
	}

	@Override
	public String getContext() {
		return "autenticar";
	}

}
