# rest-client

A rest client that can call REST APIs using Resteasy Proxies.

## Configuration

As part of this REST client you can use a Configuration class within the application server. It uses two JNDI-Entries to access the configuration files.

* java:global/configurationFilename
* java:global/passwordFilename

In a Wildfly application server you can e.g. place the configuration files in the Wildfly configuration directory by putting this entry into the standalone.xml file.

    <bindings>
        <simple name="java:global/configurationFilename" value="${jboss.server.config.dir}/backend.properties"/>
        <simple name="java:global/passwordFilename" value="${jboss.server.config.dir}/secrets.properties"/>
    </bindings>

## Usage

First of all we need a simple bean class that is used for the data transfer. For e.g. a simple customer class:

    public class Customer {
        private Long customerNr;
        private String name;
        ...
    }

In the REST API we are using interfaces to define that API:

    @Path("customer")
    public interface CustomerResource {
        @GET
        @Path("/{customerNr}")
        @Produces(MediaType.APPLICATION_JSON)
        public Customer getCustomer(@PathParam("customerNr") Long customerNr);
    }

The implementation should not be part of this client, but it will look like this:

    @RequestScoped
    public class CustomerResourceImpl implements CustomerResource {
        @Inject
        private CustomerService customerService;

        @Override
        public Customer getCustomer(Long customerNr) {
            // Do some error handling
            return customerService.getCustomerFromDatabase(customerNr);
        }
    }

Before using the RestClient in an enterprise application we have to configure the endpoint in our configuration file.
It it simply accessed by its full classname and the .endpoint suffix:

    de.beit.web.example.customer.rest.CustomerResource.endpoint = http://localhost:8090/customer-service/api

In an enterprise application you can now inject the RestClient into e.g. a stateless bean:

    @Stateless
    public class CustomerService {
        @Inject
        private RestClient restClient;

        public Customer getCustomerFromDatabase(Long customerNr) {
            // Find Customer in Database
            Customer result = restClient.endpoint(CustomerResource.class).getCustomer(customerNr);
            return result;
        }
    }