// gateway/proxy.go

package gateway

import (
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"

	"github.com/gin-gonic/gin"
)

func ReverseProxy(target string) gin.HandlerFunc {
	return func(c *gin.Context) {
		remoto, err := url.Parse(target)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "URL de destino inválida"})
			return
		}
		// NewSingleHostReverseProxy crea un proxy que preserva el esquema y host
		// destino pero permite reescritura de path, ideal para enrutamiento hacia
		// microservicios donde el prefijo de ruta difiere entre gateway y servicio
		proxy := httputil.NewSingleHostReverseProxy(remoto)
		fmt.Println("Redirigiendo a:", remoto.String())

		// Extracción y reescritura de path: elimina el prefijo de enrutamiento del
		// gateway (/python/*path o /java/*path) dejando solo el path del microservicio,
		// transformando ej. /python/alertas -> /alertas en el servicio destino
		path := c.Param("path")
		c.Request.URL.Path = path

		// Manejador de errores personalizado: intercepta fallos de comunicación
		// para devolver respuestas JSON consistentes con el contrato de la API
		// en lugar de respuestas HTML por defecto del proxy
		proxy.ErrorHandler = func(rw http.ResponseWriter, req *http.Request, err error) {
			c.JSON(http.StatusBadGateway, gin.H{"error": "Error de conexión con el microservicio", "detalle": err.Error()})
		}

		// ServeHTTP delega completamente el control del ciclo petición-respuesta:
		// transmite headers, body, y status code bidireccionalmente entre cliente
		// y microservicio, actuando como intermediario transparente
		proxy.ServeHTTP(c.Writer, c.Request)
	}
}
