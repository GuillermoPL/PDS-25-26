package es.um.pds.tableros.domain.ports.input.card.commands;

public record AnadirEtiquetaCommand(
    String cardId, 
    String nombre, 
    String color
) {}