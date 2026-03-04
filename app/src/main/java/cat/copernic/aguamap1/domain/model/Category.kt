package cat.copernic.aguamap1.domain.model

/**
 * Representa una categoría de clasificación para las fuentes de agua.
 * Se utiliza para agrupar fuentes con características similares y facilitar
 * el filtrado o la navegación del usuario en el mapa.
 *
 * @property id Identificador único de la categoría (coincide con el ID del documento en Firestore).
 * @property name Nombre descriptivo de la categoría (ej. "Fuente Ornamental", "Agua Potable").
 * @property imageUrl URL de la imagen o icono que representa visualmente a la categoría.
 * @property description Breve explicación sobre qué tipo de fuentes pertenecen a este grupo.
 */
data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val description: String = ""
)