package br.nom.penha.bruno.resources;

import br.nom.penha.bruno.dto.Product;
import br.nom.penha.bruno.dto.Result;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/product")
public class ProductResource {

    @Inject
    Validator validator;

    @POST
    public Result addProduct(@Valid Product product){

        return new Result("Product added");

    }
}
