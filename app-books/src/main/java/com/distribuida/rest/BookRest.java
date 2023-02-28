package com.distribuida.rest;

import com.distribuida.clientes.authors.AuthorRestProxy;
import com.distribuida.clientes.authors.AuthorsCliente;
import com.distribuida.db.Book;
import com.distribuida.dtos.BookDto;
import com.distribuida.servicios.BookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundTimeout;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.faulttolerance.*;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)


public class BookRest {

    @Inject BookRepository bookService;

    @RestClient
    @Inject AuthorRestProxy proxyAuthor;

    /**
     * GET          /books/{id}     buscar un libro por ID
     * GET          /books          listar todos
     * POST         /books          insertar
     * PUT/PATCH    /books/{id}     actualizar
     * DELETE       /books/{id}     eliminar
     */

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Integer id) {
        Optional<Book> ret = bookService.findById(id);

        if( ret.isPresent() ) {
            return Response.ok(ret.get()).build();
        }
        else {
            String msg = String.format( "Book[id=%d] not found.", id );

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    public List<Book> findAll() {
        System.out.println("Buscando todos");
        return bookService.findAll();
    }

    @GET
    @Path("/all")
    @Retry(maxRetries = 2, delay = 400, maxDuration = 3000, jitter = 300)
    @Timeout(value = 5000, unit = ChronoUnit.MILLIS)
    @Bulkhead(waitingTaskQueue = 10, value = 5)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.4, successThreshold = 3, delay = 5000)
    @Fallback(fallbackMethod = "findAll")
    public List<BookDto> findAllCompleto() throws Exception {
        var books = bookService.findAll();
//
//       AuthorRestProxy proxyAuthor = RestClientBuilder.newBuilder()
//                .baseUrl( new URL("http://127.0.0.1:8080") )
//                .connectTimeout( 1000, TimeUnit.MILLISECONDS )
//                .build( AuthorRestProxy.class );

        List<BookDto> ret = books.stream()
                .map(s -> {
                    System.out.println("*********buscando " + s.getId() );

                    AuthorsCliente author = proxyAuthor.findById(s.getId().longValue());
                    return new BookDto(
                            s.getId(),
                            s.getIsbn(),
                            s.getTitle(),
                            s.getAuthor(),
                            s.getPrice(),
                            String.format("%s, %s", author.getLastName(), author.getFirtName())
                    );
                })
                .collect(Collectors.toList());

        return ret;
    }

    @POST
    public void insert(Book book) {
        bookService.insert(book);
    }

    @PUT
    @Path("/{id}")
    public void update(Book book, @PathParam("id") Integer id) {
        book.setId(id);

        bookService.update(book);
    }

    @DELETE
    @Path("/{id}")
    public void delete( @PathParam("id") Integer id ) {
        bookService.delete(id);
    }
}
