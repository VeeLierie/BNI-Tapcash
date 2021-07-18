package id.co.hanoman.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.hanoman.tapcash.model.TrxPembayaran;
import id.co.hanoman.tapcash.util.NetClientTapCash;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


@Api(value="tapcash", description="Gateway for tapcash api")
@RestController
@RequestMapping("/tapcash")

public class TapcashController {

	@Autowired
	NetClientTapCash netClientTapCash;
	
	static Logger log = LoggerFactory.getLogger(NetClientTapCash.class);
	
	@ApiOperation(value = "Pembayaran",response = Iterable.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved list"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})

	@RequestMapping(value = "/pembayaran", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	
	public ResponseEntity<Object> pembayaran(@RequestBody TrxPembayaran req){
		Object res = null;
		try {
			log.info("request pembayaran : "+getJson(req));
			res = netClientTapCash.payment(req);
			log.info("response Pembayaran : "+getJson(res));
		} catch (Exception e) {
			log.error("pembayaran",e);
		}		
		return ResponseEntity.ok(res);
	}

	public JsonNode getJson(Object obj){
		ObjectMapper mapper = new ObjectMapper();
		JsonNode reqJson = mapper.valueToTree(obj);
		return reqJson;
	}
	
}
