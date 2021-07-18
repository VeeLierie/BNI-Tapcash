package id.co.hanoman.tapcash.util;

import java.io.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import id.co.hanoman.tapcash.model.Response;
import id.co.hanoman.tapcash.model.TrxPembayaran;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.hanoman.config.YAMLConfig;
import id.co.hanoman.domain.Token;
import id.co.hanoman.tapcash.model.ErrorResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;


@Component
public class NetClientTapCash {
	static Logger log = LoggerFactory.getLogger(NetClientTapCash.class);

	@Autowired	
	YAMLConfig config ;
	
	public String getToken() throws Exception{
		Token token = null;
		if(token==null){

			String userAndPass = config.getUsername() +":"+ config.getPassword();
			String encodedDataString = Base64.getUrlEncoder().encodeToString(userAndPass.getBytes());

			String body = "grant_type=client_credentials";

			try {
				URL url = new URL(config.getBaseUrl()+"/api/oauth/token");
				log.info("Access Token Url :"+url);
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

				byte[] postData = body.getBytes( StandardCharsets.UTF_8 );
				int postDataLength = postData.length;

				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty( "charset", "utf-8");
				conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
				conn.setRequestProperty("Authorization", "Basic "+ encodedDataString);
				conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));

				OutputStream os = conn.getOutputStream();
				os.write(postData);
				os.flush();

				if (conn.getResponseCode() != 200) {
					ErrorResponse errRes = new ErrorResponse();
					errRes.setCode(String.valueOf(conn.getResponseCode()));
					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
					ObjectMapper mapper = new ObjectMapper();
					JsonNode root = mapper.readTree(br);
					errRes.setError(root);
					log.error("Error Token : "+errRes);
				}else {
					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
					ObjectMapper mapper = new ObjectMapper();
					JsonNode root = mapper.readTree(br);
					log.info("Response Token:"+ root);
					String accessToken = root.get("access_token") != null ?root.get("access_token").asText():"";
					Token tokenNew = new Token();
					tokenNew.setToken(accessToken);
					return accessToken;
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String calculateHMAC(String data, String key) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
		mac.init(secretKeySpec);

		return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes()));
	}
	
	public Object payment(TrxPembayaran req) throws Exception{
		Object resCall = null;
		try {

			String tapcashNum = req.getTapcashNum();
			String amount = req.getAmount();
			String accountNum = req.getAccountNum();

			String request = "{\"tapcashNum\":\""+tapcashNum+"\",\"amount\":\""+amount+"\",\"accountNum\":\""+accountNum+"\"}";

			String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
			String encryptBody = Base64.getUrlEncoder().withoutPadding().encodeToString(request.getBytes());
			String hmac = calculateHMAC(header+"."+encryptBody, config.getSecretKey());

			String bodyRequest = "{\"tapcashNum\":\""+tapcashNum+"\",\"amount\":\""+amount+"\",\"accountNum\":\""+accountNum+"\",\"signature\":\""+header+"."+encryptBody+"."+hmac+"\"}";

			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(bodyRequest);
			resCall = root;
			resCall = callUrl(bodyRequest);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resCall;
	}

	private Object callUrl(String data) throws Exception{

		String access_token  = getToken();
		log.info("Access Token :"+ access_token);

		byte[] postData = data.getBytes( StandardCharsets.UTF_8 );
		int    postDataLength = postData.length;

		URL url = new URL(config.getBaseUrl()+"/sharingBiller/tapcash/pay?access_token="+access_token);
		log.info("CallUrl Payment :"+url);

		try {
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("x-api-key", config.getxApiKey());
			conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));

			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());
			os.flush();

			log.info("Request Body :"+data);

			if (conn.getResponseCode() != 200) {
				ErrorResponse errRes = new ErrorResponse();
				String responseCode = String.valueOf(conn.getResponseCode());
				errRes.setCode(responseCode);
				errRes.setFaultCode("G01");
				errRes.setFaultMessage("Error Server TapCash");
				log.info("error responseCode :"+ responseCode);
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(br);
				errRes.setError(root);

				return errRes;

			}else {
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				log.info("respCode :"+ br);

				ObjectMapper mapper = new ObjectMapper();
				ObjectNode rootNode = mapper.createObjectNode();
				ObjectNode parentNode = mapper.createObjectNode();
				JsonNode root = mapper.readTree(br);

				String[] keys = {"reffNum","journal","financialJournal","voucherNum","errorDescription"};

				ArrayList<String> list = new ArrayList<String>();
				for (String key : keys) {
					JsonNode value = root.findValue(key);
					String findNode = value == null ? null : value.asText();
					list.add(findNode);
				}

				if( list.get(0) == null ) {
					parentNode.put("faultCode", "G02");
					parentNode.put("faultMessage", "Invalid Transaction");
					parentNode.put("errorDescription", list.get(4));
					String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentNode);
					return jsonString;
				}else{
					parentNode.put("ReffNum", list.get(0));
					parentNode.put("Journal", list.get(1));
					parentNode.put("FinancialJournal", list.get(2));
					parentNode.put("VoucherNum", list.get(3));

					rootNode.set("response", parentNode);
					String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
					return jsonString;
				}
			}
		} catch (Exception e) {
			String strErrMsg = e.toString();
			log.info("error payment :"+ strErrMsg);
			ErrorResponse errRes = new ErrorResponse();
			errRes.setCode("400");
			if(strErrMsg.equals("java.net.ConnectException: Connection refused: connect")){
				errRes.setFaultCode("G01");
				errRes.setFaultMessage("Failed to Connect");
			} else if(strErrMsg.equals("java.net.SocketTimeoutException: Read timed out")){
				errRes.setFaultCode("G01");
				errRes.setFaultMessage("Timeout from Server");
			} else if(strErrMsg.equals("java.net.SocketException: Connection reset")){
				errRes.setFaultCode("G01");
				errRes.setFaultMessage("Bad Message Request");
			}else {
				errRes.setFaultCode("G99");
				errRes.setFaultMessage("General Error");
			}
			return errRes;
		}
	}
}


