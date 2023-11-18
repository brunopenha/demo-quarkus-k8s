package br.nom.penha.bruno.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Product {
    private Long id;
    @NotNull(message = "You must inform the description")
    @NotBlank(message = "Description field can not be empty")
    private String description;

    @NotNull(message = "You must inform the label")
    @NotBlank(message = "Label field can not be empty")
    private String label;

    @NotNull(message = "You must inform the quantity")
    @Min(message = "Quantity can not be zero", value = 1)
    private Integer quantity;

    @NotNull(message = "You must inform the price")
    @Min(message = "Price have to be higher than zero", value = 1)
    private Double price;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
