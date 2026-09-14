/**
 * VELMORE – Scripts de interfaz
 * Funciones auxiliares para la UI: copiar mensaje WhatsApp y confirmar eliminación.
 */

function copiarMensaje() {
    const mensaje = document.getElementById("mensajeGenerado");
    if (!mensaje) return;
    if (navigator.clipboard) {
        navigator.clipboard.writeText(mensaje.textContent.trim())
            .then(() => alert("¡Mensaje copiado! Pégalo en WhatsApp."))
            .catch(() => fallbackCopiar(mensaje.textContent.trim()));
    } else {
        fallbackCopiar(mensaje.textContent.trim());
    }
}

function fallbackCopiar(texto) {
    const el = document.createElement("textarea");
    el.value = texto;
    document.body.appendChild(el);
    el.select();
    document.execCommand("copy");
    document.body.removeChild(el);
    alert("¡Mensaje copiado! Pégalo en WhatsApp.");
}

function confirmarEliminacion() {
    return confirm("¿Seguro que deseas eliminar este producto? Esta acción no se puede deshacer.");
}
