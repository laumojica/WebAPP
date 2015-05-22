package gdm.tdm.pacientes.servicios;

import co.edu.uniandes.csw.isis2503.security.jwt.api.JsonWebToken;
import co.edu.uniandes.csw.isis2503.security.jwt.api.JwtHashAlgorithm;
import co.edu.uniandes.csw.isis2503.security.logic.dto.UserDTO;
import com.google.gson.Gson;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.api.ApiKey;
import com.stormpath.sdk.api.ApiKeys;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.application.ApplicationList;
import com.stormpath.sdk.application.Applications;
import com.stormpath.sdk.authc.AuthenticationRequest;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.authc.UsernamePasswordRequest;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import com.stormpath.sdk.resource.ResourceException;
import com.stormpath.sdk.tenant.Tenant;
import gdm.tdm.pacientes.persistencia.PersistenceManager;
import gdm.tdm.pacientes.pojos.DTO.AlarmaDTO;
import gdm.tdm.pacientes.pojos.DTO.ListaAlarmasDTO;
import gdm.tdm.pacientes.pojos.Paciente;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "paciente" path)
 */
@Path("/paciente/")
public class PacienteService {

    @PersistenceContext(unitName = "PacientesPu")
    EntityManager entityManager;
    
    @PostConstruct
    public void init(){
        try{
            entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Hello, desde Paciente";
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearPaciente(Paciente nuevo){
        try{
            entityManager.getTransaction().begin();
            entityManager.persist(nuevo);
            entityManager.getTransaction().commit();
            
        }catch(Throwable t){
            t.printStackTrace();
            if(entityManager.getTransaction().isActive()){
                entityManager.getTransaction().rollback();
            }
            nuevo = null;
        }finally{
            entityManager.clear();
            entityManager.close();
        }
        return Response.status(200).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS").header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition, Content-Description").entity("{\"id\":"+nuevo.getId()+"}").build();
    }
    
    @GET
    @Path("{id}/causas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response darCausas(@PathParam("id") String id){
        String query = "select c.nombre, COUNT(c.id) from episodio_causa ec inner join causa c on ec.causas_id = c.id GROUP BY c.id HAVING c.paciente_id = ?1";
        Query q = entityManager.createNativeQuery(query);
        q.setParameter(1, Long.parseLong(id));
        List<Object[]> resultados = q.getResultList();
        List<AlarmaDTO> listaAlarmas = new LinkedList<>();
        for(Object[] l : resultados){
            AlarmaDTO dto = new AlarmaDTO((String)l[0], (Long)l[1]);
            listaAlarmas.add(dto);
        }
        return Response.status(200).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS").header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition, Content-Description").entity(new ListaAlarmasDTO(listaAlarmas)).build();
    }
    
    @Path("login")
    @POST
    public Response login(UserDTO user) {
        int status = 500; //Codigo de error en el servidor
        String token = "User and/or password wrong";
        UserDTO userStorm = new UserDTO();
        String path = "C:\\Users\\TEXONE\\DownloadsapiKey.properties";//Colocar la Ubicacion de su archivo apiKey.properties
        ApiKey apiKey = ApiKeys.builder().setFileLocation(path).build();
        Client client = Clients.builder().setApiKey(apiKey).build();

        try {
            AuthenticationRequest request = new UsernamePasswordRequest(user.getUsername(), user.getPassword());
            Tenant tenant = client.getCurrentTenant();
            ApplicationList applications = tenant.getApplications(Applications.where(Applications.name().eqIgnoreCase("Aplicacion Grupo-7")));
            Application application = applications.iterator().next();

            AuthenticationResult result = application.authenticateAccount(request);
            Account account = result.getAccount();
            userStorm.setEmail(account.getEmail());
            userStorm.setName(account.getFullName());
            userStorm.setUsername(account.getUsername());
            userStorm.setPassword(user.getPassword());
            userStorm.setLevelAccess("Admin");
            token = new Gson().toJson(JsonWebToken.encode(userStorm, "Un14nd3s2014@", JwtHashAlgorithm.HS256));
            status = 200;

        } catch (ResourceException ex) {
            System.out.println(ex.getStatus() + " " + ex.getMessage());
        }

        return Response.status(status).entity(token).build();
    }
    
}
