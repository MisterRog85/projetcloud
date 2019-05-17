package foo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

@Api(name = "cloud")
public class PetitionEndpoint {
	
	@ApiMethod(name = "listAllPets")
	public List<Entity> listPetitionsEntity() {
			Query q = new Query("Petition");

			//Récupération de toutes les pétitions dans une mliste d'entités
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			PreparedQuery prepQuery = datastore.prepare(q);
			List<Entity> result = prepQuery.asList(FetchOptions.Builder.withDefaults());
			return result;
	}


	@ApiMethod(name = "listPetsCents")
	public List<Entity> listPetitionsCentsEntity(@Named("name") String name) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("Petition").addSort("nbSignataires", Query.SortDirection.DESCENDING); 
		PreparedQuery pq = store.prepare(query);
		return pq.asList(FetchOptions.Builder.withLimit(100));
	}
	
	@ApiMethod(name = "newPet")
	public Entity newPet(@Named("owner") String owner, @Named("contenu") String contenu, @Named("titre") String titre ) {
			
			Entity e = new Entity("Petition");
			e.setProperty("owner", owner);
			e.setProperty("titre", titre);
			e.setProperty("contenu", contenu);
			ArrayList<String> signataire= new ArrayList<>();
			e.setProperty("signataires", signataire);
			e.setProperty("nbSignataires", 0);		
			

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(e);
			
			return  e;
	}


	@ApiMethod(name = "signPet")
	public Entity signPet(@Named("Key") Key idPets, @Named("idUser") Key idUser) {
			
	
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Entity e = new Entity(idPets);
			try {
				e = datastore.get(idPets);
				
				Entity user = new Entity(idUser);
				user = datastore.get(idUser);
				
				String name = (String) user.getProperty("name");
				
				ArrayList<String> signataires = (ArrayList<String>) e.getProperty("signataires");
				if(!signataires.contains(name)) {
					signataires.add(name);
					e.setProperty("nbSignataires", signataires.size());
					
					ArrayList<Key> petitionsSignes = (ArrayList<Key>) user.getProperty("petitionsSignes");
					petitionsSignes.add(idPets);
					
				}
				
				datastore.put(e);
			} catch (EntityNotFoundException e1) {

				e1.printStackTrace();
			}
			
			return  e;
	}
	
	@ApiMethod(name = "petitionsSignesParUSer")
	public ArrayList<Key> petitionsSignesParUser(@Named("Key") Key idUser) {
			ArrayList<Key> listePets = new ArrayList<>();

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Entity user = new Entity(idUser);
			try {
				user = datastore.get(idUser);
				listePets = (ArrayList<Key>) user.getProperty("petitionsSignes");
			} catch (EntityNotFoundException e1) {

				e1.printStackTrace();
			}
			
			
			
			return listePets;
			
	}
	
	
	
	
}