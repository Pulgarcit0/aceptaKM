package com.mavacode.aceptakm.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class BillingManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onPurchaseSuccess: (Purchase) -> Unit,
    private val getCurrentUserId: () -> String?,
    private val onPurchaseError: (String) -> Unit = {}
) : PurchasesUpdatedListener {

    private val TAG = "BillingManager"

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun iniciarConexion(onConnected: () -> Unit = {}) {
        if (billingClient.isReady) {
            Log.d(TAG, "BillingClient ya estaba listo")
            onConnected()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient conectado OK")
                    onConnected()
                } else {
                    Log.e(
                        TAG,
                        "Error al conectar BillingClient: code=${billingResult.responseCode} msg=${billingResult.debugMessage}"
                    )
                    onPurchaseError(
                        billingResult.debugMessage ?: "No se pudo conectar con Google Play"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "BillingClient desconectado")
            }
        })
    }

    fun lanzarCobroSuscripcion(activity: Activity, productId: String, oldPurchaseToken: String? = null) {
        Log.d(TAG, "lanzarCobroSuscripcion productId=$productId, actualizando: ${oldPurchaseToken != null}")
        coroutineScope.launch {
            try {
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )

                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()

                val productDetailsResult = withContext(Dispatchers.IO) {
                    billingClient.queryProductDetails(params)
                }

                if (productDetailsResult.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    val msg = productDetailsResult.billingResult.debugMessage ?: "Error consultando productos"
                    onPurchaseError(msg)
                    return@launch
                }

                val productDetails = productDetailsResult.productDetailsList?.firstOrNull()
                if (productDetails == null) {
                    onPurchaseError("Producto no disponible: $productId")
                    return@launch
                }

                val offerToken = productDetails.subscriptionOfferDetails
                    ?.find { it.offerId == "pruebagratis7dias" }
                    ?.offerToken
                    ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

                if (offerToken == null) {
                    onPurchaseError("No hay oferta disponible para este plan")
                    return@launch
                }

                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )

                val flowParamsBuilder = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)

                // ======== LÓGICA INTELIGENTE DE UPGRADE / DOWNGRADE ========
                if (!oldPurchaseToken.isNullOrEmpty()) {
                    val isUpgrade = productId == "aceptakm_anual"

                    val modoReemplazo = if (isUpgrade) {
                        BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_PRORATED_PRICE
                    } else {
                        BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.DEFERRED
                    }

                    flowParamsBuilder.setSubscriptionUpdateParams(
                        BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                            .setOldPurchaseToken(oldPurchaseToken)
                            .setSubscriptionReplacementMode(modoReemplazo)
                            .build()
                    )
                }
                // ==========================================================

                val launchResult = billingClient.launchBillingFlow(activity, flowParamsBuilder.build())

                if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    onPurchaseError(launchResult.debugMessage ?: "No se pudo abrir el pago")
                }
            } catch (e: Exception) {
                onPurchaseError(e.message ?: "Error al iniciar el cobro")
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d(
            TAG,
            "onPurchasesUpdated code=${billingResult.responseCode} purchases=${purchases?.size ?: 0}"
        )
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    Log.w(TAG, "OK pero sin purchases")
                    onPurchaseError("No se recibió la compra")
                    return
                }
                purchases.forEach { purchase ->
                    Log.d(
                        TAG,
                        "Purchase state=${purchase.purchaseState} ack=${purchase.isAcknowledged} products=${purchase.products}"
                    )
                    confirmarCompra(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Usuario canceló la compra")
                onPurchaseError("cancelado")
            }
            else -> {
                Log.e(
                    TAG,
                    "Error en compra: code=${billingResult.responseCode} msg=${billingResult.debugMessage}"
                )
                onPurchaseError(
                    billingResult.debugMessage ?: "Error en la compra (${billingResult.responseCode})"
                )
            }
        }
    }

    private fun confirmarCompra(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            Log.w(TAG, "Ignorando compra: state=${purchase.purchaseState}")
            return
        }

        coroutineScope.launch {
            if (!purchase.isAcknowledged) {
                val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                val ackResult = withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgeParams)
                }

                if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.e(TAG, "Error al acknowledge: ${ackResult.debugMessage}")
                    onPurchaseError(
                        ackResult.debugMessage ?: "Error al confirmar la compra con Google"
                    )
                    return@launch
                }
                Log.d(TAG, "Compra acknowledgeada OK")
            } else {
                Log.d(TAG, "Compra ya acknowledgeada — igual validamos en backend")
            }

            val uid = getCurrentUserId()
            val productId = purchase.products.firstOrNull()

            if (uid == null || productId == null) {
                Log.e(TAG, "uid o productId null. uid=$uid productId=$productId")
                onPurchaseError("Usuario no autenticado o producto inválido")
                return@launch
            }

            Log.d(
                TAG,
                "Enviando a backend uid=$uid productId=$productId tokenLen=${purchase.purchaseToken.length}"
            )
            val backendOk = enviarTokenAlBackend(uid, productId, purchase.purchaseToken)

            if (backendOk) {
                Log.d(TAG, "Backend OK — premium debería estar en Firestore")
                onPurchaseSuccess(purchase)
            } else {
                Log.e(TAG, "Backend FALLÓ — revisa Cloud Function logs")
                onPurchaseError(
                    "La compra se hizo en Google, pero no se pudo activar en el servidor. Reintenta o contacta soporte."
                )
            }
        }
    }

    fun restaurarCompras() {
        Log.d(TAG, "restaurarCompras()")
        coroutineScope.launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val result = withContext(Dispatchers.IO) {
                billingClient.queryPurchasesAsync(params)
            }

            Log.d(
                TAG,
                "queryPurchases code=${result.billingResult.responseCode} count=${result.purchasesList.size}"
            )

            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                result.purchasesList.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        confirmarCompra(purchase)
                    }
                }
            } else {
                Log.e(TAG, "Error restaurar: ${result.billingResult.debugMessage}")
            }
        }
    }

    fun cerrar() {
        if (billingClient.isReady) {
            billingClient.endConnection()
            Log.d(TAG, "BillingClient cerrado")
        }
    }
}

// ==================== BACKEND ====================

suspend fun enviarTokenAlBackend(
    uid: String,
    subscriptionId: String,
    purchaseToken: String
): Boolean = withContext(Dispatchers.IO) {
    val tag = "BillingManager"
    try {
        val url = URL("https://us-central1-aceptakm.cloudfunctions.net/validarSuscripcionPlayStore")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000

        val json = JSONObject().apply {
            put("uid", uid)
            put("subscriptionId", subscriptionId)
            put("purchaseToken", purchaseToken)
        }.toString()

        val bytes = json.toByteArray(Charsets.UTF_8)
        connection.setRequestProperty("Content-Length", bytes.size.toString())
        Log.d(tag, "POST body=$json")

        connection.outputStream.use { os ->
            os.write(bytes)
            os.flush()
        }

        val code = connection.responseCode
        val body = try {
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            stream?.bufferedReader()?.readText()
        } catch (e: Exception) {
            Log.e(tag, "No se pudo leer body", e)
            null
        }

        Log.d(tag, "Backend response code=$code body=$body")
        code == HttpURLConnection.HTTP_OK
    } catch (e: Exception) {
        Log.e(tag, "Error al llamar backend", e)
        false
    }
}