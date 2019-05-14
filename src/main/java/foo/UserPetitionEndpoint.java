package foo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
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
import com.google.appengine.api.users.User;

@Api(name = "cloud")
public class UserPetitionEndpoint {
	
	public UserPetitionEndpoint() {}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            DatastoreService store = DatastoreServiceFactory.getDatastoreService();
            Query query = new Query("User").addSort("listPetitions",  Query.SortDirection.DESCENDING);
            List<Entity> results = store.prepare(query).asList(FetchOptions.Builder.withLimit(100));

            this.getServletContext().getRequestDispatcher("../webapp/main.js").forward(req, resp);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) {	
        try {
			// UserPetition usr = new UserPetition(req.getParameter("name"));
            // ofy().save().entity(usr);
			// store.put(usr);
			
            DatastoreService store = DatastoreServiceFactory.getDatastoreService();
			Entity user = new Entity("UserCloud");
			user.setProperty("listPetitions", null);
			user.setProperty("listPetitionsSignes", null);
	  		user.setProperty("pseudo",req.getParameter("name"));
	  		store.put(user);


            //resp.sendRedirect("/CreatePetition");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public List<Entity> queryForge(DatastoreService datastore, String pseudo){		        
		Filter filter = new FilterPredicate("pseudo", FilterOperator.EQUAL, pseudo);
		Query query = new Query("UserCloud").setFilter(filter);
		PreparedQuery prepQuery = datastore.prepare(query);
		List<Entity> results = prepQuery.asList(FetchOptions.Builder.withDefaults());
		
		return results;
		
	}
	
	
	@ApiMethod(
	        path = "user/getCloud/{pseudo}",
	        httpMethod = HttpMethod.GET
	    )
	public UserPetition getUserPet(@Named("pseudo") String pseudo) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<Entity> results = queryForge(datastore, pseudo);
		
		if(results.size()>0) {
			Entity e = results.get(0);
			UserPetition uc = new UserPetition((String)e.getProperty("pseudo"));
			uc.setListPetitions((ArrayList<String>)e.getProperty("listPetitions"));
			return uc;
		}
		
		return null;				
	}
	
	@ApiMethod(
	        path = "user/create",
	        httpMethod = HttpMethod.POST
	    )
	public User createUser(@Named("email") String email, @Named("auth") String auth) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		User u = new User(email, auth);
		
		if(u != null) {
			Entity user = new Entity("UserCloud");
			user.setProperty("listPetitions", null);
			user.setProperty("listPetitionsSignes", null);
	  		user.setProperty("pseudo",u.getNickname());
	  		datastore.put(user);
	  		return u;
		}else {
			return null;
			
		}
	}
	
	@ApiMethod(
	        path = "user/signPet",
	        httpMethod = HttpMethod.POST
	    )
	public UserPetition signPet(@Named("user") String user, @Named("petition") String petitions) {
		
		UserPetition uc = this.getUserPet(user);
		ArrayList<String> list = uc.getListPetitionsSignes();
		if(list == null) {
			list = new ArrayList<>();
		}
		if(!list.contains(petitions)) {
			list.add(petitions);
			//IL FAUT RECUPERER LA PETITION ET INCREMENTER SON SIGNATURE!!
			Entity pet;
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Filter filter = new FilterPredicate("petition", FilterOperator.EQUAL, petitions);
			Query query = new Query("Petition").setFilter(filter);
			PreparedQuery prepQuery = datastore.prepare(query);
			List<Entity> results = prepQuery.asList(FetchOptions.Builder.withDefaults());
			if(results.size()>0) {
				pet = results.get(0);
				pet.setProperty("listPetitionsSignes", (int)pet.getProperty("listPetitionsSignes")+1);
		  		datastore.put(pet);
			} else {
				return null;
			}
		}
		uc.setListPetitionsSignes(list);
	
		Entity u;
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<Entity> results = queryForge(datastore, user);
	
		if(results.size()>0) {
			u = results.get(0);
			u.setProperty("listPetitionsSignes", uc.getListPetitionsSignes());
	  		u.setProperty("pseudo",uc.getName());
	  		datastore.put(u);
	  		return uc;
		} else {
			return null;
		}
	}
}
