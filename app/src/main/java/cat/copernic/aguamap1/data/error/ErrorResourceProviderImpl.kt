package cat.copernic.aguamap1.data.error

import android.content.Context
import cat.copernic.aguamap1.domain.error.ErrorResourceProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorResourceProviderImpl @Inject constructor(
    private val context: Context
) : ErrorResourceProvider {

    override fun getString(resId: Int): String = context.getString(resId)

    override fun getString(resId: Int, vararg args: Any): String =
        context.getString(resId, *args)
}