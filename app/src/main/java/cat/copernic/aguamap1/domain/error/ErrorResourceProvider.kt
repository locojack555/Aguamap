// domain/error/ErrorResourceProvider.kt
package cat.copernic.aguamap1.domain.error

interface ErrorResourceProvider {
    fun getString(resId: Int): String
    fun getString(resId: Int, vararg args: Any): String
}