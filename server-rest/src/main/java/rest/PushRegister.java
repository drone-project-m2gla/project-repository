package rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import service.PushService.TypeClient;
import service.impl.PushServiceImpl;

import com.google.android.gcm.server.MulticastResult;

import entity.PushEntity;

@Path("/register")
public class PushRegister {
	private static final Logger LOGGER = Logger.getLogger(PushRegister.class);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void registerClient(PushEntity entity) {
		PushServiceImpl.getInstance().registerClient(entity.getTypeClient(), entity.getId());
		LOGGER.info("User connect to push service " + entity.getId() + " " + entity.getTypeClient());
	}
	
	@DELETE
	@Path("/{id}")
	public void unregisterClient(@PathParam("id") String id) {
		PushServiceImpl.getInstance().unregisterClient(id);
		LOGGER.info("User disconect to push service " + id);
	}
	
	@POST
	@Path("add/{scope}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response testPush(@PathParam("scope") String scope, Object object) {
		//FIXME Pour les tests, a supprimer
		Response response;
		
		try {			
			MulticastResult result = PushServiceImpl.getInstance().sendMessage(TypeClient.ALL, scope, object);
			
			if (result == null) throw new Exception("Not user");
			
			if (result.getSuccess() == 1) {
				response =  Response.status(Status.CREATED).entity(result).build();
			} else {
				response =  Response.status(Status.SERVICE_UNAVAILABLE).entity(result).build();
			}
		} catch (Exception e) {
			LOGGER.error("Error push message", e);
			response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e).build();
		}
		
		return response;
	}
}
